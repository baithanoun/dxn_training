package com.dxn.training

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dxn.training.data.remote.ApiClient
import com.dxn.training.ui.navigation.AppNavigation
import com.dxn.training.ui.theme.BrandIvory
import com.dxn.training.ui.theme.DXNTrainingTheme

class MainActivity : ComponentActivity() {
    private var pendingLeaderDxnId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        pendingLeaderDxnId = extractLeaderDxnId(intent)
        enableEdgeToEdge()
        setContent {
            DXNTrainingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BrandIvory)
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                    ) {
                        AppNavigation(
                            navController = navController,
                            pendingLeaderDxnId = pendingLeaderDxnId,
                            onLeaderDxnIdConsumed = { pendingLeaderDxnId = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractLeaderDxnId(intent)?.let { pendingLeaderDxnId = it }
    }

    private fun extractLeaderDxnId(intent: Intent?): String? {
        val data = intent?.data ?: return null
        if (intent.action != Intent.ACTION_VIEW) return null
        if (data.scheme != "https" || data.host != "dxn-train.dockhosting.cloud") return null
        val segments = data.pathSegments
        if (segments.size != 2 || segments[0] != "join") return null
        val dxnId = segments[1]
        return dxnId.takeIf { it.matches(Regex("^[A-Za-z0-9-]{1,32}$")) }
    }
}
