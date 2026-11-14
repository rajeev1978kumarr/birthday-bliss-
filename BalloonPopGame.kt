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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.ConfettiBurstOverlay
import com.example.ui.theme.ConfettiColors
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private enum class BalloonKind { NORMAL, GOLDEN, STORM }

private data class ArcadeBalloon(
    val id: Long,
    var xNorm: Float,
    var yNorm: Float,
    val speed: Float,
    val phase: Float,
    val radiusNorm: Float,
    val kind: BalloonKind,
    val color: Color,
)

private data class PopSpark(
    var xNorm: Float,
    var yNorm: Float,
    val vx: Float,
    val vy: Float,
    var life: Float,
    val color: Color,
)

@Composable
fun BalloonPopGame(
    bestScore: Int,
    onGameFinished: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val balloons = remember { mutableStateListOf<ArcadeBalloon>() }
    val sparks = remember { mutableStateListOf<PopSpark>() }
    var isRunning by remember { mutableStateOf(true) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var runKey by remember { mutableIntStateOf(0) }
    var frameTick by remember { mutableLongStateOf(0L) }
    var burstTrigger by remember { mutableIntStateOf(0) }

    fun spawnBalloon(nextId: Long): ArcadeBalloon {
        val roll = Random.nextFloat()
        val kind = when {
            roll < 0.14f -> BalloonKind.GOLDEN
            roll < 0.28f -> BalloonKind.STORM
            else -> BalloonKind.NORMAL
        }
        val color = when (kind) {
            BalloonKind.GOLDEN -> Color(0xFFF59E0B)
            BalloonKind.STORM -> Color(0xFF33293A)
            BalloonKind.NORMAL -> ConfettiColors.random()
        }
        return ArcadeBalloon(
            id = nextId,
            xNorm = 0.1f + Random.nextFloat() * 0.8f,
            yNorm = 1.08f + Random.nextFloat() * 0.12f,
            speed = when (kind) {
                BalloonKind.GOLDEN -> 0.28f + Random.nextFloat() * 0.1f
                BalloonKind.STORM -> 0.20f + Random.nextFloat() * 0.08f
                BalloonKind.NORMAL -> 0.18f + Random.nextFloat() * 0.1f
            },
            phase = Random.nextFloat() * 6.28f,
            radiusNorm = if (kind == BalloonKind.GOLDEN) 0.065f else 0.075f,
            kind = kind,
            color = color,
        )
    }

    LaunchedEffect(runKey) {
        balloons.clear()
        sparks.clear()
        score = 0
        combo = 0
        timeLeft = 30
        isRunning = true

        var nextId = 1L
        repeat(5) {
            balloons.add(spawnBalloon(nextId++).copy(yNorm = 0.55f + it * 0.1f))
        }

        var lastNanos = 0L
        var elapsedSec = 0f
        var spawnAccum = 0f

        while (isActive && isRunning) {
            withFrameNanos { nanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                lastNanos = nanos
                elapsedSec += dt
                spawnAccum += dt

                val remaining = (30 - elapsedSec.toInt()).coerceAtLeast(0)
                if (remaining != timeLeft) timeLeft = remaining
                if (remaining <= 0) {
                    isRunning = false
                }

                if (spawnAccum >= 0.55f && balloons.size < 11) {
                    spawnAccum = 0f
                    balloons.add(spawnBalloon(nextId++))
                }

                val bIter = balloons.listIterator()
                while (bIter.hasNext()) {
                    val b = bIter.next()
                    b.yNorm -= b.speed * dt
                    if (b.yNorm < -0.15f) bIter.remove()
                }

                val sIter = sparks.listIterator()
                while (sIter.hasNext()) {
                    val s = sIter.next()
                    s.life += dt
                    if (s.life >= 0.45f) {
                        sIter.remove()
                    } else {
                        s.xNorm += s.vx * dt
                        s.yNorm += s.vy * dt
                    }
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("balloon_game_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Balloon Pop Blitz", style = MaterialTheme.typography.titleLarge)
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

            // Scoreboard row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                GameStatPill("Score", "$score")
                GameStatPill("Combo", "${combo}x")
                GameStatPill("Time", "${timeLeft}s")
            }

            // Legend
            Text(
                text = "Tap colorful (+1) & golden (+5) balloons! Avoid dark storm balloons (-5).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            // Playfield
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFE0F7FA), Color(0xFFFFF3E9))
                        )
                    )
                    .pointerInput(isRunning) {
                        detectTapGestures { tap ->
                            if (!isRunning) return@detectTapGestures
                            val w = size.width.toFloat().coerceAtLeast(1f)
                            val h = size.height.toFloat().coerceAtLeast(1f)
                            val hitIdx = balloons.indexOfLast { b ->
                                val bx = (b.xNorm + sin(b.yNorm * 5f + b.phase) * 0.03f) * w
                                val by = b.yNorm * h
                                val br = b.radiusNorm * minOf(w, h) * 1.45f
                                hypot(tap.x - bx, tap.y - by) <= br
                            }
                            if (hitIdx >= 0) {
                                val popped = balloons.removeAt(hitIdx)
                                when (popped.kind) {
                                    BalloonKind.NORMAL -> {
                                        combo++
                                        score += 1 + (combo / 5)
                                    }
                                    BalloonKind.GOLDEN -> {
                                        combo++
                                        score += 5
                                    }
                                    BalloonKind.STORM -> {
                                        combo = 0
                                        score = (score - 5).coerceAtLeast(0)
                                    }
                                }
                                repeat(10) { i ->
                                    val ang = (2.0 * PI * i / 10).toFloat()
                                    sparks.add(
                                        PopSpark(
                                            xNorm = popped.xNorm,
                                            yNorm = popped.yNorm,
                                            vx = cos(ang) * 0.25f,
                                            vy = sin(ang) * 0.25f,
                                            life = 0f,
                                            color = popped.color,
                                        )
                                    )
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    frameTick.let { _ ->
                        val w = size.width
                        val h = size.height
                        balloons.forEach { b ->
                            val cx = (b.xNorm + sin(b.yNorm * 5f + b.phase) * 0.03f) * w
                            val cy = b.yNorm * h
                            val rx = b.radiusNorm * minOf(w, h)
                            val ry = rx * 1.22f

                            drawLine(
                                color = Color(0xFF7A6870),
                                start = Offset(cx, cy + ry),
                                end = Offset(cx, cy + ry + rx * 1.3f),
                                strokeWidth = 3f,
                            )
                            drawOval(
                                color = b.color,
                                topLeft = Offset(cx - rx, cy - ry),
                                size = Size(rx * 2f, ry * 2f),
                            )
                            if (b.kind == BalloonKind.STORM) {
                                // Lightning zig mark
                                drawCircle(
                                    color = Color(0xFFFFD54F),
                                    radius = rx * 0.22f,
                                    center = Offset(cx, cy),
                                )
                            } else {
                                drawOval(
                                    color = Color.White.copy(alpha = 0.45f),
                                    topLeft = Offset(cx - rx * 0.55f, cy - ry * 0.65f),
                                    size = Size(rx * 0.42f, ry * 0.52f),
                                )
                            }
                        }

                        sparks.forEach { s ->
                            val alpha = (1f - s.life / 0.45f).coerceIn(0f, 1f)
                            drawCircle(
                                color = s.color.copy(alpha = alpha),
                                radius = 7f * alpha,
                                center = Offset(s.xNorm * w, s.yNorm * h),
                            )
                        }
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
                            Text("Time's Up!", style = MaterialTheme.typography.headlineMedium)
                            Text(
                                text = "Final Score: $score",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Button(
                                onClick = { runKey++ },
                                modifier = Modifier.testTag("balloon_play_again"),
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

@Composable
internal fun GameStatPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
