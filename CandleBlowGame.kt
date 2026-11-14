package com.example.ui.screens.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.ConfettiBurstOverlay
import kotlinx.coroutines.isActive
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private data class SmokePuff(
    var x: Float,
    var y: Float,
    var life: Float,
)

@Composable
fun CandleBlowGame(
    bestScore: Int,
    onGameFinished: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val candleCount = 7
    val litStates = remember { mutableStateListOf<Boolean>().apply { repeat(candleCount) { add(true) } } }
    val smokePuffs = remember { mutableStateListOf<SmokePuff>() }

    var isRunning by remember { mutableStateOf(true) }
    var score by remember { mutableIntStateOf(0) }
    var cakesCleared by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var runKey by remember { mutableIntStateOf(0) }
    var frameTick by remember { mutableLongStateOf(0L) }
    var burstTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(runKey) {
        litStates.clear()
        repeat(candleCount) { litStates.add(true) }
        smokePuffs.clear()
        score = 0
        cakesCleared = 0
        timeLeft = 30
        isRunning = true

        var lastNanos = 0L
        var elapsed = 0f
        var relightAccum = 0f

        while (isActive && isRunning) {
            withFrameNanos { nanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                lastNanos = nanos
                elapsed += dt
                relightAccum += dt

                val rem = (30 - elapsed.toInt()).coerceAtLeast(0)
                if (rem != timeLeft) timeLeft = rem
                if (rem <= 0) {
                    isRunning = false
                }

                // Mischievous spark occasionally relights a blown-out candle
                val relightInterval = (1.6f - cakesCleared * 0.15f).coerceAtLeast(0.75f)
                if (relightAccum >= relightInterval) {
                    relightAccum = 0f
                    val unlitIndices = litStates.indices.filter { !litStates[it] }
                    if (unlitIndices.isNotEmpty()) {
                        litStates[unlitIndices.random()] = true
                    }
                }

                val puffIter = smokePuffs.listIterator()
                while (puffIter.hasNext()) {
                    val puff = puffIter.next()
                    puff.life += dt
                    puff.y -= 75f * dt
                    if (puff.life >= 0.7f) puffIter.remove()
                }

                frameTick = nanos
            }
        }

        if (score > 0) burstTrigger++
        onGameFinished(score)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("candle_game_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Candle Blow-Out", style = MaterialTheme.typography.titleLarge)
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Best: $bestScore", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                GameStatPill("Score", "$score")
                GameStatPill("Cakes", "$cakesCleared")
                GameStatPill("Time", "${timeLeft}s")
            }

            Text(
                text = "Tap the flickering flames to blow them out before tricky sparks relight them!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF281224), Color(0xFF471E3B))
                        )
                    )
                    .pointerInput(isRunning) {
                        detectTapGestures { tap ->
                            if (!isRunning) return@detectTapGestures
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val cakeTopY = h * 0.56f
                            val candleWidth = w * 0.042f
                            val candleHeight = h * 0.14f
                            val startX = w * 0.2f
                            val spacing = (w * 0.6f) / (candleCount - 1)

                            for (i in 0 until candleCount) {
                                if (!litStates[i]) continue
                                val cx = startX + i * spacing
                                val flameY = cakeTopY - candleHeight - 24f
                                if (hypot(tap.x - cx, tap.y - flameY) <= (candleWidth * 2.2f).coerceAtLeast(62f)) {
                                    litStates[i] = false
                                    score += 2
                                    repeat(3) {
                                        smokePuffs.add(
                                            SmokePuff(
                                                x = cx + (Random.nextFloat() - 0.5f) * 12f,
                                                y = flameY,
                                                life = 0f,
                                            )
                                        )
                                    }
                                    if (litStates.none { it }) {
                                        cakesCleared++
                                        score += 15
                                        burstTrigger++
                                        repeat(candleCount) { idx -> litStates[idx] = true }
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
                        topLeft = Offset(w * 0.12f, h * 0.85f),
                        size = Size(w * 0.76f, h * 0.035f),
                        cornerRadius = CornerRadius(30f, 30f),
                    )
                    // Bottom cake tier
                    drawRoundRect(
                        color = Color(0xFFFF8FAB),
                        topLeft = Offset(w * 0.16f, h * 0.68f),
                        size = Size(w * 0.68f, h * 0.17f),
                        cornerRadius = CornerRadius(24f, 24f),
                    )
                    // Top cake tier
                    drawRoundRect(
                        color = Color(0xFFFFC2D4),
                        topLeft = Offset(w * 0.18f, h * 0.56f),
                        size = Size(w * 0.64f, h * 0.14f),
                        cornerRadius = CornerRadius(24f, 24f),
                    )
                    // Frosting wave
                    drawRoundRect(
                        color = Color(0xFFFFF8F0),
                        topLeft = Offset(w * 0.17f, h * 0.55f),
                        size = Size(w * 0.66f, h * 0.045f),
                        cornerRadius = CornerRadius(22f, 22f),
                    )

                    val cakeTopY = h * 0.56f
                    val candleWidth = w * 0.038f
                    val candleHeight = h * 0.135f
                    val startX = w * 0.2f
                    val spacing = (w * 0.6f) / (candleCount - 1)
                    val candleColors = listOf(
                        Color(0xFFF59E0B), Color(0xFF0D9488), Color(0xFFD81B60),
                        Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFFF97316), Color(0xFF3B82F6),
                    )

                    for (i in 0 until candleCount) {
                        val cx = startX + i * spacing
                        drawRoundRect(
                            color = candleColors[i % candleColors.size],
                            topLeft = Offset(cx - candleWidth / 2f, cakeTopY - candleHeight),
                            size = Size(candleWidth, candleHeight),
                            cornerRadius = CornerRadius(8f, 8f),
                        )
                        // Wick
                        drawLine(
                            color = Color(0xFF2D1622),
                            start = Offset(cx, cakeTopY - candleHeight),
                            end = Offset(cx, cakeTopY - candleHeight - 12f),
                            strokeWidth = 3f,
                        )

                        if (litStates[i]) {
                            val flicker = sin(timeSec * 14f + i * 1.4f) * 4f
                            val baseY = cakeTopY - candleHeight - 12f
                            // Halo glow
                            drawCircle(
                                color = Color(0x55FCD34D),
                                radius = candleWidth * 1.45f,
                                center = Offset(cx + flicker * 0.3f, baseY - 18f),
                            )
                            val flamePath = Path().apply {
                                moveTo(cx, baseY)
                                quadraticTo(cx - candleWidth * 0.65f, baseY - 18f, cx + flicker, baseY - 40f)
                                quadraticTo(cx + candleWidth * 0.65f, baseY - 18f, cx, baseY)
                                close()
                            }
                            drawPath(flamePath, color = Color(0xFFFFB300))
                            drawCircle(
                                color = Color(0xFFFFF59D),
                                radius = candleWidth * 0.26f,
                                center = Offset(cx + flicker * 0.2f, baseY - 14f),
                            )
                        }
                    }

                    smokePuffs.forEach { puff ->
                        val alpha = (1f - puff.life / 0.7f).coerceIn(0f, 1f) * 0.65f
                        drawCircle(
                            color = Color.White.copy(alpha = alpha),
                            radius = 12f + puff.life * 20f,
                            center = Offset(puff.x, puff.y),
                        )
                    }
                }

                if (!isRunning) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text("Wish Granted!", style = MaterialTheme.typography.headlineMedium)
                            Text(
                                text = "Score: $score • Cakes Cleared: $cakesCleared",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Button(
                                onClick = { runKey++ },
                                modifier = Modifier.testTag("candle_play_again"),
                                shape = RoundedCornerShape(50),
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play Again")
                            }
                        }
                    }
                }
            }
        }

        ConfettiBurstOverlay(burstTrigger = burstTrigger)
    }
}
