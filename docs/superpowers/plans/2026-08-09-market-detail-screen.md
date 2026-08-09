# Market Detail Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Tapping a prediction market card opens a native in-app detail screen with market stats and a 30-day probability chart instead of launching the browser.

**Architecture:** MVVM + Repository, matching the app's existing pattern (Fragment → ViewModel → Repository → StateFlow<Resource<T>>). Market objects travel to the detail screen as manually-implemented Parcelables via two nullable nav arguments. History comes from Polymarket's public CLOB API when a token id exists, otherwise from a deterministic synthetic generator. The chart is a custom Canvas View — no chart library.

**Tech Stack:** Kotlin, XML layouts + View Binding, Jetpack Navigation, Retrofit + Gson, JUnit 4.

**Spec:** `docs/superpowers/specs/2026-08-09-market-detail-design.md`

## Global Constraints

- Do NOT add the kotlin-parcelize plugin — it does not work with AGP 9.0 built-in Kotlin (verified). Parcelable is implemented by hand.
- No new library dependencies at all.
- All repository I/O on `Dispatchers.IO`; ViewModels emit `Resource.Loading()` before work (app convention).
- Dark M3 theme: cards use `@color/surface_1`, text appearances `TextAppearance.LiveTicker.*`, positive/negative use `accent_green`/`accent_red`.
- Commit messages: `<type>: <summary ≤72 chars>` — types feat|fix|docs|style|refactor|test|chore (git hook enforces this). End every commit body with the Claude Code trailer used in this repo.
- Unit tests are plain JUnit (no Robolectric/Mockito/coroutines-test in this project) — only pure-JVM logic is unit-testable. `android.os.Parcel` is a stub in unit tests, so Parcelable correctness is verified on-device in Task 6, not by unit test.

---

### Task 1: ProbabilityPoint model + synthetic history generator

**Files:**
- Create: `app/src/main/java/com/example/liveticker/data/ProbabilityHistory.kt`
- Test: `app/src/test/java/com/example/liveticker/SyntheticHistoryGeneratorTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: `data class ProbabilityPoint(val timestampMs: Long, val probability: Double)`;
  `SyntheticHistoryGenerator.generate(seed: String, endProbability: Double, points: Int = 30, endTimeMs: Long = System.currentTimeMillis()): List<ProbabilityPoint>`.

- [ ] **Step 1: Write the failing test**

```kotlin
package com.example.liveticker

import com.example.liveticker.data.SyntheticHistoryGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyntheticHistoryGeneratorTest {

    @Test
    fun `same seed produces identical series`() {
        val a = SyntheticHistoryGenerator.generate("mock-1", 0.72, endTimeMs = 1_000_000_000_000L)
        val b = SyntheticHistoryGenerator.generate("mock-1", 0.72, endTimeMs = 1_000_000_000_000L)
        assertEquals(a, b)
    }

    @Test
    fun `different seeds produce different series`() {
        val a = SyntheticHistoryGenerator.generate("mock-1", 0.72, endTimeMs = 1_000_000_000_000L)
        val b = SyntheticHistoryGenerator.generate("mock-2", 0.72, endTimeMs = 1_000_000_000_000L)
        assertNotEquals(a, b)
    }

    @Test
    fun `series has requested length and ends at current probability`() {
        val series = SyntheticHistoryGenerator.generate("seed", 0.65, points = 30, endTimeMs = 1_000_000_000_000L)
        assertEquals(30, series.size)
        assertEquals(0.65, series.last().probability, 1e-9)
        assertEquals(1_000_000_000_000L, series.last().timestampMs)
    }

    @Test
    fun `values are clamped to 0_02 to 0_98`() {
        val series = SyntheticHistoryGenerator.generate("seed", 0.99, points = 60, endTimeMs = 1_000_000_000_000L)
        assertTrue(series.all { it.probability in 0.02..0.98 })
    }

    @Test
    fun `timestamps ascend one day apart`() {
        val series = SyntheticHistoryGenerator.generate("seed", 0.5, points = 3, endTimeMs = 200_000_000_000L)
        val dayMs = 24 * 60 * 60 * 1000L
        assertEquals(200_000_000_000L - 2 * dayMs, series[0].timestampMs)
        assertEquals(200_000_000_000L - dayMs, series[1].timestampMs)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "com.example.liveticker.SyntheticHistoryGeneratorTest"`
Expected: FAIL (unresolved reference `SyntheticHistoryGenerator`).

- [ ] **Step 3: Write the implementation**

```kotlin
package com.example.liveticker.data

import java.util.Random

data class ProbabilityPoint(
    val timestampMs: Long,
    val probability: Double
)

/**
 * Deterministic backward random walk used when no real price history is
 * available (Kalshi markets, Polymarket mock fallbacks, API failures).
 * Same seed + endProbability + endTimeMs always yields the same series.
 */
object SyntheticHistoryGenerator {

    private const val DAY_MS = 24 * 60 * 60 * 1000L
    private const val MIN_P = 0.02
    private const val MAX_P = 0.98
    private const val MAX_DAILY_STEP = 0.06

    fun generate(
        seed: String,
        endProbability: Double,
        points: Int = 30,
        endTimeMs: Long = System.currentTimeMillis()
    ): List<ProbabilityPoint> {
        val rng = Random(seed.hashCode().toLong())
        val values = DoubleArray(points)
        values[points - 1] = endProbability.coerceIn(MIN_P, MAX_P)
        for (i in points - 2 downTo 0) {
            val step = (rng.nextDouble() - 0.5) * MAX_DAILY_STEP
            values[i] = (values[i + 1] + step).coerceIn(MIN_P, MAX_P)
        }
        return values.mapIndexed { i, v ->
            ProbabilityPoint(endTimeMs - (points - 1 - i) * DAY_MS, v)
        }
    }
}
```

Note: the final point stores the clamped value, so the "ends at current probability" test uses 0.65 (inside bounds); the clamping test uses 0.99 and only asserts bounds.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "com.example.liveticker.SyntheticHistoryGeneratorTest"`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/liveticker/data/ProbabilityHistory.kt app/src/test/java/com/example/liveticker/SyntheticHistoryGeneratorTest.kt
git commit -m "feat: add probability point model and synthetic history generator"
```

---

### Task 2: Manual Parcelable on the two market display models

**Files:**
- Modify: `app/src/main/java/com/example/liveticker/data/PredictionMarketRepository.kt` (the two data classes near the bottom, currently around lines 328–352)

**Interfaces:**
- Consumes: existing `PolymarketMarketDisplay` / `KalshiMarketDisplay` data classes (both already have `webUrl`).
- Produces: both classes implement `android.os.Parcelable`; `PolymarketMarketDisplay` gains `val clobTokenId: String? = null` as its last constructor parameter (used by Task 3; mock fallbacks leave it null).

- [ ] **Step 1: Replace the two data classes**

Replace the existing `PolymarketMarketDisplay` and `KalshiMarketDisplay` declarations (keep their `webUrl` properties exactly as they are) with:

```kotlin
// Display models for market discovery.
// Parcelable is implemented manually: the kotlin-parcelize compiler plugin
// does not integrate with AGP 9 built-in Kotlin (see design spec).
data class PolymarketMarketDisplay(
    val id: String,
    val slug: String,
    val question: String,
    val probability: Double,
    val volume24h: Double,
    val liquidity: Double,
    val category: String,
    val resolutionDate: String,
    val clobTokenId: String? = null
) : Parcelable {
    val webUrl: String get() = "https://polymarket.com/event/$slug"

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(slug)
        dest.writeString(question)
        dest.writeDouble(probability)
        dest.writeDouble(volume24h)
        dest.writeDouble(liquidity)
        dest.writeString(category)
        dest.writeString(resolutionDate)
        dest.writeString(clobTokenId)
    }

    companion object CREATOR : Parcelable.Creator<PolymarketMarketDisplay> {
        override fun createFromParcel(source: Parcel) = PolymarketMarketDisplay(
            id = source.readString().orEmpty(),
            slug = source.readString().orEmpty(),
            question = source.readString().orEmpty(),
            probability = source.readDouble(),
            volume24h = source.readDouble(),
            liquidity = source.readDouble(),
            category = source.readString().orEmpty(),
            resolutionDate = source.readString().orEmpty(),
            clobTokenId = source.readString()
        )

        override fun newArray(size: Int): Array<PolymarketMarketDisplay?> = arrayOfNulls(size)
    }
}

data class KalshiMarketDisplay(
    val ticker: String,
    val title: String,
    val probability: Double,
    val volume24h: Double,
    val liquidity: Double,
    val category: String,
    val closeTime: String
) : Parcelable {
    val webUrl: String get() = "https://kalshi.com/markets/$ticker"

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(ticker)
        dest.writeString(title)
        dest.writeDouble(probability)
        dest.writeDouble(volume24h)
        dest.writeDouble(liquidity)
        dest.writeString(category)
        dest.writeString(closeTime)
    }

    companion object CREATOR : Parcelable.Creator<KalshiMarketDisplay> {
        override fun createFromParcel(source: Parcel) = KalshiMarketDisplay(
            ticker = source.readString().orEmpty(),
            title = source.readString().orEmpty(),
            probability = source.readDouble(),
            volume24h = source.readDouble(),
            liquidity = source.readDouble(),
            category = source.readString().orEmpty(),
            closeTime = source.readString().orEmpty()
        )

        override fun newArray(size: Int): Array<KalshiMarketDisplay?> = arrayOfNulls(size)
    }
}
```

Add these imports at the top of `PredictionMarketRepository.kt`:

```kotlin
import android.os.Parcel
import android.os.Parcelable
```

IMPORTANT: field order in `writeToParcel` and `createFromParcel` must match exactly — a mismatch silently corrupts every field after it.

- [ ] **Step 2: Verify it compiles and existing tests pass**

Run: `./gradlew testDebugUnitTest --tests "com.example.liveticker.MarketUrlTest" && ./gradlew assembleDebug -q`
Expected: MarketUrlTest PASS (webUrl unchanged), build succeeds.
(No unit test for the Parcel round-trip: `android.os.Parcel` is stubbed in local unit tests. Round-trip is exercised on-device in Task 6 Step 4.)

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/liveticker/data/PredictionMarketRepository.kt
git commit -m "feat: make market display models Parcelable for nav args"
```

---

### Task 3: CLOB history API + repository getProbabilityHistory

**Files:**
- Create: `app/src/main/java/com/example/liveticker/network/PolymarketClobApiService.kt`
- Modify: `app/src/main/java/com/example/liveticker/network/RetrofitClient.kt` (add `PolymarketClobClient` object)
- Modify: `app/src/main/java/com/example/liveticker/network/PolymarketApiService.kt` (add `clobTokenIds` to `PolymarketMarket`)
- Modify: `app/src/main/java/com/example/liveticker/data/PredictionMarketRepository.kt` (map token id, add history methods)
- Modify: `app/src/main/java/com/example/liveticker/data/ProbabilityHistory.kt` (add CLOB mapping + token-id parsing helpers)
- Test: `app/src/test/java/com/example/liveticker/ProbabilityHistoryMappingTest.kt`

**Interfaces:**
- Consumes: `ProbabilityPoint`, `SyntheticHistoryGenerator.generate(seed, endProbability)` (Task 1); `PolymarketMarketDisplay.clobTokenId` (Task 2); `Resource<T>` (existing).
- Produces:
  - `ClobPricePoint(t: Long, p: Double)`, `ClobPriceHistoryResponse(history: List<ClobPricePoint>?)`
  - `fun List<ClobPricePoint>.toProbabilityPoints(): List<ProbabilityPoint>` (seconds → ms)
  - `fun parseFirstClobTokenId(raw: String?): String?`
  - `PredictionMarketRepository.getProbabilityHistory(market: PolymarketMarketDisplay): Resource<List<ProbabilityPoint>>` (suspend)
  - `PredictionMarketRepository.getProbabilityHistory(market: KalshiMarketDisplay): Resource<List<ProbabilityPoint>>`

- [ ] **Step 1: Write the failing tests**

```kotlin
package com.example.liveticker

import com.example.liveticker.data.ClobPricePoint
import com.example.liveticker.data.parseFirstClobTokenId
import com.example.liveticker.data.toProbabilityPoints
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProbabilityHistoryMappingTest {

    @Test
    fun `clob points map seconds to millis preserving probability`() {
        val points = listOf(ClobPricePoint(t = 1_700_000_000L, p = 0.42)).toProbabilityPoints()
        assertEquals(1_700_000_000_000L, points[0].timestampMs)
        assertEquals(0.42, points[0].probability, 1e-9)
    }

    @Test
    fun `empty clob list maps to empty list`() {
        assertEquals(emptyList<Any>(), emptyList<ClobPricePoint>().toProbabilityPoints())
    }

    @Test
    fun `token id parses first entry of json string array`() {
        assertEquals("123abc", parseFirstClobTokenId("""["123abc","456def"]"""))
    }

    @Test
    fun `token id parse returns null for null blank or malformed input`() {
        assertNull(parseFirstClobTokenId(null))
        assertNull(parseFirstClobTokenId(""))
        assertNull(parseFirstClobTokenId("not-json"))
        assertNull(parseFirstClobTokenId("[]"))
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew testDebugUnitTest --tests "com.example.liveticker.ProbabilityHistoryMappingTest"`
Expected: FAIL (unresolved references).

- [ ] **Step 3: Add mapping helpers to ProbabilityHistory.kt**

Append to `app/src/main/java/com/example/liveticker/data/ProbabilityHistory.kt`:

```kotlin
// --- Polymarket CLOB price history ---

data class ClobPricePoint(
    val t: Long,   // unix seconds
    val p: Double  // price of the YES token == probability
)

data class ClobPriceHistoryResponse(
    val history: List<ClobPricePoint>?
)

fun List<ClobPricePoint>.toProbabilityPoints(): List<ProbabilityPoint> =
    map { ProbabilityPoint(timestampMs = it.t * 1000, probability = it.p) }

/**
 * Gamma returns clobTokenIds as a JSON-encoded string array
 * (e.g. "[\"123\",\"456\"]"). First entry is the YES outcome token.
 */
fun parseFirstClobTokenId(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        com.google.gson.Gson().fromJson(raw, Array<String>::class.java)?.firstOrNull()
    } catch (e: Exception) {
        null
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew testDebugUnitTest --tests "com.example.liveticker.ProbabilityHistoryMappingTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: Create the CLOB Retrofit service**

Create `app/src/main/java/com/example/liveticker/network/PolymarketClobApiService.kt`:

```kotlin
package com.example.liveticker.network

import com.example.liveticker.data.ClobPriceHistoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Polymarket CLOB API — public, no auth.
 * https://clob.polymarket.com/prices-history?market=<tokenId>&interval=1m&fidelity=1440
 */
interface PolymarketClobApiService {

    @GET("prices-history")
    suspend fun getPriceHistory(
        @Query("market") tokenId: String,
        @Query("interval") interval: String = "1m",
        @Query("fidelity") fidelityMinutes: Int = 1440
    ): ClobPriceHistoryResponse

    companion object {
        const val BASE_URL = "https://clob.polymarket.com/"
    }
}
```

(`interval=1m` is one month; `fidelity=1440` minutes gives daily points → ~30 points.)

- [ ] **Step 6: Register the client in RetrofitClient.kt**

Append to `app/src/main/java/com/example/liveticker/network/RetrofitClient.kt`:

```kotlin
object PolymarketClobClient {
    val api: PolymarketClobApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(PolymarketClobApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(PolymarketClobApiService::class.java)
    }
}
```

- [ ] **Step 7: Add clobTokenIds to the Gamma DTO and map it**

In `app/src/main/java/com/example/liveticker/network/PolymarketApiService.kt`, add to `PolymarketMarket` (after the `events` field):

```kotlin
    @SerializedName("clobTokenIds") val clobTokenIds: String?
```

(Remember the comma on the previous line.)

In `PredictionMarketRepository.getPolymarketMarkets()`, extend the mapping (currently builds `PolymarketMarketDisplay(...)` around line 90) with:

```kotlin
                    resolutionDate = market.resolutionDate ?: "TBD",
                    clobTokenId = parseFirstClobTokenId(market.clobTokenIds)
```

- [ ] **Step 8: Add the history methods to PredictionMarketRepository**

Add inside the repository class (alongside the other suspend functions). The class already imports `kotlinx.coroutines.delay`; add `kotlinx.coroutines.Dispatchers` and `kotlinx.coroutines.withContext` imports plus `com.example.liveticker.network.PolymarketClobClient` if not present:

```kotlin
    /**
     * 30-day probability history. Real CLOB data when a token id exists;
     * deterministic synthetic series otherwise. Never returns Resource.Error —
     * the chart always has something to draw (same philosophy as the
     * mock-data fallbacks above).
     */
    suspend fun getProbabilityHistory(market: PolymarketMarketDisplay): Resource<List<ProbabilityPoint>> =
        withContext(Dispatchers.IO) {
            val tokenId = market.clobTokenId
            if (tokenId.isNullOrBlank()) {
                return@withContext Resource.Success(syntheticHistory(market.id, market.probability))
            }
            try {
                val points = PolymarketClobClient.api.getPriceHistory(tokenId)
                    .history.orEmpty().toProbabilityPoints()
                if (points.size >= 2) Resource.Success(points)
                else Resource.Success(syntheticHistory(market.id, market.probability))
            } catch (e: Exception) {
                android.util.Log.e("Polymarket", "History error: ${e.message}")
                Resource.Success(syntheticHistory(market.id, market.probability))
            }
        }

    fun getProbabilityHistory(market: KalshiMarketDisplay): Resource<List<ProbabilityPoint>> =
        Resource.Success(syntheticHistory(market.ticker, market.probability))

    private fun syntheticHistory(seed: String, endProbability: Double): List<ProbabilityPoint> =
        SyntheticHistoryGenerator.generate(seed, endProbability)
```

- [ ] **Step 9: Full test run + build**

Run: `./gradlew testDebugUnitTest && ./gradlew assembleDebug -q`
Expected: all tests pass, build succeeds.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/example/liveticker/network/PolymarketClobApiService.kt app/src/main/java/com/example/liveticker/network/RetrofitClient.kt app/src/main/java/com/example/liveticker/network/PolymarketApiService.kt app/src/main/java/com/example/liveticker/data/PredictionMarketRepository.kt app/src/main/java/com/example/liveticker/data/ProbabilityHistory.kt app/src/test/java/com/example/liveticker/ProbabilityHistoryMappingTest.kt
git commit -m "feat: fetch probability history from Polymarket CLOB with fallback"
```

---

### Task 4: ProbabilityChartView (custom Canvas view)

**Files:**
- Create: `app/src/main/java/com/example/liveticker/ui/ProbabilityChartView.kt`

**Interfaces:**
- Consumes: `ProbabilityPoint` (Task 1), colors `accent_green`, `accent_red`, `outline`, `text_hint` (existing).
- Produces: `class ProbabilityChartView(context, attrs) : View` with `fun setPoints(points: List<ProbabilityPoint>)`.

- [ ] **Step 1: Write the view**

```kotlin
package com.example.liveticker.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.example.liveticker.R
import com.example.liveticker.data.ProbabilityPoint

/**
 * Minimal probability line chart: gridlines at 0/50/100%, polyline,
 * gradient fill under the line. Trend up (or flat) draws in accent_green,
 * trend down in accent_red. No touch interaction.
 */
class ProbabilityChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var points: List<ProbabilityPoint> = emptyList()

    private val density = resources.displayMetrics.density
    private val labelWidth = 32 * density
    private val chartPadding = 4 * density

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.outline)
        alpha = 60
        strokeWidth = 1 * density
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.text_hint)
        textSize = 10 * density
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2 * density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setPoints(points: List<ProbabilityPoint>) {
        this.points = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val left = labelWidth
        val right = width - chartPadding
        val top = chartPadding + labelPaint.textSize
        val bottom = height - chartPadding - labelPaint.textSize

        // Gridlines + labels at 100%, 50%, 0%
        listOf(1.0 to "100", 0.5 to "50", 0.0 to "0").forEach { (p, label) ->
            val y = yFor(p, top, bottom)
            canvas.drawLine(left, y, right, y, gridPaint)
            canvas.drawText(label, 0f, y + labelPaint.textSize / 3, labelPaint)
        }

        if (points.size < 2) return

        val trendUp = points.last().probability >= points.first().probability
        val color = context.getColor(if (trendUp) R.color.accent_green else R.color.accent_red)
        linePaint.color = color
        fillPaint.shader = LinearGradient(
            0f, top, 0f, bottom,
            (color and 0x00FFFFFF) or 0x55000000, (color and 0x00FFFFFF),
            Shader.TileMode.CLAMP
        )

        val minT = points.first().timestampMs
        val maxT = points.last().timestampMs
        val spanT = (maxT - minT).coerceAtLeast(1)

        val line = Path()
        points.forEachIndexed { i, pt ->
            val x = left + (right - left) * (pt.timestampMs - minT).toFloat() / spanT
            val y = yFor(pt.probability, top, bottom)
            if (i == 0) line.moveTo(x, y) else line.lineTo(x, y)
        }

        val fill = Path(line).apply {
            lineTo(right, bottom)
            lineTo(left, bottom)
            close()
        }
        canvas.drawPath(fill, fillPaint)
        canvas.drawPath(line, linePaint)
    }

    private fun yFor(probability: Double, top: Float, bottom: Float): Float =
        bottom - ((bottom - top) * probability).toFloat()
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew assembleDebug -q`
Expected: build succeeds. (Rendering is verified visually in Task 6 — custom View drawing is not unit-testable in this project's plain-JUnit setup.)

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/liveticker/ui/ProbabilityChartView.kt
git commit -m "feat: add custom probability chart view"
```

---

### Task 5: MarketDetailViewModel + factory

**Files:**
- Create: `app/src/main/java/com/example/liveticker/ui/MarketDetailViewModel.kt`

**Interfaces:**
- Consumes: `PredictionMarketRepository.getProbabilityHistory(...)` overloads (Task 3), `Resource<T>`, display models.
- Produces: `MarketDetailViewModel.history: StateFlow<Resource<List<ProbabilityPoint>>>`; `fun loadHistory(polymarket: PolymarketMarketDisplay?, kalshi: KalshiMarketDisplay?)`; `MarketDetailViewModelFactory(repository)`.

- [ ] **Step 1: Write the ViewModel + factory (same-file pattern used by the other screens)**

```kotlin
package com.example.liveticker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.liveticker.data.KalshiMarketDisplay
import com.example.liveticker.data.PolymarketMarketDisplay
import com.example.liveticker.data.PredictionMarketRepository
import com.example.liveticker.data.ProbabilityPoint
import com.example.liveticker.data.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarketDetailViewModel(
    private val repository: PredictionMarketRepository
) : ViewModel() {

    private val _history = MutableStateFlow<Resource<List<ProbabilityPoint>>>(Resource.Loading())
    val history: StateFlow<Resource<List<ProbabilityPoint>>> = _history.asStateFlow()

    fun loadHistory(polymarket: PolymarketMarketDisplay?, kalshi: KalshiMarketDisplay?) {
        viewModelScope.launch {
            _history.value = Resource.Loading()
            _history.value = when {
                polymarket != null -> repository.getProbabilityHistory(polymarket)
                kalshi != null -> repository.getProbabilityHistory(kalshi)
                else -> Resource.Error("No market provided")
            }
        }
    }
}

class MarketDetailViewModelFactory(
    private val repository: PredictionMarketRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarketDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew assembleDebug -q`
Expected: build succeeds. (No unit test: the project has no repository abstraction or mocking library; this ViewModel is thin dispatch logic, exercised on-device in Task 6.)

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/liveticker/ui/MarketDetailViewModel.kt
git commit -m "feat: add market detail view model"
```

---

### Task 6: Layout, fragment, nav graph, and click rewiring

**Files:**
- Create: `app/src/main/res/layout/fragment_market_detail.xml`
- Create: `app/src/main/java/com/example/liveticker/MarketDetailFragment.kt`
- Modify: `app/src/main/res/navigation/nav_graph.xml` (new destination + action)
- Modify: `app/src/main/java/com/example/liveticker/MarketBrowserFragment.kt` (`onMarketClicked`, imports)

**Interfaces:**
- Consumes: everything from Tasks 1–5; `webUrl` on both models; nav action id `action_MarketBrowserFragment_to_MarketDetailFragment`; arg keys `"polymarket_market"` / `"kalshi_market"`.
- Produces: the user-facing screen.

- [ ] **Step 1: Create the layout**

`app/src/main/res/layout/fragment_market_detail.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <!-- Header card -->
        <com.google.android.material.card.MaterialCardView
            style="@style/Widget.Material3.CardView.Filled"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:cardBackgroundColor="@color/surface_1"
            app:cardCornerRadius="12dp"
            app:cardElevation="0dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical">

                    <TextView
                        android:id="@+id/detail_source_badge"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:textSize="10sp"
                        android:textStyle="bold"
                        android:textColor="@color/onPrimaryContainer"
                        android:background="@drawable/category_background"
                        android:paddingHorizontal="8dp"
                        android:paddingVertical="2dp"
                        tools:text="Polymarket" />

                    <TextView
                        android:id="@+id/detail_category"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="8dp"
                        android:textSize="10sp"
                        android:textColor="@color/onPrimaryContainer"
                        android:background="@drawable/category_background"
                        android:paddingHorizontal="8dp"
                        android:paddingVertical="2dp"
                        tools:text="Crypto" />
                </LinearLayout>

                <TextView
                    android:id="@+id/detail_question"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    android:textAppearance="@style/TextAppearance.LiveTicker.TitleMedium"
                    tools:text="Will Bitcoin hit $100k by end of 2025?" />

                <TextView
                    android:id="@+id/detail_probability"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:textSize="36sp"
                    android:textStyle="bold"
                    android:fontFamily="monospace"
                    android:textColor="@color/onSurface"
                    tools:text="72.0%" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Stats card -->
        <com.google.android.material.card.MaterialCardView
            style="@style/Widget.Material3.CardView.Filled"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            app:cardBackgroundColor="@color/surface_1"
            app:cardCornerRadius="12dp"
            app:cardElevation="0dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Market Stats"
                    android:textAppearance="@style/TextAppearance.LiveTicker.TitleMedium" />

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="12dp"
                    android:orientation="horizontal">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="Yes price"
                        android:textColor="@color/text_secondary"
                        android:textSize="13sp" />

                    <TextView
                        android:id="@+id/detail_yes_price"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:fontFamily="monospace"
                        android:textColor="@color/accent_green"
                        android:textSize="13sp"
                        tools:text="72¢" />
                </LinearLayout>

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:orientation="horizontal">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="No price"
                        android:textColor="@color/text_secondary"
                        android:textSize="13sp" />

                    <TextView
                        android:id="@+id/detail_no_price"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:fontFamily="monospace"
                        android:textColor="@color/accent_red"
                        android:textSize="13sp"
                        tools:text="28¢" />
                </LinearLayout>

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:orientation="horizontal">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="24h volume"
                        android:textColor="@color/text_secondary"
                        android:textSize="13sp" />

                    <TextView
                        android:id="@+id/detail_volume"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:fontFamily="monospace"
                        android:textColor="@color/text_primary"
                        android:textSize="13sp"
                        tools:text="$150.0K" />
                </LinearLayout>

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:orientation="horizontal">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="Liquidity"
                        android:textColor="@color/text_secondary"
                        android:textSize="13sp" />

                    <TextView
                        android:id="@+id/detail_liquidity"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:fontFamily="monospace"
                        android:textColor="@color/text_primary"
                        android:textSize="13sp"
                        tools:text="$2.5M" />
                </LinearLayout>

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:orientation="horizontal">

                    <TextView
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="Resolves"
                        android:textColor="@color/text_secondary"
                        android:textSize="13sp" />

                    <TextView
                        android:id="@+id/detail_resolution"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:fontFamily="monospace"
                        android:textColor="@color/text_primary"
                        android:textSize="13sp"
                        tools:text="2025-12-31" />
                </LinearLayout>
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Chart card -->
        <com.google.android.material.card.MaterialCardView
            style="@style/Widget.Material3.CardView.Filled"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            app:cardBackgroundColor="@color/surface_1"
            app:cardCornerRadius="12dp"
            app:cardElevation="0dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Probability (30d)"
                    android:textAppearance="@style/TextAppearance.LiveTicker.TitleMedium" />

                <FrameLayout
                    android:layout_width="match_parent"
                    android:layout_height="180dp"
                    android:layout_marginTop="12dp">

                    <com.example.liveticker.ui.ProbabilityChartView
                        android:id="@+id/detail_chart"
                        android:layout_width="match_parent"
                        android:layout_height="match_parent" />

                    <ProgressBar
                        android:id="@+id/detail_chart_loading"
                        android:layout_width="32dp"
                        android:layout_height="32dp"
                        android:layout_gravity="center"
                        android:visibility="gone" />
                </FrameLayout>
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <com.google.android.material.button.MaterialButton
            android:id="@+id/detail_open_browser"
            style="@style/Widget.Material3.Button.TextButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:layout_marginTop="8dp"
            tools:text="Open on Polymarket" />
    </LinearLayout>
</ScrollView>
```

- [ ] **Step 2: Add nav destination and action**

In `app/src/main/res/navigation/nav_graph.xml`, add inside the `MarketBrowserFragment` `<fragment>` element (next to its existing actions):

```xml
        <action
            android:id="@+id/action_MarketBrowserFragment_to_MarketDetailFragment"
            app:destination="@id/MarketDetailFragment" />
```

And add as a new top-level destination (before the `<dialog>` element):

```xml
    <fragment
        android:id="@+id/MarketDetailFragment"
        android:name="com.example.liveticker.MarketDetailFragment"
        android:label="Market Detail"
        tools:layout="@layout/fragment_market_detail">

        <argument
            android:name="polymarket_market"
            app:argType="com.example.liveticker.data.PolymarketMarketDisplay"
            app:nullable="true"
            android:defaultValue="@null" />

        <argument
            android:name="kalshi_market"
            app:argType="com.example.liveticker.data.KalshiMarketDisplay"
            app:nullable="true"
            android:defaultValue="@null" />
    </fragment>
```

- [ ] **Step 3: Create MarketDetailFragment**

```kotlin
package com.example.liveticker

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.liveticker.data.KalshiMarketDisplay
import com.example.liveticker.data.PolymarketMarketDisplay
import com.example.liveticker.data.PredictionMarketRepository
import com.example.liveticker.data.Resource
import com.example.liveticker.databinding.FragmentMarketDetailBinding
import com.example.liveticker.ui.MarketDetailViewModel
import com.example.liveticker.ui.MarketDetailViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class MarketDetailFragment : Fragment() {

    private var _binding: FragmentMarketDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MarketDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val polymarket = BundleCompat.getParcelable(args, "polymarket_market", PolymarketMarketDisplay::class.java)
        val kalshi = BundleCompat.getParcelable(args, "kalshi_market", KalshiMarketDisplay::class.java)
        check(polymarket != null || kalshi != null) { "MarketDetailFragment needs a market argument" }

        val factory = MarketDetailViewModelFactory(PredictionMarketRepository())
        viewModel = ViewModelProvider(this, factory)[MarketDetailViewModel::class.java]

        bindMarket(polymarket, kalshi)
        observeHistory()
        viewModel.loadHistory(polymarket, kalshi)
    }

    private fun bindMarket(polymarket: PolymarketMarketDisplay?, kalshi: KalshiMarketDisplay?) {
        val question: String
        val probability: Double
        val volume: Double
        val liquidity: Double
        val resolves: String
        val category: String
        val sourceName: String
        val webUrl: String

        if (polymarket != null) {
            question = polymarket.question
            probability = polymarket.probability
            volume = polymarket.volume24h
            liquidity = polymarket.liquidity
            resolves = polymarket.resolutionDate
            category = polymarket.category
            sourceName = "Polymarket"
            webUrl = polymarket.webUrl
        } else {
            val market = kalshi!!
            question = market.title
            probability = market.probability
            volume = market.volume24h
            liquidity = market.liquidity
            resolves = market.closeTime
            category = market.category
            sourceName = "Kalshi"
            webUrl = market.webUrl
        }

        binding.detailSourceBadge.text = sourceName
        binding.detailCategory.text = category
        binding.detailQuestion.text = question
        binding.detailProbability.text = String.format(Locale.US, "%.1f%%", probability * 100)
        binding.detailYesPrice.text = String.format(Locale.US, "%.0f¢", probability * 100)
        binding.detailNoPrice.text = String.format(Locale.US, "%.0f¢", (1 - probability) * 100)
        binding.detailVolume.text = formatCurrency(volume)
        binding.detailLiquidity.text = formatCurrency(liquidity)
        binding.detailResolution.text = resolves
        binding.detailOpenBrowser.text = "Open on $sourceName"
        binding.detailOpenBrowser.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No browser available to open market", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeHistory() {
        lifecycleScope.launch {
            viewModel.history.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> binding.detailChartLoading.visibility = View.VISIBLE
                    is Resource.Success -> {
                        binding.detailChartLoading.visibility = View.GONE
                        binding.detailChart.setPoints(resource.data.orEmpty())
                    }
                    is Resource.Error -> binding.detailChartLoading.visibility = View.GONE
                }
            }
        }
    }

    private fun formatCurrency(value: Double): String = when {
        value >= 1_000_000 -> String.format(Locale.US, "$%.1fM", value / 1_000_000)
        value >= 1_000 -> String.format(Locale.US, "$%.1fK", value / 1_000)
        else -> String.format(Locale.US, "$%.0f", value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

- [ ] **Step 4: Rewire MarketBrowserFragment clicks**

In `app/src/main/java/com/example/liveticker/MarketBrowserFragment.kt`, replace the whole `onMarketClicked` function with:

```kotlin
    private fun onMarketClicked(marketItem: MarketListItem) {
        val bundle = when (marketItem) {
            is MarketListItem.PolymarketItem -> bundleOf("polymarket_market" to marketItem.market)
            is MarketListItem.KalshiItem -> bundleOf("kalshi_market" to marketItem.market)
        }
        findNavController().navigate(R.id.action_MarketBrowserFragment_to_MarketDetailFragment, bundle)
    }
```

Imports: add `androidx.core.os.bundleOf`; remove now-unused `android.content.ActivityNotFoundException`, `android.content.Intent`, `android.net.Uri` (Toast stays — the auth button uses it).

- [ ] **Step 5: Build + full unit test run**

Run: `./gradlew testDebugUnitTest && ./gradlew assembleDebug -q`
Expected: all tests pass, build succeeds.

- [ ] **Step 6: On-device verification (emulator `Ticker_Test`)**

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.liveticker/.MainActivity
```

Then, driving via `adb shell input tap` + `adb exec-out screencap -p` (coordinates on the 1080×2400 emulator):
1. Tap Browse Markets (773, 738). Tap the first market card (~540, 1030).
2. Screenshot: header shows question + source/category chips + big probability; stats card shows Yes/No prices summing to 100¢; chart card shows a colored line chart (this exercises the Parcel round-trip).
3. Tap "Open on Polymarket" → Chrome opens the event URL. Return with back.
4. Back to the browser list; switch to the Kalshi tab; open a Kalshi market; screenshot — same layout, "Open on Kalshi" button, chart renders (synthetic).
5. Press back twice → returns through Market Browser to Markets list.

Expected: all five checks pass, no crashes in `adb logcat -d --pid=$(adb shell pidof com.example.liveticker)`.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/res/layout/fragment_market_detail.xml app/src/main/java/com/example/liveticker/MarketDetailFragment.kt app/src/main/res/navigation/nav_graph.xml app/src/main/java/com/example/liveticker/MarketBrowserFragment.kt
git commit -m "feat: render prediction markets in-app with detail screen"
```

---

### Task 7: Spec status + docs

**Files:**
- Modify: `docs/superpowers/specs/2026-08-09-market-detail-design.md` (status line)

**Interfaces:** none.

- [ ] **Step 1: Mark spec implemented**

Change `**Status:** Approved (pending spec review)` to `**Status:** Implemented 2026-08-09`.

- [ ] **Step 2: Commit**

```bash
git add docs/superpowers/specs/2026-08-09-market-detail-design.md
git commit -m "docs: mark market detail spec implemented"
```
