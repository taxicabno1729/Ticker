package com.example.liveticker.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Repository for managing API authentication tokens
 */
class AuthRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "api_auth_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // Kalshi Auth
    fun saveKalshiToken(token: String) {
        encryptedPrefs.edit {
            putString(KEY_KALSHI_TOKEN, token)
            putBoolean(KEY_KALSHI_LOGGED_IN, true)
        }
    }

    fun getKalshiToken(): String? {
        return encryptedPrefs.getString(KEY_KALSHI_TOKEN, null)
    }

    fun isKalshiLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_KALSHI_LOGGED_IN, false) && 
               !getKalshiToken().isNullOrEmpty()
    }

    fun clearKalshiAuth() {
        encryptedPrefs.edit {
            remove(KEY_KALSHI_TOKEN)
        }
        prefs.edit {
            putBoolean(KEY_KALSHI_LOGGED_IN, false)
        }
    }

    // Polymarket Auth (if needed in future)
    fun savePolymarketApiKey(apiKey: String) {
        encryptedPrefs.edit {
            putString(KEY_POLYMARKET_API_KEY, apiKey)
        }
    }

    fun getPolymarketApiKey(): String? {
        return encryptedPrefs.getString(KEY_POLYMARKET_API_KEY, null)
    }

    // User credentials (for auto-login)
    fun saveKalshiCredentials(email: String) {
        prefs.edit {
            putString(KEY_KALSHI_EMAIL, email)
        }
    }

    fun getKalshiEmail(): String? {
        return prefs.getString(KEY_KALSHI_EMAIL, null)
    }

    companion object {
        private const val KEY_KALSHI_TOKEN = "kalshi_token"
        private const val KEY_KALSHI_EMAIL = "kalshi_email"
        private const val KEY_KALSHI_LOGGED_IN = "kalshi_logged_in"
        private const val KEY_POLYMARKET_API_KEY = "polymarket_api_key"
    }
}
