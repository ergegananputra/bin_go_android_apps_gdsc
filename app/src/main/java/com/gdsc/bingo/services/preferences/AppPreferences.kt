package com.gdsc.bingo.services.preferences

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("COM_GDSC_BINGO_SERVICES_ACCOUNT", Context.MODE_PRIVATE)

    fun setToken(token: String) {
        with(prefs.edit()) {
            putString(Keys.TOKEN, token)
            apply()
        }
    }

    fun getToken(): String? {
        return with(prefs) {
            getString(Keys.TOKEN, null)
        }
    }

    fun isTokenExist(): Boolean {
        return getToken() != null
    }

    private object Keys {
        const val TOKEN = "USER_TOKEN_BINGO_ACCOUNT"
    }
}