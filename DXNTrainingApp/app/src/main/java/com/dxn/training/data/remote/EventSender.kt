package com.dxn.training.data.remote

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "EventSender"

/**
 * إرسال أحداث العميل إلى السيرفر دون حظر الواجهة أو تدفق الإكمال.
 * الفشل يُتجاهل (لا طابور محلي — ليس جزءاً من المواصفات).
 */
object EventSender {
    private val allowedNames = setOf(
        "app_opened",
        "level_started",
        "question_answered",
        "share_achievement",
        "certificate_downloaded"
    )
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun send(name: String, levelId: String? = null, payload: Map<String, Any?> = emptyMap()) {
        if (name !in allowedNames) {
            Log.w(TAG, "Ignoring disallowed client event: $name")
            return
        }
        scope.launch {
            try {
                val response = ApiClient.api.sendEvents(
                    AnalyticsEventsBatchRequest(
                        listOf(AnalyticsEventRequest(eventName = name, levelId = levelId, payload = payload))
                    )
                )
                if (!response.isSuccessful) {
                    Log.w(TAG, "sendEvents($name) failed with HTTP ${response.code()}")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "sendEvents($name) failed", e)
            }
        }
    }
}
