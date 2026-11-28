package com.dxn.training.ui.certificate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/** Wraps the rendered certificate bitmap in an A4-landscape PDF without a new library. */
object CertificatePdfWriter {
    private const val PageWidth = 842
    private const val PageHeight = 595

    fun write(context: Context, bitmap: Bitmap, fileName: String): File {
        val directory = File(context.filesDir, "certificates").apply { mkdirs() }
        val file = File(directory, "$fileName.pdf")
        FileOutputStream(file).use { writeTo(bitmap, it) }
        return file
    }

    fun writeTo(bitmap: Bitmap, output: OutputStream) {
        val document = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(PageWidth, PageHeight, 1).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawBitmap(
                bitmap,
                null,
                RectF(0f, 0f, PageWidth.toFloat(), PageHeight.toFloat()),
                null
            )
            document.finishPage(page)
            document.writeTo(output)
        } finally {
            document.close()
        }
    }
}
