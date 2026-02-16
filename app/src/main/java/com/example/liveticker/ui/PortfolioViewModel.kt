package com.example.liveticker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.liveticker.data.CoinRepository
import com.example.liveticker.data.GreeksCalculator
import com.example.liveticker.data.PortfolioGreeksState
import com.example.liveticker.data.PortfolioToken
import com.example.liveticker.data.Resource
import com.example.liveticker.data.TokenGreeks
import com.example.liveticker.data.WalletRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class PortfolioViewModel(
    private val walletRepository: WalletRepository,
    private val coinRepository: CoinRepository
) : ViewModel() {

    private val _portfolio = MutableStateFlow<Resource<List<PortfolioToken>>>(Resource.Loading())
    val portfolio: StateFlow<Resource<List<PortfolioToken>>> = _portfolio

    private val _nativeBalance = MutableStateFlow<Resource<BigDecimal>>(Resource.Loading())
    val nativeBalance: StateFlow<Resource<BigDecimal>> = _nativeBalance

    private val _portfolioGreeks = MutableStateFlow<Resource<PortfolioGreeksState>>(Resource.Loading())
    val portfolioGreeks: StateFlow<Resource<PortfolioGreeksState>> = _portfolioGreeks

    fun loadPortfolio(address: String) {
        viewModelScope.launch {
            _portfolio.value = Resource.Loading()
            _portfolio.value = walletRepository.getPortfolio(address, coinRepository)
        }
    }
    
    fun loadNativeBalance(address: String) {
        viewModelScope.launch {
            _nativeBalance.value = Resource.Loading()
            _nativeBalance.value = walletRepository.getNativeBalance(address)
        }
    }

    fun loadPortfolioGreeks(tokens: List<PortfolioToken>, days: Int = 30) {
        viewModelScope.launch {
            _portfolioGreeks.value = Resource.Loading()
            try {
                val valuableTokens = tokens.filter { it.valueUsd > 0 && it.coingeckoId.isNotEmpty() }
                if (valuableTokens.isEmpty()) {
                    _portfolioGreeks.value = Resource.Error("No tokens with value for Greeks calculation")
                    return@launch
                }

                val totalValue = valuableTokens.sumOf { it.valueUsd }

                // Deduplicate by coingeckoId, aggregate value
                val idToValue = mutableMapOf<String, Double>()
                val idToInfo = mutableMapOf<String, Pair<String, String>>() // id -> (symbol, name)
                for (token in valuableTokens) {
                    idToValue[token.coingeckoId] = (idToValue[token.coingeckoId] ?: 0.0) + token.valueUsd
                    if (!idToInfo.containsKey(token.coingeckoId)) {
                        idToInfo[token.coingeckoId] = Pair(token.symbol, token.name)
                    }
                }

                // Fetch charts in parallel
                val chartResults = idToValue.keys.map { coinId ->
                    async { Pair(coinId, coinRepository.getMarketChart(coinId, days)) }
                }.awaitAll()

                val tokenGreeksList = mutableListOf<TokenGreeks>()
                val weightedItems = mutableListOf<Pair<Double, com.example.liveticker.data.CryptoGreeks>>()

                for ((coinId, chartResult) in chartResults) {
                    val prices = chartResult.data?.prices ?: continue
                    val greeks = GreeksCalculator.calculate(prices) ?: continue
                    val value = idToValue[coinId] ?: continue
                    val weight = value / totalValue
                    val info = idToInfo[coinId] ?: continue

                    tokenGreeksList.add(
                        TokenGreeks(
                            coingeckoId = coinId,
                            symbol = info.first,
                            name = info.second,
                            weight = weight,
                            greeks = greeks
                        )
                    )
                    weightedItems.add(Pair(value, greeks))
                }

                if (tokenGreeksList.isEmpty()) {
                    _portfolioGreeks.value = Resource.Error("Could not compute Greeks for any token")
                    return@launch
                }

                val portfolioGreeks = GreeksCalculator.calculateWeighted(weightedItems)
                _portfolioGreeks.value = Resource.Success(
                    PortfolioGreeksState(
                        portfolioGreeks = portfolioGreeks,
                        tokenGreeks = tokenGreeksList.sortedByDescending { it.weight },
                        days = days
                    )
                )
            } catch (e: Exception) {
                _portfolioGreeks.value = Resource.Error(e.message ?: "Error computing portfolio Greeks")
            }
        }
    }
}

class PortfolioViewModelFactory(
    private val walletRepository: WalletRepository,
    private val coinRepository: CoinRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PortfolioViewModel(walletRepository, coinRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
