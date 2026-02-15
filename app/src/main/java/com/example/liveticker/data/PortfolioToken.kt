package com.example.liveticker.data

data class PortfolioToken(
    val symbol: String,
    val name: String,
    val contractAddress: String?,
    val decimals: Int,
    val balance: Double,
    val priceUsd: Double,
    val valueUsd: Double,
    val chainName: String = "Ethereum"
)

object Erc20Tokens {
    data class TokenInfo(
        val symbol: String,
        val name: String,
        val contractAddress: String,
        val decimals: Int,
        val coingeckoId: String
    )

    val POPULAR_TOKENS = listOf(
        TokenInfo("USDT", "Tether", "0xdAC17F958D2ee523a2206206994597C13D831ec7", 6, "tether"),
        TokenInfo("USDC", "USD Coin", "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48", 6, "usd-coin"),
        TokenInfo("DAI", "Dai", "0x6B175474E89094C44Da98b954EedeAC495271d0F", 18, "dai"),
        TokenInfo("WETH", "Wrapped Ether", "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2", 18, "weth"),
        TokenInfo("UNI", "Uniswap", "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984", 18, "uniswap"),
        TokenInfo("LINK", "Chainlink", "0x514910771AF9Ca656af840dff83E8264EcF986CA", 18, "chainlink"),
        TokenInfo("AAVE", "Aave", "0x7Fc66500c84A76Ad7e9c93437bFc5Ac33E2DDaE9", 18, "aave"),
        TokenInfo("SHIB", "Shiba Inu", "0x95aD61b0a150d79219dCF64E1E6Cc01f0B64C4cE", 18, "shiba-inu"),
        TokenInfo("MATIC", "Polygon", "0x7D1AfA7B718fb893dB30A3aBc0Cfc608AaCfeBB0", 18, "matic-network"),
        TokenInfo("LDO", "Lido DAO", "0x5A98FcBEA516Cf06857215779Fd812CA3beF1B32", 18, "lido-dao")
    )
}
