package com.dxn.training.ui.certificate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import com.dxn.training.R
import com.dxn.training.ui.theme.BrandIvory
import com.dxn.training.ui.theme.BrandPlum
import com.dxn.training.ui.theme.BrandPlum700
import com.dxn.training.ui.theme.BrandRose600
import com.dxn.training.ui.theme.TextMuted
import com.dxn.training.ui.theme.TextPrimary
import java.util.Locale

/**
 * D3: the certificate is drawn on the device as a PNG; no PDF or rendering library.
 * A4 landscape at 1600x1131 keeps the text sharp enough to save and share.
 */
object CertificateRenderer {
    const val WIDTH = 1600
    const val HEIGHT = 1131

    private const val FRAME_INSET = 48f
    private const val TEXT_MAX_WIDTH = WIDTH - 220f
    // Forces a RTL paragraph direction before Arabic text drawn on a raw Canvas.
    private const val RTL_MARK = "\u200F"

    fun render(
        context: Context,
        name: String,
        certificateTitle: String,
        code: String,
        issuedAt: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(BrandIvory.toArgb())

        val outerFrame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BrandPlum.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 18f
        }
        canvas.drawRect(
            FRAME_INSET,
            FRAME_INSET,
            WIDTH - FRAME_INSET,
            HEIGHT - FRAME_INSET,
            outerFrame
        )
        val innerFrame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BrandRose600.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(
            FRAME_INSET + 22f,
            FRAME_INSET + 22f,
            WIDTH - FRAME_INSET - 22f,
            HEIGHT - FRAME_INSET - 22f,
            innerFrame
        )

        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.brand_logo)
        if (logo != null) {
            val scale = minOf(190f / logo.width, 190f / logo.height)
            val width = logo.width * scale
            val height = logo.height * scale
            canvas.drawBitmap(
                logo,
                null,
                RectF((WIDTH - width) / 2f, 108f, (WIDTH + width) / 2f, 108f + height),
                null
            )
            logo.recycle()
        }

        val bold = font(context, R.font.ibm_plex_sans_arabic_bold, Typeface.BOLD)
        val medium = font(context, R.font.ibm_plex_sans_arabic_medium, Typeface.BOLD)
        val regular = font(context, R.font.ibm_plex_sans_arabic_regular, Typeface.NORMAL)
        val centerX = WIDTH / 2f
        val date = issuedAt.take(10).ifBlank { "—" }

        drawCentered(canvas, rtl("شهادة إتمام"), centerX, 390f, textPaint(bold, 86f, BrandPlum700.toArgb()))
        drawCentered(canvas, rtl("تُمنح هذه الشهادة إلى"), centerX, 472f, textPaint(regular, 42f, TextMuted.toArgb()))
        drawCentered(canvas, rtl(name), centerX, 576f, textPaint(bold, 70f, BrandPlum.toArgb()))
        drawCentered(canvas, rtl("عن إتمام"), centerX, 652f, textPaint(regular, 40f, TextMuted.toArgb()))
        drawCentered(canvas, rtl(certificateTitle), centerX, 744f, textPaint(medium, 56f, TextPrimary.toArgb()))
        drawCentered(canvas, rtl("التاريخ: $date"), centerX, 852f, textPaint(regular, 40f, TextMuted.toArgb()))
        drawCentered(canvas, rtl("رمز الشهادة: $code"), centerX, 926f, textPaint(regular, 36f, BrandPlum700.toArgb()))
        drawCentered(canvas, rtl("حكاية تميّز · DXN"), centerX, 1022f, textPaint(bold, 42f, BrandPlum.toArgb()))
        return bitmap
    }

    private fun font(context: Context, fontRes: Int, style: Int): Typeface =
        ResourcesCompat.getFont(context, fontRes) ?: Typeface.create(Typeface.DEFAULT, style)

    private fun textPaint(typeface: Typeface, size: Float, color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        textSize = size
        this.color = color
        textLocale = Locale.forLanguageTag("ar")
    }

    private fun drawCentered(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint
    ) {
        paint.textAlign = Paint.Align.CENTER
        val measured = paint.measureText(text)
        if (measured > TEXT_MAX_WIDTH && measured > 0f) {
            paint.textSize *= TEXT_MAX_WIDTH / measured
        }
        canvas.drawText(text, x, y, paint)
    }

    private fun rtl(text: String) = RTL_MARK + text
}
