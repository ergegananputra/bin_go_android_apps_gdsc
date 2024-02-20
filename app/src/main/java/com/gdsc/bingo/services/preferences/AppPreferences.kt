package com.gdsc.bingo.services.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.gdsc.bingo.services.preferences.AppPreferences.Keys.KEY_USER_ID

class AppPreferences(private val context: Context) {
    private val mKA = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "COM_GDSC_BINGO_SERVICES_ACCOUNT",
        mKA,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var userId: String
        get() = sharedPreferences.getString(KEY_USER_ID, "")!!
        set(value) = sharedPreferences.edit().putString(KEY_USER_ID, value).apply()

    fun isUserLoggedIn(): Boolean= userId.isNotEmpty()

    var userName: String
        get() = sharedPreferences.getString(Keys.KEY_USER_NAME, "")!!
        set(value) = sharedPreferences.edit().putString(Keys.KEY_USER_NAME, value).apply()

    var userEmail: String
        get() = sharedPreferences.getString(Keys.KEY_USER_EMAIL, "")!!
        set(value) = sharedPreferences.edit().putString(Keys.KEY_USER_EMAIL, value).apply()

    var score: Int
        get() = sharedPreferences.getInt(Keys.KEY_SCORE, 0)
        set(value) = sharedPreferences.edit().putInt(Keys.KEY_SCORE, value).apply()

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    private object Keys {
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_SCORE = "user_score"
    }
}