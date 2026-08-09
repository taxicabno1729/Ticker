package com.example.liveticker

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.example.liveticker.data.KalshiMarketDisplay
import com.example.liveticker.data.PolymarketMarketDisplay

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test that round-trips the manually-implemented `Parcelable`
 * models through a real `android.os.Parcel`. Plain JUnit cannot construct
 * `Parcel`, so this coverage must live under androidTest rather than test.
 */
@RunWith(AndroidJUnit4::class)
class MarketDisplayParcelTest {

    @Test
    fun polymarketMarketDisplay_roundTripsThroughParcel() {
        val original = PolymarketMarketDisplay(
            "id1",
            "slug-1",
            "Q?",
            0.72,
            150000.0,
            2500000.0,
            "Crypto",
            "2025-12-31",
            "tok123"
        )

        val parcel = Parcel.obtain()
        try {
            original.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)

            val restored = PolymarketMarketDisplay.CREATOR.createFromParcel(parcel)

            assertEquals(original, restored)
        } finally {
            parcel.recycle()
        }
    }

    @Test
    fun polymarketMarketDisplay_roundTripsNullClobTokenId() {
        val original = PolymarketMarketDisplay(
            "id1",
            "slug-1",
            "Q?",
            0.72,
            150000.0,
            2500000.0,
            "Crypto",
            "2025-12-31",
            null
        )

        val parcel = Parcel.obtain()
        try {
            original.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)

            val restored = PolymarketMarketDisplay.CREATOR.createFromParcel(parcel)

            assertEquals(original, restored)
            assertNull(restored.clobTokenId)
        } finally {
            parcel.recycle()
        }
    }

    @Test
    fun kalshiMarketDisplay_roundTripsThroughParcel() {
        val original = KalshiMarketDisplay(
            "KX-1",
            "T?",
            0.68,
            125000.0,
            1800000.0,
            "Crypto",
            "2025-12-31"
        )

        val parcel = Parcel.obtain()
        try {
            original.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)

            val restored = KalshiMarketDisplay.CREATOR.createFromParcel(parcel)

            assertEquals(original, restored)
        } finally {
            parcel.recycle()
        }
    }
}
