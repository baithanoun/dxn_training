package com.dxn.training.ui.certificate

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** PdfDocument needs the Android runtime; run with :app:connectedDebugAndroidTest. */
@RunWith(AndroidJUnit4::class)
class CertificatePdfWriterInstrumentedTest {
    @Test
    fun writeProducesPdfFile() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val bitmap = CertificateRenderer.render(
            context = context,
            name = "اختبار",
            certificateTitle = "شهادة إتمام",
            code = "DXN-PDF-TEST",
            issuedAt = "2026-11-28T10:00:00Z"
        )
        val file = CertificatePdfWriter.write(context, bitmap, "plan6-pdf-test")
        try {
            assertTrue(file.exists())
            assertEquals("%PDF-", file.readBytes().copyOf(5).toString(Charsets.US_ASCII))
        } finally {
            file.delete()
            bitmap.recycle()
        }
    }
}
