package com.dxn.training.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dxn.training.data.remote.ApiService
import com.dxn.training.data.remote.AppConfigResponse
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.first

private val Context.appConfigDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_config")

class AppConfigRepository(private val api: ApiService, private val context: Context) {
    private val configKey = stringPreferencesKey("app_config_json")
    private val gson = Gson()

    suspend fun load(): AppConfigResponse {
        val fromNetwork = try {
            val response = api.getAppConfig()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }

        if (fromNetwork != null) {
            try {
                context.appConfigDataStore.edit { prefs ->
                    prefs[configKey] = gson.toJson(fromNetwork)
                }
            } catch (e: Exception) {
                // فشل الحفظ لا يجب أن يؤثر على القيمة المعروضة.
            }
            return fromNetwork
        }

        val cached = try {
            context.appConfigDataStore.data.first()[configKey]?.let(::parseCached)
        } catch (e: Exception) {
            null
        }
        if (cached != null) return cached

        // shortcut: hard-coded first-run fallback mirrors server defaults, update both together
        return AppConfigResponse(
            appName = "حكاية تميّز",
            teamName = "حكاية تميّز",
            teamTagline = "لأن كل طموح يستحق أن يُروى!",
            teamBio = "في حكاية تميّز، نجمع بين ثقافة الحياة الصحية مع DXN وقوة العمل الجماعي الذكي عبر الإنترنت. نوفّر بيئة تدريبية داعمة ترافق كل متدرب ومتدربة من البداية حتى الاحتراف، لبناء مشروع خاص بمرونة تامة، ولتصبح كل حكاية قادمة حكاية تميّز.",
            teamWelcome = "أهلاً بك في عائلتك الثانية.. رحلتك نحو التميّز تبدأ الآن!",
            maintenanceMode = false
        )
    }

    private fun parseCached(json: String): AppConfigResponse? {
        return try {
            val obj = gson.fromJson(json, JsonObject::class.java) ?: return null
            if (obj.get("appName")?.isJsonPrimitive != true ||
                obj.get("teamName")?.isJsonPrimitive != true ||
                obj.get("maintenanceMode")?.isJsonPrimitive != true
            ) {
                null
            } else {
                gson.fromJson(obj, AppConfigResponse::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
}
