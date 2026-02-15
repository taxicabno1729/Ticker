package com.example.liveticker.data

import com.example.liveticker.BuildConfig

data class ChainConfig(
    val chainId: Int,
    val name: String,
    val symbol: String,
    val rpcUrl: String,
    val coingeckoNativeId: String,
    val tokens: List<Erc20Tokens.TokenInfo>
)

object ChainConfigs {
    val ETHEREUM = ChainConfig(
        chainId = 1,
        name = "Ethereum",
        symbol = "ETH",
        rpcUrl = BuildConfig.RPC_ETHEREUM,
        coingeckoNativeId = "ethereum",
        tokens = Erc20Tokens.POPULAR_TOKENS
    )

    val POLYGON = ChainConfig(
        chainId = 137,
        name = "Polygon",
        symbol = "POL",
        rpcUrl = BuildConfig.RPC_POLYGON,
        coingeckoNativeId = "matic-network",
        tokens = listOf(
            Erc20Tokens.TokenInfo("USDT", "Tether", "0xc2132D05D31c914a87C6611C10748AEb04B58e8F", 6, "tether"),
            Erc20Tokens.TokenInfo("USDC", "USD Coin", "0x3c499c542cEF5E3811e1192ce70d8cC03d5c3359", 6, "usd-coin"),
            Erc20Tokens.TokenInfo("DAI", "Dai", "0x8f3Cf7ad23Cd3CaDbD9735AFf958023239c6A063", 18, "dai"),
            Erc20Tokens.TokenInfo("WETH", "Wrapped Ether", "0x7ceB23fD6bC0adD59E62ac25578270cFf1b9f619", 18, "weth"),
            Erc20Tokens.TokenInfo("AAVE", "Aave", "0xD6DF932A45C0f255f85145f286eA0b292B21C90B", 18, "aave"),
            Erc20Tokens.TokenInfo("LINK", "Chainlink", "0x53E0bca35eC356BD5ddDFebbD1Fc0fD03FaBad39", 18, "chainlink")
        )
    )

    val ARBITRUM = ChainConfig(
        chainId = 42161,
        name = "Arbitrum",
        symbol = "ETH",
        rpcUrl = BuildConfig.RPC_ARBITRUM,
        coingeckoNativeId = "ethereum",
        tokens = listOf(
            Erc20Tokens.TokenInfo("USDT", "Tether", "0xFd086bC7CD5C481DCC9C85ebE478A1C0b69FCbb9", 6, "tether"),
            Erc20Tokens.TokenInfo("USDC", "USD Coin", "0xaf88d065e77c8cC2239327C5EDb3A432268e5831", 6, "usd-coin"),
            Erc20Tokens.TokenInfo("DAI", "Dai", "0xDA10009cBd5D07dd0CeCc66161FC93D7c9000da1", 18, "dai"),
            Erc20Tokens.TokenInfo("ARB", "Arbitrum", "0x912CE59144191C1204E64559FE8253a0e49E6548", 18, "arbitrum"),
            Erc20Tokens.TokenInfo("LINK", "Chainlink", "0xf97f4df75117a78c1A5a0DBb814Af92458539FB4", 18, "chainlink")
        )
    )

    val OPTIMISM = ChainConfig(
        chainId = 10,
        name = "Optimism",
        symbol = "ETH",
        rpcUrl = BuildConfig.RPC_OPTIMISM,
        coingeckoNativeId = "ethereum",
        tokens = listOf(
            Erc20Tokens.TokenInfo("USDT", "Tether", "0x94b008aA00579c1307B0EF2c499aD98a8ce58e58", 6, "tether"),
            Erc20Tokens.TokenInfo("USDC", "USD Coin", "0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85", 6, "usd-coin"),
            Erc20Tokens.TokenInfo("DAI", "Dai", "0xDA10009cBd5D07dd0CeCc66161FC93D7c9000da1", 18, "dai"),
            Erc20Tokens.TokenInfo("OP", "Optimism", "0x4200000000000000000000000000000000000042", 18, "optimism"),
            Erc20Tokens.TokenInfo("LINK", "Chainlink", "0x350a791Bfc2C21F9Ed5d10980Dad2e2638ffa7f6", 18, "chainlink")
        )
    )

    val BASE = ChainConfig(
        chainId = 8453,
        name = "Base",
        symbol = "ETH",
        rpcUrl = BuildConfig.RPC_BASE,
        coingeckoNativeId = "ethereum",
        tokens = listOf(
            Erc20Tokens.TokenInfo("USDC", "USD Coin", "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913", 6, "usd-coin"),
            Erc20Tokens.TokenInfo("DAI", "Dai", "0x50c5725949A6F0c72E6C4a641F24049A917DB0Cb", 18, "dai")
        )
    )

    val ALL_CHAINS = listOf(ETHEREUM, POLYGON, ARBITRUM, OPTIMISM, BASE)
}
