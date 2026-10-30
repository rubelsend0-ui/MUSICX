package com.musicx.player.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePreferencesManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "musicx_vault",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("musicx_vault_fallback", Context.MODE_PRIVATE)
    }

    fun saveApiConfig(baseUrl: String, token: String) {
        prefs.edit()
            .putString("api_url", baseUrl.trim().removeSuffix("/"))
            .putString("api_token", token.trim())
            .apply()
    }

    fun getBaseUrl(): String = prefs.getString("api_url", "http://127.0.0.1:8787") ?: "http://127.0.0.1:8787"

    fun getApiToken(): String = prefs.getString("api_token", "") ?: ""

    fun clear() {
        prefs.edit().clear().apply()
    }
}
