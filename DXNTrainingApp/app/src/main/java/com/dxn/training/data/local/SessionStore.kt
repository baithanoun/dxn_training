package com.dxn.training.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

data class Session(
    val token: String,
    val userId: String,
    val role: String
)

class SessionStore(private val context: Context) {
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val ROLE_KEY = stringPreferencesKey("role")

    val session: Flow<Session?> = context.dataStore.data.map { prefs ->
        val token = prefs[TOKEN_KEY]
        val userId = prefs[USER_ID_KEY]
        val role = prefs[ROLE_KEY]
        if (token != null && userId != null && role != null) {
            Session(token, userId, role)
        } else {
            null
        }
    }

    suspend fun save(token: String, userId: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[ROLE_KEY] = role
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(ROLE_KEY)
        }
    }
}