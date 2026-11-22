package com.dxn.training.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dxn.training.data.local.SessionStore
import com.dxn.training.data.model.User
import com.dxn.training.data.remote.ApiClient
import com.dxn.training.data.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val sessionStore: SessionStore? = ApiClient.getSessionStore()
) : ViewModel() {
    val user: StateFlow<User> = userRepository.user

    init {
        viewModelScope.launch {
            sessionStore?.session
                ?.map { it?.userId }
                ?.distinctUntilChanged()
                ?.collect { userId ->
                    if (userId != null) {
                        try {
                            userRepository.loadUserProfile(userId)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            // يُعرض المحتوى المحلي الاحتياطي إذا تعذّر جلب الملف الشخصي.
                        }
                    }
                }
        }
    }
}
