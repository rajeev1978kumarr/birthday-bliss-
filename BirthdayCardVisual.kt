package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.BirthdayCard
import com.example.data.model.PhotoFrameShape
import com.example.ui.theme.PacificoFontFamily
import com.example.ui.theme.PlayfairFontFamily
import java.io.File

@Composable
fun BirthdayCardVisual(
    card: BirthdayCard,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onPhotoSlotClick: (() -> Unit)? = null,
) {
    val theme = card.resolveTheme()
    val sticker = card.resolveSticker()
    val frameShape = card.resolveFrameShape()
    val hasPhoto = !card.photoPath.isNullOrBlank() && File(card.photoPath).exists()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (!compact) Modifier.aspectRatio(0.78f) else Modifier),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = if (compact) 4.dp else 10.dp,
        border = BorderStroke(1.5.dp, theme.accentColor.copy(alpha = 0.28f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(theme.gradientStart, theme.gradientEnd)
                    )
                )
        ) {
            // Optional bundled decorative stationery background
            theme.backgroundDrawable?.let { drawableId ->
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = 0.88f,
                    modifier = Modifier.matchParentSize(),
                )
                // Soft readability veil
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(if (compact) 14.dp else 22.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xD9FFF9F5))
                )
            }

            // Subtle Canvas festive dots
            Canvas(modifier = Modifier.matchParentSize()) {
                val dotColors = listOf(
                    theme.accentColor.copy(alpha = 0.35f),
                    Color(0xFFF59E0B).copy(alpha = 0.35f),
                    Color(0xFF0D9488).copy(alpha = 0.3f),
                )
                val anchors = listOf(
                    0.12f to 0.14f, 0.88f to 0.16f, 0.14f to 0.86f,
                    0.86f to 0.84f, 0.22f to 0.28f, 0.79f to 0.32f,
                )
                anchors.forEachIndexed { idx, (ax, ay) ->
                    drawCircle(
                        color = dotColors[idx % dotColors.size],
                        radius = (if (compact) 4.dp else 6.dp).toPx(),
                        center = Offset(size.width * ax, size.height * ay),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (compact) 18.dp else 26.dp, vertical = if (compact) 18.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = sticker.emoji,
                    fontSize = if (compact) 28.sp else 36.sp,
                )

                if (card.milestone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = theme.accentColor,
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = card.milestone.uppercase(),
                            color = if (theme.isDarkCard) Color(0xFF28121E) else Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }

                if (hasPhoto) {
                    Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))
                    FramedUploadedPhoto(
                        photoPath = card.photoPath!!,
                        shape = frameShape,
                        accentColor = theme.accentColor,
                        sizeDp = if (compact) 86 else 132,
                    )
                } else if (onPhotoSlotClick != null && !compact) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        onClick = onPhotoSlotClick,
                        shape = CircleShape,
                        color = theme.accentColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.5.dp, theme.accentColor.copy(alpha = 0.5f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("📸", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap to Upload Photo",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = theme.accentColor,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (compact) 10.dp else 16.dp))

                Text(
                    text = card.greetingTitle.ifBlank { "Happy Birthday!" },
                    fontFamily = PacificoFontFamily,
                    fontSize = if (compact) 22.sp else 28.sp,
                    lineHeight = if (compact) 28.sp else 34.sp,
                    color = theme.accentColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (card.recipientName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dear ${card.recipientName}",
                        fontFamily = PlayfairFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 17.sp else 21.sp,
                        color = theme.textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))

                Text(
                    text = card.message.ifBlank { "Wishing you endless joy, bright moments, and a truly wonderful year ahead!" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = if (compact) 13.sp else 15.sp,
                        lineHeight = if (compact) 19.sp else 22.sp,
                    ),
                    color = theme.textColor.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    maxLines = if (compact) 3 else 5,
                    overflow = TextOverflow.Ellipsis,
                )

                if (card.senderName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(if (compact) 10.dp else 16.dp))
                    Text(
                        text = "With love, ${card.senderName}",
                        fontFamily = PacificoFontFamily,
                        fontSize = if (compact) 14.sp else 17.sp,
                        color = theme.subtitleColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun FramedUploadedPhoto(
    photoPath: String,
    shape: PhotoFrameShape,
    accentColor: Color,
    sizeDp: Int,
) {
    val context = LocalContext.current
    val request = ImageRequest.Builder(context)
        .data(File(photoPath))
        .crossfade(true)
        .build()

    when (shape) {
        PhotoFrameShape.CIRCLE -> {
            Box(
                modifier = Modifier
                    .size(sizeDp.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, accentColor, CircleShape)
                    .padding(4.dp)
            ) {
                AsyncImage(
                    model = request,
                    contentDescription = "Birthday photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            }
        }
        PhotoFrameShape.POLAROID -> {
            Surface(
                modifier = Modifier
                    .rotate(-2.5f)
                    .shadow(8.dp, RoundedCornerShape(10.dp)),
                color = Color.White,
                shape = RoundedCornerShape(10.dp),
            ) {
                Column(
                    modifier = Modifier.padding(start = 7.dp, end = 7.dp, top = 7.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AsyncImage(
                        model = request,
                        contentDescription = "Birthday photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(sizeDp.dp)
                            .clip(RoundedCornerShape(6.dp)),
                    )
                }
            }
        }
        PhotoFrameShape.ARCH -> {
            val archShape = RoundedCornerShape(
                topStartPercent = 50,
                topEndPercent = 50,
                bottomStartPercent = 10,
                bottomEndPercent = 10,
            )
            Box(
                modifier = Modifier
                    .size(width = sizeDp.dp, height = (sizeDp * 1.12f).dp)
                    .shadow(6.dp, archShape)
                    .clip(archShape)
                    .background(Color.White)
                    .border(2.5.dp, accentColor.copy(alpha = 0.6f), archShape)
                    .padding(4.dp)
            ) {
                AsyncImage(
                    model = request,
                    contentDescription = "Birthday photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(archShape),
                )
            }
        }
    }
}
