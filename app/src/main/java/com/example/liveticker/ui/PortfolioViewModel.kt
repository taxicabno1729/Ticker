package com.example.liveticker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.liveticker.data.CoinRepository
import com.example.liveticker.data.PortfolioToken
import com.example.liveticker.data.Resource
import com.example.liveticker.data.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class PortfolioViewModel(
    private val walletRepository: WalletRepository,
    private val coinRepository: CoinRepository
) : ViewModel() {

    private val _portfolio = MutableStateFlow<Resource<List<PortfolioToken>>>(Resource.Loading())
    val portfolio: StateFlow<Resource<List<PortfolioToken>>> = _portfolio

    private val _nativeBalance = MutableStateFlow<Resource<BigDecimal>>(Resource.Loading())
    val nativeBalance: StateFlow<Resource<BigDecimal>> = _nativeBalance

    fun loadPortfolio(address: String) {
        viewModelScope.launch {
            _portfolio.value = Resource.Loading()
            _portfolio.value = walletRepository.getPortfolio(address, coinRepository)
        }
    }
    
    fun loadNativeBalance(address: String) {
        viewModelScope.launch {
            _nativeBalance.value = Resource.Loading()
            _nativeBalance.value = walletRepository.getNativeBalance(address)
        }
    }
}

class PortfolioViewModelFactory(
    private val walletRepository: WalletRepository,
    private val coinRepository: CoinRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PortfolioViewModel(walletRepository, coinRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
