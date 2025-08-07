package com.example.gpsapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {
    private val USERNAME_KEY = stringPreferencesKey("username")
    private val PASSWORD_KEY = stringPreferencesKey("password")
    private val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")

    suspend fun saveLogin(username: String, password: String, rememberMe: Boolean) {
        context.dataStore.edit {
            it[USERNAME_KEY] = username
            it[PASSWORD_KEY] = password
            it[REMEMBER_ME_KEY] = rememberMe
        }
    }

    suspend fun clearLogin() {
        context.dataStore.edit {
            it.clear()
        }
    }

    suspend fun getLogin(): Triple<String?, String?, Boolean> {
        val prefs = context.dataStore.data.first()
        return Triple(
            prefs[USERNAME_KEY],
            prefs[PASSWORD_KEY],
            prefs[REMEMBER_ME_KEY] ?: false
        )
    }
}
