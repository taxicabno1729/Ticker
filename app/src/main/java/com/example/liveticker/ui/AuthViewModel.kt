package com.example.liveticker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.liveticker.data.AuthRepository
import com.example.liveticker.data.Resource
import com.example.liveticker.network.KalshiClient
import com.example.liveticker.network.KalshiLoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
    object LoggedOut : AuthState()
}

class AuthViewModel(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val _kalshiAuthState = MutableStateFlow<AuthState>(AuthState.Initial)
    val kalshiAuthState: StateFlow<AuthState> = _kalshiAuthState

    init {
        // Check if already logged in
        if (authRepository.isKalshiLoggedIn()) {
            _kalshiAuthState.value = AuthState.Success("Logged in as ${authRepository.getKalshiEmail()}")
        }
    }

    fun loginKalshi(email: String, password: String) {
        viewModelScope.launch {
            _kalshiAuthState.value = AuthState.Loading
            try {
                val response = KalshiClient.api.login(
                    KalshiLoginRequest(email, password)
                )
                
                if (response.token != null) {
                    authRepository.saveKalshiToken(response.token)
                    authRepository.saveKalshiCredentials(email)
                    _kalshiAuthState.value = AuthState.Success("Welcome, ${response.email}")
                } else {
                    _kalshiAuthState.value = AuthState.Error(response.message ?: "Login failed")
                }
            } catch (e: Exception) {
                _kalshiAuthState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun logoutKalshi() {
        viewModelScope.launch {
            try {
                val token = authRepository.getKalshiToken()
                if (token != null) {
                    KalshiClient.api.logout("Bearer $token")
                }
            } catch (e: Exception) {
                // Ignore logout errors
            } finally {
                authRepository.clearKalshiAuth()
                _kalshiAuthState.value = AuthState.LoggedOut
            }
        }
    }

    fun isKalshiLoggedIn(): Boolean {
        return authRepository.isKalshiLoggedIn()
    }

    fun getKalshiEmail(): String? {
        return authRepository.getKalshiEmail()
    }
}

class AuthViewModelFactory(
    private val application: Application,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(application, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
