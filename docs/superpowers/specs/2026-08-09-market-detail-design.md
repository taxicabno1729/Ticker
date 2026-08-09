# Market Detail Screen — Design

**Date:** 2026-08-09
**Status:** Approved (pending spec review)

## Goal

Tapping a prediction market card in `MarketBrowserFragment` currently opens the
market's web page in an external browser. Replace that with an in-app native
detail screen showing the market's stats and a probability history chart.

## Decisions

| Question | Decision |
|---|---|
| Render style | Native detail screen (new fragment, MVVM), not WebView or bottom sheet |
| Content | Header + stats + probability history chart + "Open on Polymarket/Kalshi" button |
| Chart | Custom Canvas `View` (~100 lines), no chart library dependency |
| History data | Real for Polymarket via public CLOB API; deterministic synthetic series for Kalshi and mock markets |

## Architecture

Follows the app's MVVM + Repository pattern and the `android-app` skill's
7-step new-screen workflow.

### Navigation

- Enable the `kotlin-parcelize` Gradle plugin (app module).
- Annotate `PolymarketMarketDisplay` and `KalshiMarketDisplay` with `@Parcelize`.
- New nav destination `MarketDetailFragment` in `nav_graph.xml` with two
  nullable Parcelable arguments — `polymarket_market` and `kalshi_market` —
  exactly one non-null per navigation. This avoids inventing a shared
  supertype for the two display models; the non-null argument also tells the
  fragment which source it is showing.
- `MarketBrowserFragment.onMarketClicked` navigates to the detail screen
  (replaces the `ACTION_VIEW` intent added in c54307a). The external link moves
  to a button on the detail screen using the existing `webUrl` property.

### Data layer

- `ProbabilityPoint(timestampMs: Long, probability: Double)` — new model in `data/`.
- `PredictionMarketRepository`:
  - `suspend fun getProbabilityHistory(market: PolymarketMarketDisplay): Resource<List<ProbabilityPoint>>`
    — extends the Gamma `PolymarketMarket` DTO with `clobTokenIds` (JSON string
    array field); fetches `https://clob.polymarket.com/prices-history?market={tokenId}&interval=1w&fidelity=60`
    via a new `PolymarketClobApiService` (Retrofit singleton in
    `RetrofitClient`). Any failure (no token id, HTTP error, empty series)
    falls back to the synthetic series — same philosophy as the repo's
    existing mock fallbacks. Returns `Resource.Success` either way.
  - `suspend fun getProbabilityHistory(market: KalshiMarketDisplay)` — synthetic
    series only (Kalshi candlesticks require auth; Kalshi data is fully mock today).
  - `generateSyntheticHistory(seed: String, endProbability: Double, points: Int = 30): List<ProbabilityPoint>`
    — deterministic random walk seeded from the market id/ticker, clamped to
    [0.02, 0.98], ending exactly at the market's current probability, spanning
    the last 30 days. Internal but unit-tested.

### UI layer

- `MarketDetailViewModel` + `MarketDetailViewModelFactory`: exposes
  `StateFlow<Resource<List<ProbabilityPoint>>>`, emits `Resource.Loading()`
  first, fetches on init via `viewModelScope`.
- `MarketDetailFragment` + `fragment_market_detail.xml`, View Binding, M3 cards
  matching Coin Detail's visual language:
  1. **Header card** — question, source chip (Polymarket/Kalshi), category chip
     (`primaryContainer`/`onPrimaryContainer`), large current probability.
  2. **Stats card** — Yes price (= probability) and No price (= 1 − probability)
     formatted in cents, 24h volume, liquidity, resolution/close date.
  3. **Chart card** — "Probability (30d)" title + `ProbabilityChartView`;
     shows a small loading spinner while `Resource.Loading`.
  4. **Open on Polymarket/Kalshi** — `MaterialButton` (text style) firing
     `ACTION_VIEW` with `market.webUrl`; `ActivityNotFoundException` → Toast.
- `ProbabilityChartView` — custom `View`; API: `setPoints(List<ProbabilityPoint>)`.
  Draws: gridlines at 0/50/100% with labels, probability polyline, subtle
  vertical gradient fill under the line. Line color `accent_green` if the
  series ends at or above its start, else `accent_red`. Respects padding;
  `invalidate()` on data change. No touch handling.

## Error handling

- Repository never surfaces `Resource.Error` for history (falls back to
  synthetic data), so the chart always renders. Real-vs-synthetic is not
  labeled in the UI for v1.
- Malformed CLOB responses are caught with the same fallback.
- Navigation args are non-optional at the call site; the fragment throws if
  both market args are null (programmer error, not a runtime state).

## Testing

- Unit tests (`app/src/test`):
  - Synthetic series: determinism (same seed → same series), length, clamping
    to [0.02, 0.98], final point equals current probability.
  - CLOB response mapping: JSON → `ProbabilityPoint` list, and fallback on
    empty/absent history.
  - Existing `MarketUrlTest` still passes (webUrl unchanged).
- Manual verification on emulator: navigate from both tabs (Polymarket mock +
  Kalshi mock), check chart renders, stats correct, external-link button works,
  back navigation intact.

## Out of scope

- Kalshi authenticated candlestick history.
- Chart touch interactions (scrubbing, tooltips).
- Order book, outcomes beyond Yes/No, position taking.
- Caching history beyond the ViewModel's lifetime.
