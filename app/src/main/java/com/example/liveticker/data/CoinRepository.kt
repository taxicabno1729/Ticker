package com.example.liveticker.data

import com.example.liveticker.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CoinRepository {
    suspend fun getMarkets(): Resource<List<Ticker>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getMarkets()
                Resource.Success(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    suspend fun getPrices(ids: String): Resource<Map<String, Map<String, Double>>> {
         return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getPrices(ids)
                Resource.Success(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    suspend fun getCoinDetail(coinId: String): Resource<CoinDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getCoinDetail(coinId)
                Resource.Success(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    suspend fun getMarketChart(coinId: String, days: Int = 30): Resource<MarketChart> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getMarketChart(coinId, days = days)
                Resource.Success(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}
