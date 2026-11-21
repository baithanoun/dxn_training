package com.dxn.training.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dxn.training.data.model.User
import com.dxn.training.data.repository.UserRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {
    val user: StateFlow<User> = userRepository.user

    init {
        viewModelScope.launch {
            userRepository.loadUserProfile("u1")
        }
    }
}
