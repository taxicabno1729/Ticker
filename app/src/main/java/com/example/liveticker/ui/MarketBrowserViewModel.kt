package com.example.liveticker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.liveticker.data.PredictionMarketRepository
import com.example.liveticker.data.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MarketBrowserViewModel(
    private val repository: PredictionMarketRepository
) : ViewModel() {

    private val _polymarketMarkets = MutableStateFlow<Resource<List<com.example.liveticker.data.PolymarketMarketDisplay>>>(Resource.Loading())
    val polymarketMarkets: StateFlow<Resource<List<com.example.liveticker.data.PolymarketMarketDisplay>>> = _polymarketMarkets

    private val _kalshiMarkets = MutableStateFlow<Resource<List<com.example.liveticker.data.KalshiMarketDisplay>>>(Resource.Loading())
    val kalshiMarkets: StateFlow<Resource<List<com.example.liveticker.data.KalshiMarketDisplay>>> = _kalshiMarkets

    private val _selectedTab = MutableStateFlow(0) // 0 = Polymarket, 1 = Kalshi
    val selectedTab: StateFlow<Int> = _selectedTab

    fun loadMarkets() {
        viewModelScope.launch {
            _polymarketMarkets.value = Resource.Loading()
            _polymarketMarkets.value = repository.getPolymarketMarkets()
        }
        
        viewModelScope.launch {
            _kalshiMarkets.value = Resource.Loading()
            _kalshiMarkets.value = repository.getKalshiMarkets()
        }
    }

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun refreshMarkets() {
        loadMarkets()
    }
}

class MarketBrowserViewModelFactory(
    private val repository: PredictionMarketRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketBrowserViewModel::class.java)) {
            return MarketBrowserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
