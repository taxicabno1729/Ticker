package com.example.liveticker.data

import com.example.liveticker.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

class WalletRepository {

    suspend fun getNativeBalance(address: String, rpcUrl: String = BuildConfig.RPC_ETHEREUM): Resource<BigDecimal> {
        return withContext(Dispatchers.IO) {
            try {
                val web3j = Web3j.build(HttpService(rpcUrl))
                val balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send()
                val etherBalance = Convert.fromWei(balance.balance.toString(), Convert.Unit.ETHER)
                Resource.Success(etherBalance)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Error fetching balance")
            }
        }
    }

    suspend fun getPortfolio(walletAddress: String, coinRepository: CoinRepository): Resource<List<PortfolioToken>> {
        return withContext(Dispatchers.IO) {
             try {
                // Query all chains in parallel
                val chainResults = ChainConfigs.ALL_CHAINS.map { chain ->
                    async(Dispatchers.IO) {
                        try {
                            val web3j = Web3j.build(HttpService(chain.rpcUrl))
                            val tokens = mutableListOf<Triple<String, String, BigDecimal>>()

                            // Native balance
                            val nativeBalance = try {
                                val result = web3j.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST).send()
                                Convert.fromWei(result.balance.toString(), Convert.Unit.ETHER)
                            } catch (e: Exception) {
                                BigDecimal.ZERO
                            }
                            tokens.add(Triple(chain.symbol, chain.coingeckoNativeId, nativeBalance))

                            // ERC-20 balances
                            for (token in chain.tokens) {
                                val balance = try {
                                    getErc20Balance(web3j, walletAddress, token.contractAddress, token.decimals)
                                } catch (e: Exception) {
                                    BigDecimal.ZERO
                                }
                                tokens.add(Triple(token.symbol, token.coingeckoId, balance))
                            }

                            Pair(chain, tokens.toList())
                        } catch (e: Exception) {
                            Pair(chain, emptyList<Triple<String, String, BigDecimal>>())
                        }
                    }
                }.awaitAll()

                // Collect all coingecko IDs for price fetch
                val allCoingeckoIds = mutableSetOf<String>()
                for (result in chainResults) {
                    for (tokenData in result.second) {
                        allCoingeckoIds.add(tokenData.second)
                    }
                }

                val pricesResult = coinRepository.getPrices(allCoingeckoIds.joinToString(","))
                val prices = pricesResult.data ?: emptyMap()

                // Build portfolio tokens
                val aggregated = mutableMapOf<String, PortfolioToken>()

                for (result in chainResults) {
                    val chain = result.first
                    val tokens = result.second
                    
                    if (tokens.isEmpty()) continue

                    // Native token
                    val nativeData = tokens.firstOrNull()
                    if (nativeData != null) {
                         val nativeCoingeckoId = nativeData.second
                        val nativePrice = prices[nativeCoingeckoId]?.get("usd") ?: 0.0
                        val nativeBalanceDouble = nativeData.third.toDouble()
                        val nativeValue = nativeBalanceDouble * nativePrice
                        val nativeKey = "${chain.symbol}-native-${chain.name}"
                        aggregated[nativeKey] = PortfolioToken(
                            symbol = chain.symbol,
                            name = "${chain.name}",
                            contractAddress = null,
                            decimals = 18,
                            balance = nativeBalanceDouble,
                            priceUsd = nativePrice,
                            valueUsd = nativeValue,
                            chainName = chain.name
                        )
                    }
                   

                    // ERC-20 tokens
                    val erc20Tokens = tokens.drop(1)
                    val chainTokenInfos = chain.tokens
                    for (index in erc20Tokens.indices) {
                        if (index >= chainTokenInfos.size) break
                        val tokenData = erc20Tokens[index]
                        val tokenInfo = chainTokenInfos[index]
                        val price = prices[tokenData.second]?.get("usd") ?: 0.0
                        val balanceDouble = tokenData.third.toDouble()
                        val value = balanceDouble * price
                        val key = "${tokenInfo.symbol}-${chain.name}"
                        aggregated[key] = PortfolioToken(
                            symbol = tokenInfo.symbol,
                            name = tokenInfo.name,
                            contractAddress = tokenInfo.contractAddress,
                            decimals = tokenInfo.decimals,
                            balance = balanceDouble,
                            priceUsd = price,
                            valueUsd = value,
                            chainName = chain.name
                        )
                    }
                }
                
                val sorted = aggregated.values.sortedByDescending { it.valueUsd }
                Resource.Success(sorted)

            } catch (e: Exception) {
                 Resource.Error(e.message ?: "Error loading portfolio")
            }
        }
    }

    private fun getErc20Balance(web3j: Web3j, walletAddress: String, contractAddress: String, decimals: Int): BigDecimal {
        val function = Function(
            "balanceOf",
            listOf(Address(walletAddress)),
            listOf(object : TypeReference<Uint256>() {})
        )
        val encodedFunction = FunctionEncoder.encode(function)
        val transaction = Transaction.createEthCallTransaction(
            walletAddress,
            contractAddress,
            encodedFunction
        )

        val response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send()

        if (response.hasError()) {
            return BigDecimal.ZERO
        }

        val decoded = FunctionReturnDecoder.decode(response.value, function.outputParameters)
        if (decoded.isEmpty()) {
            return BigDecimal.ZERO
        }

        val rawBalance = decoded[0].value as BigInteger
        return BigDecimal(rawBalance).divide(BigDecimal.TEN.pow(decimals))
    }
}
