package com.dxn.training.data.repository

import android.util.Log
import com.dxn.training.data.model.User
import com.dxn.training.data.model.UserRole
import com.dxn.training.data.remote.ApiClient
import com.dxn.training.data.remote.ApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "UserRepository"

class UserRepository(
    private val api: ApiService? = ApiClient.create()
) {
    private val _user = MutableStateFlow(MockData.currentUser)
    val user: StateFlow<User> = _user.asStateFlow()

    /**
     * تحميل بيانات المستخدم من السيرفر مع العودة إلى البيانات المحلية عند الفشل.
     */
    suspend fun loadUserProfile(userId: String): Boolean {
        val service = api ?: return false
        return try {
            val response = service.getUserProfile(userId)
            if (response.isSuccessful) {
                response.body()?.let { profile ->
                    _user.value = profile.toDomain()
                }
                true
            } else {
                Log.w(TAG, "getUserProfile failed with HTTP ${response.code()}")
                false
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Network error while loading user profile", e)
            false
        }
    }

    fun addXp(amount: Int) {
        _user.value = _user.value.copy(totalXp = _user.value.totalXp + amount)
    }

    fun updateHearts(hearts: Int) {
        _user.value = _user.value.copy(hearts = hearts.coerceIn(0, 5))
    }
}

private fun com.dxn.training.data.remote.UserProfileResponse.toDomain(): User = User(
    id = id,
    fullName = fullName,
    email = email ?: "",
    phone = "", // غير متوفر في الاستجابة الحالية
    dxnId = dxnId,
    role = role.toUserRole(),
    totalXp = totalXp,
    currentStreak = currentStreak,
    hearts = hearts,
    gems = gems,
    badges = emptyList(),
    certificates = emptyList()
)

private fun String.toUserRole(): UserRole = try {
    UserRole.valueOf(this.uppercase())
} catch (e: Exception) {
    UserRole.TRAINEE
}
