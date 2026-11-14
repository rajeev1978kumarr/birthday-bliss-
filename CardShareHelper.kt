package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.BirthdayCard
import com.example.data.model.PhotoFrameShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object CardShareHelper {
    suspend fun shareCard(context: Context, card: BirthdayCard): Boolean {
        val shareUri = withContext(Dispatchers.IO) {
            runCatching {
                val bitmap = renderCardBitmap(context, card)
                val dir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
                val file = File(dir, "birthday_bliss_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                }
                bitmap.recycle()
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
            }.getOrNull()
        } ?: return false

        val shareText = buildString {
            append(card.greetingTitle.ifBlank { "Happy Birthday!" })
            if (card.recipientName.isNotBlank()) {
                append(" ${card.recipientName}!")
            }
            append("\n\n")
            append(card.message)
            if (card.senderName.isNotBlank()) {
                append("\n— ${card.senderName}")
            }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.share_chooser_title))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        return true
    }

    private fun renderCardBitmap(context: Context, card: BirthdayCard): Bitmap {
        val width = 1080
        val height = 1440
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val theme = card.resolveTheme()

        // 1. Background gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                theme.gradientStart.toArgb(),
                theme.gradientEnd.toArgb(),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Optional decorative template image
        theme.backgroundDrawable?.let { resId ->
            BitmapFactory.decodeResource(context.resources, resId)?.let { bgBmp ->
                val dest = RectF(0f, 0f, width.toFloat(), height.toFloat())
                val srcRatio = bgBmp.width.toFloat() / bgBmp.height
                val dstRatio = width.toFloat() / height
                val drawRect = if (srcRatio > dstRatio) {
                    val scaledW = height * srcRatio
                    val left = (width - scaledW) / 2f
                    RectF(left, 0f, left + scaledW, height.toFloat())
                } else {
                    val scaledH = width / srcRatio
                    val top = (height - scaledH) / 2f
                    RectF(0f, top, width.toFloat(), top + scaledH)
                }
                val imgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 215 }
                canvas.drawBitmap(bgBmp, null, drawRect, imgPaint)
                bgBmp.recycle()

                // Soft readability veil in center
                val veilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.argb(145, 255, 250, 245)
                }
                canvas.drawRoundRect(
                    RectF(96f, 120f, width - 96f, height - 120f),
                    56f, 56f, veilPaint
                )
            }
        }

        // 3. Decorative confetti dots
        val dotColors = listOf(
            theme.accentColor.toArgb(),
            android.graphics.Color.parseColor("#F59E0B"),
            android.graphics.Color.parseColor("#0D9488"),
            android.graphics.Color.parseColor("#FF6B6B"),
        )
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val positions = listOf(
            130f to 140f, 940f to 160f, 180f to 1280f, 900f to 1250f,
            220f to 240f, 860f to 280f, 140f to 700f, 940f to 760f,
        )
        positions.forEachIndexed { idx, (dx, dy) ->
            dotPaint.color = dotColors[idx % dotColors.size]
            dotPaint.alpha = 180
            canvas.drawCircle(dx, dy, 12f + (idx % 3) * 5f, dotPaint)
        }

        // 4. Inner border frame
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            color = theme.accentColor.toArgb()
            alpha = 120
        }
        canvas.drawRoundRect(
            RectF(64f, 64f, width - 64f, height - 64f),
            48f, 48f, borderPaint
        )

        val pacifico = ResourcesCompat.getFont(context, R.font.pacifico) ?: Typeface.SERIF
        val playfair = ResourcesCompat.getFont(context, R.font.playfair_display) ?: Typeface.SERIF
        val jakarta = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans) ?: Typeface.SANS_SERIF

        var currentY = 150f

        // 5. Sticker + Milestone header
        val stickerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 72f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(card.resolveSticker().emoji, width / 2f, currentY, stickerPaint)
        currentY += 64f

        if (card.milestone.isNotBlank()) {
            val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.accentColor.toArgb()
            }
            val pillTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(jakarta, Typeface.BOLD)
                textSize = 34f
                color = if (theme.isDarkCard) android.graphics.Color.parseColor("#24121C")
                else android.graphics.Color.WHITE
                textAlign = Paint.Align.CENTER
            }
            val textW = pillTextPaint.measureText(card.milestone.uppercase())
            val pillRect = RectF(
                width / 2f - textW / 2f - 44f,
                currentY,
                width / 2f + textW / 2f + 44f,
                currentY + 62f,
            )
            canvas.drawRoundRect(pillRect, 31f, 31f, pillPaint)
            canvas.drawText(card.milestone.uppercase(), width / 2f, currentY + 43f, pillTextPaint)
            currentY += 96f
        }

        // 6. Uploaded photo (if present)
        val photoBmp = card.photoPath?.let { path ->
            runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
        }
        if (photoBmp != null) {
            val frameSize = 360f
            val left = (width - frameSize) / 2f
            val top = currentY + 12f
            drawFramedPhoto(
                canvas = canvas,
                bitmap = photoBmp,
                rect = RectF(left, top, left + frameSize, top + frameSize),
                shape = card.resolveFrameShape(),
                accentColor = theme.accentColor.toArgb(),
            )
            photoBmp.recycle()
            currentY = top + frameSize + 64f
        } else {
            currentY += 36f
        }

        // 7. Greeting title (Pacifico)
        val greetingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = pacifico
            textSize = 78f
            color = theme.accentColor.toArgb()
            textAlign = Paint.Align.CENTER
        }
        val greetingText = card.greetingTitle.ifBlank { "Happy Birthday!" }
        canvas.drawText(greetingText, width / 2f, currentY, greetingPaint)
        currentY += 78f

        // 8. Recipient name (Playfair Display)
        if (card.recipientName.isNotBlank()) {
            val recipientPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(playfair, Typeface.BOLD)
                textSize = 54f
                color = theme.textColor.toArgb()
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Dear ${card.recipientName}", width / 2f, currentY, recipientPaint)
            currentY += 68f
        }

        // 9. Wish message (StaticLayout)
        val msgPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = jakarta
            textSize = 36f
            color = theme.textColor.toArgb()
        }
        val textWidth = width - 240
        val layout = StaticLayout.Builder
            .obtain(card.message, 0, card.message.length, msgPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(12f, 1f)
            .build()
        canvas.save()
        canvas.translate(120f, currentY)
        layout.draw(canvas)
        canvas.restore()
        currentY += layout.height + 64f

        // 10. Sender signature
        if (card.senderName.isNotBlank()) {
            val senderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = pacifico
                textSize = 44f
                color = theme.subtitleColor.toArgb()
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("With love, ${card.senderName}", width / 2f, currentY.coerceAtMost(height - 150f), senderPaint)
        }

        // 11. Subtle Birthday Bliss footer
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = jakarta
            textSize = 24f
            color = theme.subtitleColor.toArgb()
            alpha = 165
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Birthday Bliss", width / 2f, height - 92f, footerPaint)

        return bitmap
    }

    private fun drawFramedPhoto(
        canvas: Canvas,
        bitmap: Bitmap,
        rect: RectF,
        shape: PhotoFrameShape,
        accentColor: Int,
    ) {
        val centerCropMatrix = Matrix().apply {
            val scale = maxOf(rect.width() / bitmap.width, rect.height() / bitmap.height)
            val dx = rect.left + (rect.width() - bitmap.width * scale) / 2f
            val dy = rect.top + (rect.height() - bitmap.height * scale) / 2f
            setScale(scale, scale)
            postTranslate(dx, dy)
        }
        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
            setLocalMatrix(centerCropMatrix)
        }
        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.shader = shader
        }

        when (shape) {
            PhotoFrameShape.CIRCLE -> {
                val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    style = Paint.Style.FILL
                }
                val cx = rect.centerX()
                val cy = rect.centerY()
                val r = rect.width() / 2f
                canvas.drawCircle(cx, cy, r + 14f, ringPaint)
                canvas.drawCircle(cx, cy, r, photoPaint)
                val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    style = Paint.Style.STROKE
                    strokeWidth = 6f
                }
                canvas.drawCircle(cx, cy, r + 14f, border)
            }
            PhotoFrameShape.POLAROID -> {
                val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    setShadowLayer(18f, 0f, 8f, android.graphics.Color.argb(55, 0, 0, 0))
                }
                val outer = RectF(rect.left - 22f, rect.top - 22f, rect.right + 22f, rect.bottom + 62f)
                canvas.drawRoundRect(outer, 20f, 20f, cardPaint)
                canvas.drawRoundRect(rect, 12f, 12f, photoPaint)
            }
            PhotoFrameShape.ARCH -> {
                val path = Path().apply {
                    val radius = rect.width() / 2f
                    addArc(RectF(rect.left, rect.top, rect.right, rect.top + rect.width()), 180f, 180f)
                    lineTo(rect.right, rect.bottom)
                    lineTo(rect.left, rect.bottom)
                    close()
                }
                val framePath = Path().apply {
                    val pad = 12f
                    val outer = RectF(rect.left - pad, rect.top - pad, rect.right + pad, rect.bottom + pad)
                    addArc(RectF(outer.left, outer.top, outer.right, outer.top + outer.width()), 180f, 180f)
                    lineTo(outer.right, outer.bottom)
                    lineTo(outer.left, outer.bottom)
                    close()
                }
                val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
                canvas.drawPath(framePath, whitePaint)
                canvas.drawPath(path, photoPaint)
            }
        }
    }
}
