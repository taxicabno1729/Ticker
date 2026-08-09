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
