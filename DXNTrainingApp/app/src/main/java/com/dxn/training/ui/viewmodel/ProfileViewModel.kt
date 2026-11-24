package com.dxn.training.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dxn.training.data.local.SessionStore
import com.dxn.training.data.model.Certificate
import com.dxn.training.data.model.User
import com.dxn.training.data.remote.ApiClient
import com.dxn.training.data.remote.ApiService
import com.dxn.training.data.remote.DeleteAccountRequest
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.data.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val TAG = "ProfileViewModel"

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val sessionStore: SessionStore? = ApiClient.getSessionStore(),
    private val api: ApiService = ApiClient.api,
    private val contentRepository: ContentRepository = ContentRepository.getInstance()
) : ViewModel() {
    val user: StateFlow<User> = userRepository.user

    // الشهادات من تقدّم الأقسام على السيرفر (certificateIssued)، لا من حالة محلية.
    private val _certificates = MutableStateFlow<List<Certificate>>(emptyList())
    val certificates: StateFlow<List<Certificate>> = _certificates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            sessionStore?.session
                ?.map { it?.userId }
                ?.distinctUntilChanged()
                ?.collect { userId -> loadProfile(userId) }
        }
    }

    /**
     * Reloads the profile and its certificates from the server.
     *
     * Called whenever the profile screen resumes so a certificate issued after
     * finishing a section shows up without restarting the app.
     */
    fun refresh() {
        viewModelScope.launch {
            val userId = sessionStore?.session?.first()?.userId
            loadProfile(userId)
        }
    }

    /**
     * Deletes the current user's account. Returns true on success.
     */
    suspend fun deleteAccount(password: String): Boolean {
        return try {
            val response = api.deleteAccount(DeleteAccountRequest(password))
            if (response.isSuccessful) {
                sessionStore?.clear()
                true
            } else {
                false
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Unable to delete account", e)
            false
        }
    }
    private suspend fun loadProfile(userId: String?) {
        if (userId == null) {
            _certificates.value = emptyList()
            return
        }
        _isLoading.value = true
        try {
            userRepository.loadUserProfile(userId)
            val response = api.getSectionProgress(userId)
            if (response.isSuccessful) {
                val titles = contentRepository.sections.first()
                    .associate { it.id to it.certificateTitle }
                _certificates.value = response.body().orEmpty()
                    .filter { it.value.certificateIssued }
                    .map { (sectionId, progress) ->
                        Certificate(
                            id = progress.certificateId ?: sectionId,
                            title = titles[sectionId]?.takeIf { it.isNotBlank() }
                                ?: "شهادة إتمام",
                            issuedAt = progress.completedAt.orEmpty()
                        )
                    }
            } else {
                Log.w(TAG, "getSectionProgress failed with HTTP ${response.code()}")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Unable to load profile certificates", e)
        } finally {
            _isLoading.value = false
        }
    }
}
