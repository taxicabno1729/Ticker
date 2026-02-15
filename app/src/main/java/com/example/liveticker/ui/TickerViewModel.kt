package com.example.liveticker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.math.BigDecimal
import com.example.liveticker.data.CoinRepository
import com.example.liveticker.data.Resource
import com.example.liveticker.data.Ticker
import com.example.liveticker.data.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class TickerViewModel(
    private val coinRepository: CoinRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _tickers = MutableStateFlow<Resource<List<Ticker>>>(Resource.Loading())
    val tickers: StateFlow<Resource<List<Ticker>>> = _tickers

    private val _nativeBalance = MutableStateFlow<Resource<BigDecimal>>(Resource.Loading())
    val nativeBalance: StateFlow<Resource<BigDecimal>> = _nativeBalance

    private var allTickers: List<Ticker> = emptyList()

    init {
        fetchTickers()
    }

    fun fetchTickers() {
        viewModelScope.launch {
            _tickers.value = Resource.Loading()
            val result = coinRepository.getMarkets()
            if (result is Resource.Success) {
                allTickers = result.data ?: emptyList()
            }
            _tickers.value = result
        }
    }

    fun fetchWalletBalance(address: String) {
        viewModelScope.launch {
            _nativeBalance.value = Resource.Loading()
            _nativeBalance.value = walletRepository.getNativeBalance(address)
        }
    }

    fun filterTickers(query: String?) {
        if (query.isNullOrEmpty()) {
            _tickers.value = Resource.Success(allTickers)
        } else {
            val filtered = allTickers.filter {
                it.name.lowercase(Locale.getDefault())
                    .contains(query.lowercase(Locale.getDefault())) ||
                        it.symbol.lowercase(Locale.getDefault())
                            .contains(query.lowercase(Locale.getDefault()))
            }
            _tickers.value = Resource.Success(filtered)
        }
    }
}

class TickerViewModelFactory(
    private val coinRepository: CoinRepository,
    private val walletRepository: WalletRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TickerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TickerViewModel(coinRepository, walletRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
