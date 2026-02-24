# Live Ticker - AI Agent Documentation

## Project Overview

**Live Ticker** is an Android cryptocurrency portfolio tracker application with integrated prediction market support. The app allows users to track live crypto prices, connect Web3 wallets to view multi-chain portfolios, analyze portfolio risk metrics using "Crypto Greeks" calculations, and browse prediction market positions.

- **Package**: `com.example.liveticker`
- **Language**: Kotlin
- **Architecture**: MVVM with Repository pattern
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 16)
- **Compile SDK**: 36

## Technology Stack

### Core Framework
- **Android SDK**: Native Android app using AndroidX libraries
- **Kotlin**: Primary programming language with Coroutines and Flow
- **Java Compatibility**: VERSION_11 (source and target)

### Architecture Components
- **Navigation Component**: Single Activity with multiple Fragments
- **ViewModel**: Lifecycle-aware UI data management
- **View Binding**: Type-safe view access (enabled in build features)
- **Repository Pattern**: Data layer abstraction for API and blockchain calls

### Networking & APIs
- **Retrofit 2.9.0**: REST API client for CoinGecko, Polymarket, Kalshi
- **Gson**: JSON serialization/deserialization
- **OkHttp 4.9.3**: HTTP client
- **Web3j 4.8.7-android**: Ethereum blockchain interaction

### Web3 & Wallet Integration
- **Reown AppKit 1.6.2** (formerly WalletConnect): Wallet connection via `com.reown:android-bom:1.6.2`
- **Supported Chains**: Ethereum (1), Polygon (137), Arbitrum (42161), Optimism (10), Base (8453)
- **Deep Linking**: `liveticker://request` scheme for wallet callbacks

### UI Components
- **Material Design Components**: Google's Material Design 1.10.0
- **RecyclerView**: Lists with DiffUtil for efficient updates
- **SwipeRefreshLayout**: Pull-to-refresh for portfolio
- **CardView**: Material card layouts
- **Security Crypto**: Encrypted SharedPreferences for sensitive data

## Project Structure

```
app/src/main/java/com/example/liveticker/
├── MainActivity.kt              # Main activity with navigation setup
├── TickerApplication.kt         # Application class, initializes Reown/AppKit
├── FirstFragment.kt             # Main screen: ticker list + wallet connect
├── SecondFragment.kt            # Portfolio view with Greeks analysis
├── CoinDetailFragment.kt        # Detailed coin info with charts
├── PredictionMarketFragment.kt  # Prediction market positions
├── MarketBrowserFragment.kt     # Browse Polymarket/Kalshi markets
├── KalshiLoginFragment.kt       # Kalshi authentication screen
├── data/                        # Data layer
│   ├── Ticker.kt               # Data models
│   ├── CoinDetail.kt
│   ├── PortfolioToken.kt
│   ├── PredictionMarketPosition.kt
│   ├── Resource.kt             # Sealed class for API states
│   ├── CoinRepository.kt       # CoinGecko API repository
│   ├── WalletRepository.kt     # Blockchain balance fetching
│   ├── PredictionMarketRepository.kt  # Polymarket/Kalshi integration
│   ├── ChainConfig.kt          # Multi-chain configuration
│   ├── CryptoGreeks.kt         # Greeks data model
│   ├── GreeksCalculator.kt     # Risk metrics calculation
│   └── ...
├── network/                     # API layer
│   ├── CoinGeckoApiService.kt
│   ├── PolymarketApiService.kt
│   ├── KalshiApiService.kt
│   └── RetrofitClient.kt       # Retrofit singletons
└── ui/                          # UI layer (ViewModels + Adapters)
    ├── TickerViewModel.kt
    ├── PortfolioViewModel.kt
    ├── CoinDetailViewModel.kt
    ├── PredictionMarketViewModel.kt
    ├── TickerAdapter.kt
    ├── PortfolioAdapter.kt
    └── ...

app/src/main/res/
├── layout/                     # XML layout files
├── navigation/                 # Navigation graph
├── values/                     # Strings, colors, themes
└── ...
```

## Build Configuration

### Key Configuration Files

**settings.gradle.kts**: Project settings with repositories (Google, Maven Central, JitPack)

**gradle/libs.versions.toml**: Version catalog
- AGP: 9.0.0
- Core KTX: 1.10.1
- Navigation: 2.6.0
- Material: 1.10.0

**app/build.gradle.kts**: Module configuration
- ViewBinding enabled
- BuildConfig enabled
- ProGuard enabled for release builds
- BuildConfig fields for API keys and RPC URLs

### API Keys Configuration

API keys are configured in `gradle.properties` (fallback values) and should be overridden in `local.properties` (not in VCS):

```properties
# In gradle.properties (default/fallback values)
INFURA_PROJECT_ID=8b0e8650ee9d4f378cb9f7b167847819
REOWN_PROJECT_ID=439a61535bc235844dbbaebe60969b35
```

**Do not commit `local.properties` with real keys to version control.**

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Install debug build on connected device
./gradlew installDebug
```

## Code Style Guidelines

### Kotlin Conventions
- Follow Kotlin official code style (`kotlin.code.style=official` in gradle.properties)
- Use Kotlin Coroutines for asynchronous operations
- Prefer `val` over `var`, immutable collections where possible
- Use data classes for models
- Use sealed classes for state representation (e.g., `Resource<T>`)

### Android Conventions
- Fragments use View Binding with nullable backing property pattern:
  ```kotlin
  private var _binding: FragmentFirstBinding? = null
  private val binding get() = _binding!!
  ```
- ViewModels use `viewModelScope` for coroutines
- Repository methods return `Resource<T>` for consistent error handling
- UI state exposed as `StateFlow` from ViewModels

### Naming Conventions
- Packages: lowercase, no underscores (`com.example.liveticker`)
- Classes: PascalCase (`CoinRepository`, `FirstFragment`)
- Functions/Variables: camelCase (`fetchTickers`, `connectedAddress`)
- Constants: UPPER_SNAKE_CASE (in companion objects)
- XML resources: snake_case (`fragment_first.xml`, `ticker_item.xml`)

## Testing Strategy

### Current Test Setup
- **Unit Tests**: JUnit 4 (`app/src/test/`)
- **Instrumented Tests**: AndroidJUnit4 with Espresso (`app/src/androidTest/`)

### Test Files
- `ExampleUnitTest.kt`: Basic unit test example
- `ExampleInstrumentedTest.kt`: Basic instrumented test example

### Running Tests
```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (requires device/emulator)
./gradlew connectedDebugAndroidTest
```

### Testing Recommendations
- Repository classes should be tested with mocked API responses
- ViewModels should be tested with `InstantTaskExecutorRule`
- UI tests should cover critical user flows (wallet connect, portfolio load)

## Key Features & Implementation Details

### 1. Live Crypto Ticker (FirstFragment)
- Auto-refresh every 10 seconds using Handler/Runnable
- Search functionality filters by name or symbol
- Clicking item navigates to CoinDetailFragment

### 2. Wallet Integration
- Uses Reown AppKit for wallet connections
- Supports WalletConnect-compatible wallets
- Deep link handling in MainActivity for connection callbacks
- Stores connected address for portfolio queries

### 3. Multi-Chain Portfolio (SecondFragment)
- Queries 5 chains in parallel using `async/awaitAll`
- Fetches native balance + ERC-20 token balances
- Groups tokens by chain with expandable sections
- Calculates portfolio-wide Greeks metrics

### 4. Crypto Greeks Calculation
- **Delta**: Average daily return
- **Gamma**: Standard deviation of returns (volatility)
- **Theta**: Annualized return over the period
- **Vega**: Annualized volatility
- **Rho**: Risk-adjusted return (Sharpe-like ratio)

### 5. Prediction Markets
- Integrates with Polymarket (Polygon-based) and Kalshi
- Currently uses fallback mock data (APIs require authentication)
- Calculates prediction market metrics (prob delta, time theta, etc.)

## Security Considerations

### API Keys
- Never commit real API keys to version control
- Use `local.properties` for local development
- Use environment variables or CI secrets for builds
- Fallback keys in `gradle.properties` are for development only

### Wallet Security
- Wallet connection handled by Reown/AppKit (industry standard)
- No private keys stored in app
- Uses deep link scheme `liveticker://` for callbacks

### Network Security
- HTTPS for all API calls
- RPC endpoints via Infura (authenticated)
- Certificate pinning not currently implemented (consider for production)

## Dependencies to Know

### Critical Dependencies
```kotlin
// Web3 & Wallet
implementation("org.web3j:core:4.8.7-android")
implementation(platform("com.reown:android-bom:1.6.2"))
implementation("com.reown:android-core")
implementation("com.reown:appkit")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.9.3")

// Android UI
implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

## Common Development Tasks

### Adding a New Chain
1. Add RPC URL to `build.gradle.kts` buildConfigField
2. Add ChainConfig in `ChainConfigs.kt`
3. Update `TickerApplication.kt` if wallet connection needs the chain

### Adding a New API Endpoint
1. Define data models in `data/` package
2. Add endpoint to appropriate `*ApiService.kt` interface
3. Create/update repository class
4. Create ViewModel and expose StateFlow
5. Observe in Fragment with `collectLatest`

### Adding a New Fragment
1. Create Fragment class with View Binding
2. Create layout XML in `res/layout/`
3. Add to navigation graph in `res/navigation/nav_graph.xml`
4. Add string resources to `res/values/strings.xml`
5. Add menu item if needed in `res/menu/menu_main.xml`

## Troubleshooting

### Build Issues
- **Gradle sync fails**: Check `local.properties` has valid `sdk.dir`
- **Missing API keys**: Ensure `INFURA_PROJECT_ID` and `REOWN_PROJECT_ID` are set
- **Out of memory**: Increase heap size in `gradle.properties` (already set to 2048m)

### Runtime Issues
- **Wallet won't connect**: Check REOWN_PROJECT_ID is valid
- **Portfolio won't load**: Verify Infura key and network connectivity
- **CoinGecko rate limiting**: Free tier has rate limits, may need retries

### IDE Setup
- Android Studio Ladybug or newer recommended
- Kotlin plugin should be up to date
- Gradle wrapper included (`./gradlew`)

## External APIs

### CoinGecko (Free Tier)
- Base URL: `https://api.coingecko.com/api/v3/`
- Endpoints: `/coins/markets`, `/simple/price`, `/coins/{id}`, `/coins/{id}/market_chart`
- Rate limits apply (10-30 calls/minute on free tier)

### Infura (Web3 RPC)
- Used for Ethereum, Polygon, Arbitrum, Optimism, Base
- Requires project ID
- Free tier: 100,000 requests/day

### Polymarket
- Gamma API for markets
- CLOB API for positions (requires auth)
- Currently using mock data fallback

### Kalshi
- REST API for markets and positions
- Requires OAuth authentication for positions
- Currently using mock data fallback

---

*This documentation is intended for AI coding agents. For human contributors, see README.md (if available).*
