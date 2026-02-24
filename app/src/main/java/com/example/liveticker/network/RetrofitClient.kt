package com.example.liveticker.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3/"

    val instance: CoinGeckoApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(COINGECKO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(CoinGeckoApiService::class.java)
    }
}

object PolymarketClient {
    val api: PolymarketApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(PolymarketApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(PolymarketApiService::class.java)
    }
}

object KalshiClient {
    val api: KalshiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(KalshiApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(KalshiApiService::class.java)
    }
}
