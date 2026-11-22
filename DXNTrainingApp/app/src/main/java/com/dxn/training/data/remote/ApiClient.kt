package com.dxn.training.data.remote

import android.content.Context
import com.dxn.training.BuildConfig
import com.dxn.training.data.local.SessionStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    @Volatile
    private var sessionStore: SessionStore? = null

    private val _logoutEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEvents: SharedFlow<Unit> = _logoutEvents

    fun init(context: Context) {
        if (sessionStore == null) {
            sessionStore = SessionStore(context.applicationContext)
        }
    }

    fun getSessionStore(): SessionStore? = sessionStore

    fun create(loggingEnabled: Boolean = BuildConfig.DEBUG): ApiService {
        val client = OkHttpClient.Builder().apply {
            if (loggingEnabled) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
            }
            addInterceptor(Interceptor { chain ->
                val request = chain.request()
                val store = sessionStore
                val token = store?.let { runBlocking { it.session.first()?.token } }
                val newRequest = request.newBuilder().apply {
                    addHeader("Accept", "application/json")
                    addHeader("X-Client", "android")
                    if (token != null) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }.build()
                val response = chain.proceed(newRequest)
                // جلسة منتهية: امسح التوكن وأخطر واجهة المستخدم لتعود إلى تسجيل الدخول.
                if (response.code == 401 && token != null) {
                    runBlocking { store.clear() }
                    _logoutEvents.tryEmit(Unit)
                }
                response
            })
        }.build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
