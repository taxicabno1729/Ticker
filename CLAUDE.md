# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK (~15s after daemon starts)
./gradlew assembleRelease        # Build release APK (minified + shrunk)
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)
./gradlew lint                   # Run lint checks
```

## Configuration

RPC URLs and API keys are defined in `gradle.properties` as `buildConfigField` entries and accessed via `BuildConfig.RPC_ETHEREUM`, `BuildConfig.RPC_POLYGON`, etc. The five supported chains are Ethereum (1), Polygon (137), Arbitrum (42161), Optimism (10), and Base (8453).

`INFURA_PROJECT_ID` and `REOWN_PROJECT_ID` are also in `gradle.properties`. Override them in `local.properties` (git-ignored) for local development.

## Architecture

**MVVM + Repository pattern** with Jetpack Navigation (single-activity, fragment-based). No Compose — all UI is XML layouts with View Binding.

### Data flow
```
Fragment → ViewModel → Repository → (Retrofit/Web3j/CoinGecko)
                ↓
         StateFlow<Resource<T>>
                ↓
Fragment collects via lifecycleScope.launch { flow.collectLatest {} }
```

`Resource<T>` is a sealed class (`Success`, `Error`, `Loading`) that wraps all async results. ViewModels always emit `Resource.Loading()` before starting work.

### Key layers

**`data/`**
- `CoinRepository` — CoinGecko API: markets list, batch prices, coin detail, market chart (for Greeks)
- `WalletRepository` — Web3j: native balance via `ethGetBalance`, ERC-20 via `ethCall` + `balanceOf`. Multi-chain queries run in parallel with `async/awaitAll`. Native tokens have `contractAddress = null`; ERC-20s have an address.
- `ChainConfig` / `ChainConfigs` — per-chain RPC URL, native symbol, coingeckoNativeId, and ERC-20 token list. `ChainConfigs.ALL_CHAINS` is the canonical list used everywhere.
- `GreeksCalculator` — computes Delta (mean return), Gamma (std dev), Theta (annualised return), Vega (volatility), Rho (Sharpe-like ratio) from a price series. Portfolio-level Greeks are value-weighted.

**`network/`**
- `RetrofitClient` — singleton Retrofit instances for CoinGecko, Kalshi, and Polymarket.

**`ui/`**
- One ViewModel + ViewModelFactory pair per screen. Factories handle constructor injection.
- `PortfolioAdapter` uses a sealed `PortfolioListItem` (Header / Token) to render collapsible chain sections.

### Screens (Fragments)
| Fragment | Purpose |
|---|---|
| `FirstFragment` | Markets list + search + wallet connect entry point |
| `SecondFragment` | Portfolio: multi-chain holdings grouped by chain, collapsible, ETH-equivalent balances |
| `CoinDetailFragment` | Coin detail with Greek indicators |
| `PredictionMarketFragment` | Polymarket + Kalshi prediction markets |
| `MarketBrowserFragment` | Market browser |
| `KalshiLoginFragment` | Kalshi auth (tokens stored via AndroidX Security Crypto) |

Navigation is defined in `res/navigation/nav_graph.xml`. `MainActivity` owns the `NavController` and handles WalletConnect deep links (`liveticker://request`).

### Wallet connection
`TickerApplication` initialises Reown AppKit (WalletConnect v2) with the five EVM chains. The connected wallet address is passed as a navigation argument (`wallet_address`) to `SecondFragment`.

## Conventions

- Dark theme only: `background_dark` (#121212), `surface_dark` (#1E1E1E), `card_dark` (#2A2A2A). Positive values use `accent_green`, negative use `accent_red`.
- All coroutine work goes through `Dispatchers.IO` inside repositories; ViewModels use `viewModelScope`.
- `PortfolioToken.contractAddress == null` identifies a native chain token (ETH, POL). ERC-20s always have a non-null address.
- ETH-equivalent portfolio value: `totalUsdValue / ethPrice` where `ethPrice` comes from `tokens.find { it.symbol == "ETH" && it.contractAddress == null }?.priceUsd`.
