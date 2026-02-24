package com.example.liveticker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.liveticker.data.PredictionMarketMetricsCalculator
import com.example.liveticker.data.PredictionMarketMetricsState
import com.example.liveticker.data.PredictionMarketPosition
import com.example.liveticker.data.PredictionMarketRepository
import com.example.liveticker.data.PositionMetrics
import com.example.liveticker.data.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PredictionMarketViewModel(
    private val repository: PredictionMarketRepository
) : ViewModel() {

    private val _positions = MutableStateFlow<Resource<List<PredictionMarketPosition>>>(Resource.Loading())
    val positions: StateFlow<Resource<List<PredictionMarketPosition>>> = _positions

    private val _metrics = MutableStateFlow<Resource<PredictionMarketMetricsState>>(Resource.Loading())
    val metrics: StateFlow<Resource<PredictionMarketMetricsState>> = _metrics

    fun loadPositions(walletAddress: String) {
        viewModelScope.launch {
            _positions.value = Resource.Loading()
            _positions.value = repository.getPositions(walletAddress)
            
            // Calculate metrics when positions are loaded
            val positionsResult = _positions.value
            if (positionsResult is Resource.Success) {
                calculateMetrics(positionsResult.data ?: emptyList())
            }
        }
    }

    private fun calculateMetrics(positions: List<PredictionMarketPosition>) {
        viewModelScope.launch {
            _metrics.value = Resource.Loading()
            try {
                val totalValue = positions.sumOf { it.currentValue }
                
                val positionMetrics = positions.map { position ->
                    PositionMetrics(
                        marketId = position.marketId,
                        marketQuestion = position.marketQuestion,
                        weight = if (totalValue > 0) position.currentValue / totalValue else 0.0,
                        metrics = PredictionMarketMetricsCalculator.calculatePositionMetrics(position)
                    )
                }.sortedByDescending { it.weight }

                val portfolioMetrics = PredictionMarketMetricsCalculator.calculatePortfolioMetrics(positions)

                _metrics.value = Resource.Success(
                    PredictionMarketMetricsState(
                        metrics = portfolioMetrics,
                        positionMetrics = positionMetrics,
                        days = 30 // Default analysis period
                    )
                )
            } catch (e: Exception) {
                _metrics.value = Resource.Error(e.message ?: "Error calculating metrics")
            }
        }
    }

    fun refreshPositions(walletAddress: String) {
        loadPositions(walletAddress)
    }
}

class PredictionMarketViewModelFactory(
    private val repository: PredictionMarketRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PredictionMarketViewModel::class.java)) {
            return PredictionMarketViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
