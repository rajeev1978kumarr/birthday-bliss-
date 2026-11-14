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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
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

private data class CandyDrop(
    var xNorm: Float,
    var yNorm: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val color: Color,
)

@Composable
fun PinataSmashGame(
    bestScore: Int,
    onGameFinished: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val candies = remember { mutableStateListOf<CandyDrop>() }
    var isRunning by remember { mutableStateOf(true) }
    var score by remember { mutableIntStateOf(0) }
    var smashedCount by remember { mutableIntStateOf(0) }
    var hitsOnCurrent by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(20) }
    var runKey by remember { mutableIntStateOf(0) }
    var swingImpulse by remember { mutableFloatStateOf(0f) }
    var frameTick by remember { mutableLongStateOf(0L) }
    var burstTrigger by remember { mutableIntStateOf(0) }

    val hitsToSmash = 10

    LaunchedEffect(runKey) {
        candies.clear()
        score = 0
        smashedCount = 0
        hitsOnCurrent = 0
        timeLeft = 20
        swingImpulse = 0f
        isRunning = true

        var lastNanos = 0L
        var elapsed = 0f
        while (isActive && isRunning) {
            withFrameNanos { nanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                lastNanos = nanos
                elapsed += dt

                val rem = (20 - elapsed.toInt()).coerceAtLeast(0)
                if (rem != timeLeft) timeLeft = rem
                if (rem <= 0) isRunning = false

                swingImpulse *= (1f - 3.2f * dt).coerceIn(0f, 1f)

                val iter = candies.listIterator()
                while (iter.hasNext()) {
                    val c = iter.next()
                    c.life += dt
                    c.xNorm += c.vx * dt
                    c.yNorm += c.vy * dt
                    c.vy += 0.7f * dt
                    if (c.life > 1.2f || c.yNorm > 1.1f) iter.remove()
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
                    IconButton(onClick = onBack, modifier = Modifier.testTag("pinata_game_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Pinata Smash", style = MaterialTheme.typography.titleLarge)
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
                GameStatPill("Smashed", "$smashedCount")
                GameStatPill("Time", "${timeLeft}s")
            }

            Text(
                text = "Tap the swinging star pinata! Every $hitsToSmash hits bursts candy for +15 bonus!",
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
                            listOf(Color(0xFFFFF0DB), Color(0xFFFFDCE8))
                        )
                    )
                    .pointerInput(isRunning) {
                        detectTapGestures { tap ->
                            if (!isRunning) return@detectTapGestures
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val timeSec = (frameTick % 1_000_000_000_000L) / 1_000_000_000f
                            val angle = sin(timeSec * 2.4f) * 0.25f + swingImpulse
                            val ropeLen = h * 0.34f
                            val px = w * 0.5f + sin(angle) * ropeLen
                            val py = cos(angle) * ropeLen + h * 0.14f

                            if (hypot(tap.x - px, tap.y - py) <= minOf(w, h) * 0.25f) {
                                score += 2
                                hitsOnCurrent++
                                swingImpulse = (if (Random.nextBoolean()) 1f else -1f) * 0.35f
                                if (hitsOnCurrent >= hitsToSmash) {
                                    hitsOnCurrent = 0
                                    smashedCount++
                                    score += 15
                                    burstTrigger++
                                    repeat(24) {
                                        val a = Random.nextFloat() * PI.toFloat() * 2f
                                        val sp = 0.2f + Random.nextFloat() * 0.45f
                                        candies.add(
                                            CandyDrop(
                                                xNorm = px / w,
                                                yNorm = py / h,
                                                vx = cos(a) * sp,
                                                vy = sin(a) * sp - 0.25f,
                                                life = 0f,
                                                color = ConfettiColors.random(),
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val timeSec = (frameTick % 1_000_000_000_000L) / 1_000_000_000f
                    val w = size.width
                    val h = size.height
                    val angle = sin(timeSec * 2.4f) * 0.25f + swingImpulse
                    val anchor = Offset(w * 0.5f, 0f)
                    val ropeLen = h * 0.34f
                    val px = w * 0.5f + sin(angle) * ropeLen
                    val py = cos(angle) * ropeLen + h * 0.14f

                    // Rope
                    drawLine(
                        color = Color(0xFF7A4E2D),
                        start = anchor,
                        end = Offset(px, py),
                        strokeWidth = 6f,
                    )

                    // Pinata Star Body
                    val outerR = minOf(w, h) * 0.15f
                    val innerR = outerR * 0.52f
                    val starPath = Path().apply {
                        for (i in 0 until 10) {
                            val r = if (i % 2 == 0) outerR else innerR
                            val a = (PI / 5.0 * i - PI / 2.0).toFloat()
                            val sx = px + cos(a) * r
                            val sy = py + sin(a) * r
                            if (i == 0) moveTo(sx, sy) else lineTo(sx, sy)
                        }
                        close()
                    }
                    rotate(degrees = Math.toDegrees(angle.toDouble()).toFloat(), pivot = Offset(px, py)) {
                        drawPath(starPath, color = Color(0xFFD81B60))
                        drawCircle(color = Color(0xFFF59E0B), radius = innerR * 0.72f, center = Offset(px, py))
                        drawCircle(color = Color(0xFF0D9488), radius = innerR * 0.36f, center = Offset(px, py))
                    }

                    // Candies
                    candies.forEach { c ->
                        drawCircle(
                            color = c.color,
                            radius = 12f,
                            center = Offset(c.xNorm * w, c.yNorm * h),
                        )
                    }
                }

                // Hit progress pill
                if (isRunning) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.88f),
                    ) {
                        Text(
                            text = "Hits: $hitsOnCurrent / $hitsToSmash",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF28161F),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        )
                    }
                } else {
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
                            Text("Candy Shower!", style = MaterialTheme.typography.headlineMedium)
                            Text(
                                text = "Score: $score • Pinatas Smashed: $smashedCount",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Button(
                                onClick = { runKey++ },
                                modifier = Modifier.testTag("pinata_play_again"),
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
