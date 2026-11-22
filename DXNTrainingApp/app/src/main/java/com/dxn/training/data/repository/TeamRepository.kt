package com.dxn.training.data.repository

import com.dxn.training.data.model.TeamMember
import com.dxn.training.data.remote.ApiClient
import com.dxn.training.data.remote.ApiService
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class TeamRepository(
    private val api: ApiService = ApiClient.api
) {
    /** أعضاء الفريق من السيرفر فقط (القادة فقط؛ للمتدربين يعيد 403). */
    suspend fun load(): Result<List<TeamMember>> = try {
        val response = api.getLeaderTeam()
        if (response.isSuccessful) {
            Result.success(response.body().orEmpty().map { it.toDomain() })
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
