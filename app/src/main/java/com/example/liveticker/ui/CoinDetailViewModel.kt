package com.example.liveticker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.liveticker.data.CoinDetailState
import com.example.liveticker.data.CoinRepository
import com.example.liveticker.data.GreeksCalculator
import com.example.liveticker.data.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoinDetailViewModel(
    private val coinRepository: CoinRepository,
    private val coinId: String
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<CoinDetailState>>(Resource.Loading())
    val state: StateFlow<Resource<CoinDetailState>> = _state

    init {
        loadCoinDetail()
    }

    fun loadCoinDetail() {
        viewModelScope.launch {
            _state.value = Resource.Loading()

            val detailDeferred = async { coinRepository.getCoinDetail(coinId) }
            val chartDeferred = async { coinRepository.getMarketChart(coinId) }

            val detailResult = detailDeferred.await()
            val chartResult = chartDeferred.await()

            if (detailResult is Resource.Error) {
                _state.value = Resource.Error(detailResult.message ?: "Failed to load coin details")
                return@launch
            }

            val coinDetail = detailResult.data
            if (coinDetail == null) {
                _state.value = Resource.Error("No data received")
                return@launch
            }

            val prices = chartResult.data?.prices
            val greeks = if (prices != null) GreeksCalculator.calculate(prices) else null

            _state.value = Resource.Success(
                CoinDetailState(
                    coinDetail = coinDetail,
                    greeks = greeks,
                    priceHistory = prices,
                    greeksDays = 30
                )
            )
        }
    }

    fun updateDays(days: Int) {
        viewModelScope.launch {
            val currentState = (_state.value as? Resource.Success)?.data ?: return@launch
            _state.value = Resource.Success(currentState.copy(greeks = null, greeksDays = days))

            val chartResult = coinRepository.getMarketChart(coinId, days)
            val prices = chartResult.data?.prices
            val greeks = if (prices != null) GreeksCalculator.calculate(prices) else null

            _state.value = Resource.Success(
                currentState.copy(
                    greeks = greeks,
                    priceHistory = prices,
                    greeksDays = days
                )
            )
        }
    }
}

class CoinDetailViewModelFactory(
    private val coinRepository: CoinRepository,
    private val coinId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoinDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoinDetailViewModel(coinRepository, coinId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
