package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CelebrationEffect
import com.example.ui.components.CelebrationStageCanvas
import com.example.ui.components.ConfettiBurstOverlay
import com.example.ui.theme.PacificoFontFamily
import com.example.util.BirthdayMusicPlayer
import kotlinx.coroutines.isActive
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private data class WishSmoke(
    var x: Float,
    var y: Float,
    var life: Float,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CelebrateScreen(
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val musicPlayer = remember { BirthdayMusicPlayer() }

    var activeEffects by remember {
        mutableStateOf(setOf(CelebrationEffect.CONFETTI, CelebrationEffect.BALLOONS))
    }
    var poppedCounter by remember { mutableIntStateOf(0) }
    var burstTrigger by remember { mutableIntStateOf(0) }
    var isPlayingMusic by remember { mutableStateOf(false) }
    var wishText by remember { mutableStateOf("") }
    var wishGrantedBanner by remember { mutableStateOf(false) }

    val candleCount = 5
    val candlesLit = remember { mutableStateListOf(true, true, true, true, true) }
    val smokePuffs = remember { mutableStateListOf<WishSmoke>() }
    var frameTick by remember { mutableLongStateOf(0L) }

    DisposableEffect(Unit) {
        onDispose { musicPlayer.stop() }
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (isActive) {
            withFrameNanos { nanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                lastNanos = nanos
                val iter = smokePuffs.listIterator()
                while (iter.hasNext()) {
                    val puff = iter.next()
                    puff.life += dt
                    puff.y -= 60f * dt
                    if (puff.life >= 0.85f) iter.remove()
                }
                frameTick = nanos
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fullscreen interactive celebration particles
        CelebrationStageCanvas(
            activeEffects = activeEffects,
            allowBalloonPop = true,
            onBalloonPopped = { poppedCounter++ },
            modifier = Modifier.matchParentSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Celebration Stage",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Mix confetti, fireworks, tappable balloons & birthday wishes!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (poppedCounter > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = "Popped: $poppedCounter",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            // Animation Layer Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Animation Effects",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        CelebrationEffect.entries.forEach { effect ->
                            val selected = effect in activeEffects
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    activeEffects = if (selected) activeEffects - effect else activeEffects + effect
                                },
                                label = { Text(effect.label) },
                                modifier = Modifier.testTag("effect_chip_${effect.name.lowercase()}"),
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilledTonalButton(
                            onClick = {
                                activeEffects = CelebrationEffect.entries.toSet()
                                burstTrigger++
                            },
                            modifier = Modifier.testTag("all_effects_button"),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("All Effects!")
                        }
                        OutlinedButton(
                            onClick = {
                                if (isPlayingMusic) {
                                    musicPlayer.stop()
                                    isPlayingMusic = false
                                } else {
                                    isPlayingMusic = true
                                    musicPlayer.playHappyBirthday(scope) {
                                        isPlayingMusic = false
                                    }
                                }
                            },
                            modifier = Modifier.testTag("celebrate_music_button"),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(
                                imageVector = if (isPlayingMusic) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isPlayingMusic) stringResource(R.string.action_stop_chime)
                                else stringResource(R.string.action_play_chime)
                            )
                        }
                    }
                }
            }

            // Interactive Make-A-Wish Cake
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Make a Birthday Wish",
                        fontFamily = PacificoFontFamily,
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Tap any candle on the cake to light or blow it out!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { tap ->
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val cakeTopY = h * 0.50f
                                    val candleH = h * 0.18f
                                    val startX = w * 0.24f
                                    val spacing = (w * 0.52f) / (candleCount - 1)
                                    for (i in 0 until candleCount) {
                                        val cx = startX + i * spacing
                                        val cy = cakeTopY - candleH * 0.8f
                                        if (hypot(tap.x - cx, tap.y - cy) <= 56f) {
                                            val wasLit = candlesLit[i]
                                            candlesLit[i] = !wasLit
                                            if (wasLit) {
                                                repeat(3) {
                                                    smokePuffs.add(
                                                        WishSmoke(
                                                            x = cx + (Random.nextFloat() - 0.5f) * 10f,
                                                            y = cakeTopY - candleH - 10f,
                                                            life = 0f,
                                                        )
                                                    )
                                                }
                                            }
                                            break
                                        }
                                    }
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val timeSec = (frameTick % 1_000_000_000_000L) / 1_000_000_000f
                            val w = size.width
                            val h = size.height

                            // Plate
                            drawRoundRect(
                                color = Color(0xFFFFE4EC),
                                topLeft = Offset(w * 0.14f, h * 0.86f),
                                size = Size(w * 0.72f, h * 0.05f),
                                cornerRadius = CornerRadius(24f, 24f),
                            )
                            // Bottom tier
                            drawRoundRect(
                                color = Color(0xFFD81B60),
                                topLeft = Offset(w * 0.18f, h * 0.66f),
                                size = Size(w * 0.64f, h * 0.20f),
                                cornerRadius = CornerRadius(22f, 22f),
                            )
                            // Top tier
                            drawRoundRect(
                                color = Color(0xFFFF8FAB),
                                topLeft = Offset(w * 0.21f, h * 0.50f),
                                size = Size(w * 0.58f, h * 0.17f),
                                cornerRadius = CornerRadius(22f, 22f),
                            )
                            // Cream icing
                            drawRoundRect(
                                color = Color(0xFFFFF8F0),
                                topLeft = Offset(w * 0.20f, h * 0.49f),
                                size = Size(w * 0.60f, h * 0.055f),
                                cornerRadius = CornerRadius(20f, 20f),
                            )

                            val cakeTopY = h * 0.50f
                            val candleW = w * 0.036f
                            val candleH = h * 0.18f
                            val startX = w * 0.24f
                            val spacing = (w * 0.52f) / (candleCount - 1)
                            val colors = listOf(
                                Color(0xFFF59E0B), Color(0xFF0D9488), Color(0xFF8B5CF6),
                                Color(0xFFEC4899), Color(0xFF3B82F6),
                            )

                            for (i in 0 until candleCount) {
                                val cx = startX + i * spacing
                                drawRoundRect(
                                    color = colors[i % colors.size],
                                    topLeft = Offset(cx - candleW / 2f, cakeTopY - candleH),
                                    size = Size(candleW, candleH),
                                    cornerRadius = CornerRadius(8f, 8f),
                                )
                                drawLine(
                                    color = Color(0xFF28161F),
                                    start = Offset(cx, cakeTopY - candleH),
                                    end = Offset(cx, cakeTopY - candleH - 10f),
                                    strokeWidth = 3f,
                                )
                                if (candlesLit[i]) {
                                    val flicker = sin(timeSec * 12f + i * 1.5f) * 3.5f
                                    val baseY = cakeTopY - candleH - 10f
                                    drawCircle(
                                        color = Color(0x55FCD34D),
                                        radius = candleW * 1.4f,
                                        center = Offset(cx + flicker * 0.25f, baseY - 14f),
                                    )
                                    val flamePath = Path().apply {
                                        moveTo(cx, baseY)
                                        quadraticTo(cx - candleW * 0.6f, baseY - 14f, cx + flicker, baseY - 34f)
                                        quadraticTo(cx + candleW * 0.6f, baseY - 14f, cx, baseY)
                                        close()
                                    }
                                    drawPath(flamePath, color = Color(0xFFFFB300))
                                    drawCircle(
                                        color = Color(0xFFFFF59D),
                                        radius = candleW * 0.25f,
                                        center = Offset(cx + flicker * 0.15f, baseY - 12f),
                                    )
                                }
                            }

                            smokePuffs.forEach { puff ->
                                val alpha = (1f - puff.life / 0.85f).coerceIn(0f, 1f) * 0.6f
                                drawCircle(
                                    color = Color(0xFF8C7A84).copy(alpha = alpha),
                                    radius = 10f + puff.life * 16f,
                                    center = Offset(puff.x, puff.y),
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = wishText,
                        onValueChange = { wishText = it },
                        label = { Text("Secret Birthday Wish (Optional)") },
                        placeholder = { Text("Type your wish before blowing the candles...") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wish_cake_input"),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                for (i in 0 until candleCount) {
                                    candlesLit[i] = false
                                }
                                activeEffects = activeEffects + CelebrationEffect.CONFETTI + CelebrationEffect.FIREWORKS
                                burstTrigger++
                                wishGrantedBanner = true
                            },
                            modifier = Modifier.testTag("blow_candles_celebrate_button"),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Blow Candles & Celebrate!")
                        }
                        OutlinedButton(
                            onClick = {
                                for (i in 0 until candleCount) candlesLit[i] = true
                                wishGrantedBanner = false
                            },
                            modifier = Modifier.testTag("relight_candles_button"),
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Relight")
                        }
                    }

                    AnimatedVisibility(visible = wishGrantedBanner) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Text(
                                text = if (wishText.isNotBlank()) {
                                    "Your wish \"$wishText\" is soaring with the confetti!"
                                } else {
                                    "Candles blown! May all your birthday dreams come true!"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }

        ConfettiBurstOverlay(burstTrigger = burstTrigger)
    }
}
