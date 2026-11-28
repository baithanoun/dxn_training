package com.dxn.training.ui.certificate

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Bitmap.createBitmap needs the Android runtime, so the dimension contract cannot be
 * exercised by the local JVM suite (no Robolectric here). Run on a device/emulator with:
 * .\gradlew.bat :app:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class CertificateRendererInstrumentedTest {
    @Test
    fun renderUsesA4LandscapeDimensions() {
        val bitmap = CertificateRenderer.render(
            context = ApplicationProvider.getApplicationContext(),
            name = "اختبار",
            certificateTitle = "شهادة إتمام",
            code = "DXN-TEST-1",
            issuedAt = "2026-11-28T10:00:00Z"
        )
        try {
            assertEquals(1600, bitmap.width)
            assertEquals(1131, bitmap.height)
        } finally {
            bitmap.recycle()
        }
    }
}
