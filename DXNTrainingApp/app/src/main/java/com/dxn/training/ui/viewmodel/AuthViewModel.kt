package com.dxn.training.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dxn.training.data.local.Session
import com.dxn.training.data.local.SessionStore
import com.dxn.training.data.remote.ApiClient
import com.dxn.training.data.remote.ApiService
import com.dxn.training.data.remote.AuthResponse
import com.dxn.training.data.remote.LoginRequest
import com.dxn.training.data.remote.RegisterRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object LoggedIn : AuthUiState()
}

class AuthViewModel(
    private val api: ApiService,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    val session: StateFlow<Session?> = sessionStore.session.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    init {
        viewModelScope.launch {
            sessionStore.session.collect { saved ->
                if (saved != null) {
                    _state.value = AuthUiState.LoggedIn
                } else if (_state.value !is AuthUiState.Idle && _state.value !is AuthUiState.Loading) {
                    _state.value = AuthUiState.Idle
                }
            }
        }
        viewModelScope.launch {
            ApiClient.logoutEvents.collect { _state.value = AuthUiState.Idle }
        }
    }

    fun login(login: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            try {
                handleAuth(api.login(LoginRequest(login.trim(), password)), "تعذر تسجيل الدخول")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = AuthUiState.Error("تعذر الاتصال بالخادم")
            }
        }
    }

    fun register(
        fullName: String,
        contact: String,
        password: String,
        dxnId: String,
        leaderDxnId: String,
        gender: String,
        acceptTerms: Boolean = false
    ) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            try {
                val value = contact.trim()
                val isEmail = value.contains("@")
                val response = api.register(
                    RegisterRequest(
                        fullName = fullName.trim(),
                        email = if (isEmail) value else null,
                        phone = if (isEmail) null else value,
                        password = password,
                        dxnId = dxnId.trim(),
                        gender = gender,
                        leaderDxnId = leaderDxnId.trim().takeIf { it.isNotEmpty() },
                        acceptTerms = acceptTerms
                    )
                )
                handleAuth(response, "تعذر إنشاء الحساب")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = AuthUiState.Error("تعذر الاتصال بالخادم")
            }
        }
    }

    suspend fun currentUserId(): String? = sessionStore.session.first()?.userId

    fun logout() {
        viewModelScope.launch {
            try {
                api.logout()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // الإلغاء المحلي يبقى مضموناً حتى لو تعذّر الاتصال بالخادم.
            }
            sessionStore.clear()
            _state.value = AuthUiState.Idle
        }
    }

    private suspend fun handleAuth(response: Response<AuthResponse>, fallback: String) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                sessionStore.save(body.token, body.user.id, body.user.role, body.user.gender)
                _state.value = AuthUiState.LoggedIn
            } else {
                _state.value = AuthUiState.Error("تعذر الاتصال بالخادم")
            }
        } else {
            _state.value = AuthUiState.Error(errorMessage(response.errorBody()?.string()) ?: fallback)
        }
    }

    private fun errorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val json = JSONObject(raw)
            val message = json.optString("message").takeIf { it.isNotBlank() }
            message ?: json.optJSONObject("errors")?.let { errors ->
                val keys = errors.keys()
                if (keys.hasNext()) {
                    val firstKey = keys.next()
                    errors.optJSONArray(firstKey)?.optString(0)?.takeIf { it.isNotBlank() }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
