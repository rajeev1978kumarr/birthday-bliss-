package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ConfettiColors
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class CelebrationEffect(val label: String) {
    CONFETTI("Confetti"),
    FIREWORKS("Fireworks"),
    BALLOONS("Balloons"),
    SPARKLES("Sparkles"),
}

private data class ConfettiPiece(
    var xNorm: Float,
    var yNorm: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var vRot: Float,
    var sizeDp: Float,
    val color: Color,
    val isRibbon: Boolean,
)

private data class FireworkSpark(
    var xNorm: Float,
    var yNorm: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val color: Color,
    val radius: Float,
)

private data class FloatingBalloon(
    val id: Int,
    var xNorm: Float,
    var yNorm: Float,
    val speed: Float,
    val phase: Float,
    val radiusNorm: Float,
    val color: Color,
)

private data class TwinkleStar(
    var xNorm: Float,
    var yNorm: Float,
    val speedY: Float,
    val phase: Float,
    val baseSize: Float,
    val color: Color,
)

@Composable
fun CelebrationStageCanvas(
    activeEffects: Set<CelebrationEffect>,
    modifier: Modifier = Modifier,
    allowBalloonPop: Boolean = false,
    onBalloonPopped: () -> Unit = {},
) {
    val confetti = remember { mutableStateListOf<ConfettiPiece>() }
    val sparks = remember { mutableStateListOf<FireworkSpark>() }
    val balloons = remember { mutableStateListOf<FloatingBalloon>() }
    val stars = remember { mutableStateListOf<TwinkleStar>() }
    var frameTick by remember { mutableLongStateOf(0L) }
    var nextBalloonId by remember { mutableLongStateOf(1L) }

    fun spawnConfetti(count: Int) {
        repeat(count) {
            confetti.add(
                ConfettiPiece(
                    xNorm = Random.nextFloat(),
                    yNorm = Random.nextFloat() * -0.4f,
                    vx = (Random.nextFloat() - 0.5f) * 0.25f,
                    vy = 0.28f + Random.nextFloat() * 0.35f,
                    rotation = Random.nextFloat() * 360f,
                    vRot = (Random.nextFloat() - 0.5f) * 260f,
                    sizeDp = 7f + Random.nextFloat() * 7f,
                    color = ConfettiColors.random(),
                    isRibbon = Random.nextBoolean(),
                )
            )
        }
    }

    fun spawnFireworkBurst() {
        val cx = 0.15f + Random.nextFloat() * 0.7f
        val cy = 0.12f + Random.nextFloat() * 0.45f
        val burstColor = ConfettiColors.random()
        val secondaryColor = Color(0xFFFCD34D)
        val count = 34
        for (i in 0 until count) {
            val angle = (2.0 * PI * i / count).toFloat() + Random.nextFloat() * 0.15f
            val speed = 0.18f + Random.nextFloat() * 0.28f
            sparks.add(
                FireworkSpark(
                    xNorm = cx,
                    yNorm = cy,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = 0f,
                    maxLife = 0.9f + Random.nextFloat() * 0.6f,
                    color = if (i % 3 == 0) secondaryColor else burstColor,
                    radius = 3.5f + Random.nextFloat() * 3.5f,
                )
            )
        }
    }

    LaunchedEffect(activeEffects) {
        if (CelebrationEffect.CONFETTI in activeEffects && confetti.isEmpty()) {
            spawnConfetti(65)
        } else if (CelebrationEffect.CONFETTI !in activeEffects) {
            confetti.clear()
        }

        if (CelebrationEffect.BALLOONS in activeEffects && balloons.isEmpty()) {
            repeat(12) {
                balloons.add(
                    FloatingBalloon(
                        id = (nextBalloonId++).toInt(),
                        xNorm = 0.08f + Random.nextFloat() * 0.84f,
                        yNorm = 0.2f + Random.nextFloat() * 0.95f,
                        speed = 0.08f + Random.nextFloat() * 0.08f,
                        phase = Random.nextFloat() * 6.28f,
                        radiusNorm = 0.055f + Random.nextFloat() * 0.025f,
                        color = ConfettiColors.random(),
                    )
                )
            }
        } else if (CelebrationEffect.BALLOONS !in activeEffects) {
            balloons.clear()
        }

        if (CelebrationEffect.SPARKLES in activeEffects && stars.isEmpty()) {
            repeat(38) {
                stars.add(
                    TwinkleStar(
                        xNorm = Random.nextFloat(),
                        yNorm = Random.nextFloat(),
                        speedY = 0.03f + Random.nextFloat() * 0.05f,
                        phase = Random.nextFloat() * 6.28f,
                        baseSize = 6f + Random.nextFloat() * 10f,
                        color = if (it % 2 == 0) Color(0xFFF59E0B) else Color(0xFFEC4899),
                    )
                )
            }
        } else if (CelebrationEffect.SPARKLES !in activeEffects) {
            stars.clear()
        }

        if (CelebrationEffect.FIREWORKS !in activeEffects) {
            sparks.clear()
        }

        if (activeEffects.isEmpty()) return@LaunchedEffect

        var lastNanos = 0L
        var fireworkTimer = 0.6f
        while (isActive) {
            withFrameNanos { nanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                lastNanos = nanos

                // Update confetti
                if (CelebrationEffect.CONFETTI in activeEffects) {
                    confetti.forEach { piece ->
                        piece.xNorm += piece.vx * dt + sin(piece.yNorm * 8f) * 0.04f * dt
                        piece.yNorm += piece.vy * dt
                        piece.rotation += piece.vRot * dt
                        if (piece.yNorm > 1.08f) {
                            piece.yNorm = -0.08f
                            piece.xNorm = Random.nextFloat()
                        }
                    }
                }

                // Update fireworks
                if (CelebrationEffect.FIREWORKS in activeEffects) {
                    fireworkTimer += dt
                    if (fireworkTimer >= 0.85f) {
                        fireworkTimer = 0f
                        spawnFireworkBurst()
                    }
                    val iterator = sparks.listIterator()
                    while (iterator.hasNext()) {
                        val s = iterator.next()
                        s.life += dt
                        if (s.life >= s.maxLife) {
                            iterator.remove()
                        } else {
                            s.xNorm += s.vx * dt
                            s.yNorm += s.vy * dt
                            s.vy += 0.18f * dt
                        }
                    }
                }

                // Update balloons
                if (CelebrationEffect.BALLOONS in activeEffects) {
                    balloons.forEach { b ->
                        b.yNorm -= b.speed * dt
                        if (b.yNorm < -0.15f) {
                            b.yNorm = 1.12f
                            b.xNorm = 0.08f + Random.nextFloat() * 0.84f
                        }
                    }
                }

                // Update sparkles
                if (CelebrationEffect.SPARKLES in activeEffects) {
                    stars.forEach { st ->
                        st.yNorm -= st.speedY * dt
                        if (st.yNorm < -0.05f) {
                            st.yNorm = 1.05f
                            st.xNorm = Random.nextFloat()
                        }
                    }
                }

                frameTick = nanos
            }
        }
    }

    val tapModifier = if (allowBalloonPop && CelebrationEffect.BALLOONS in activeEffects) {
        modifier
            .fillMaxSize()
            .pointerInput(activeEffects) {
                detectTapGestures { offset ->
                    val w = size.width.toFloat().coerceAtLeast(1f)
                    val h = size.height.toFloat().coerceAtLeast(1f)
                    val hitIndex = balloons.indexOfLast { b ->
                        val bx = (b.xNorm + sin(b.yNorm * 5f + b.phase) * 0.025f) * w
                        val by = b.yNorm * h
                        val br = b.radiusNorm * minOf(w, h) * 1.45f
                        hypot(offset.x - bx, offset.y - by) <= br
                    }
                    if (hitIndex >= 0) {
                        val popped = balloons.removeAt(hitIndex)
                        // Small pop spark burst
                        repeat(12) { i ->
                            val angle = (2.0 * PI * i / 12).toFloat()
                            sparks.add(
                                FireworkSpark(
                                    xNorm = popped.xNorm,
                                    yNorm = popped.yNorm,
                                    vx = cos(angle) * 0.16f,
                                    vy = sin(angle) * 0.16f,
                                    life = 0f,
                                    maxLife = 0.45f,
                                    color = popped.color,
                                    radius = 5f,
                                )
                            )
                        }
                        balloons.add(
                            FloatingBalloon(
                                id = (nextBalloonId++).toInt(),
                                xNorm = 0.1f + Random.nextFloat() * 0.8f,
                                yNorm = 1.15f,
                                speed = 0.09f + Random.nextFloat() * 0.08f,
                                phase = Random.nextFloat() * 6.28f,
                                radiusNorm = 0.055f + Random.nextFloat() * 0.025f,
                                color = ConfettiColors.random(),
                            )
                        )
                        onBalloonPopped()
                    }
                }
            }
    } else {
        modifier.fillMaxSize()
    }

    Canvas(modifier = tapModifier) {
        // Read frameTick to trigger invalidation cleanly
        val timeSec = (frameTick % 1_000_000_000_000L) / 1_000_000_000f
        val w = size.width
        val h = size.height

        // Draw twinkling stars
        stars.forEach { st ->
            val twinkle = 0.4f + 0.6f * ((sin(timeSec * 4.5f + st.phase) + 1f) / 2f)
            drawFourPointStar(
                center = Offset(st.xNorm * w, st.yNorm * h),
                radius = st.baseSize * twinkle,
                color = st.color.copy(alpha = 0.85f * twinkle),
            )
        }

        // Draw balloons
        balloons.forEach { b ->
            val swayX = sin(b.yNorm * 5f + b.phase) * 0.025f
            val cx = (b.xNorm + swayX) * w
            val cy = b.yNorm * h
            val rx = b.radiusNorm * minOf(w, h)
            val ry = rx * 1.24f

            // String
            drawLine(
                color = Color.Gray.copy(alpha = 0.55f),
                start = Offset(cx, cy + ry),
                end = Offset(cx + sin(timeSec * 3f + b.phase) * 10f, cy + ry + rx * 1.7f),
                strokeWidth = 2.5f,
            )
            // Balloon body
            drawOval(
                color = b.color.copy(alpha = 0.9f),
                topLeft = Offset(cx - rx, cy - ry),
                size = Size(rx * 2f, ry * 2f),
            )
            // Glossy highlight
            drawOval(
                color = Color.White.copy(alpha = 0.42f),
                topLeft = Offset(cx - rx * 0.55f, cy - ry * 0.65f),
                size = Size(rx * 0.45f, ry * 0.55f),
            )
        }

        // Draw firework sparks
        sparks.forEach { s ->
            val alpha = (1f - s.life / s.maxLife).coerceIn(0f, 1f)
            drawCircle(
                color = s.color.copy(alpha = alpha),
                radius = s.radius * (0.5f + 0.5f * alpha),
                center = Offset(s.xNorm * w, s.yNorm * h),
            )
        }

        // Draw confetti
        confetti.forEach { piece ->
            val px = piece.xNorm * w
            val py = piece.yNorm * h
            val dim = piece.sizeDp.dp.toPx()
            rotate(degrees = piece.rotation, pivot = Offset(px, py)) {
                if (piece.isRibbon) {
                    drawRect(
                        color = piece.color,
                        topLeft = Offset(px - dim * 0.65f, py - dim * 0.22f),
                        size = Size(dim * 1.3f, dim * 0.44f),
                    )
                } else {
                    drawCircle(
                        color = piece.color,
                        radius = dim * 0.4f,
                        center = Offset(px, py),
                    )
                }
            }
        }
    }
}

@Composable
fun ConfettiBurstOverlay(
    burstTrigger: Int,
    modifier: Modifier = Modifier,
) {
    val pieces = remember { mutableStateListOf<ConfettiPiece>() }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(burstTrigger) {
        if (burstTrigger <= 0) return@LaunchedEffect
        pieces.clear()
        repeat(80) {
            val angle = Random.nextFloat() * PI.toFloat() * 2f
            val speed = 0.15f + Random.nextFloat() * 0.55f
            pieces.add(
                ConfettiPiece(
                    xNorm = 0.5f + (Random.nextFloat() - 0.5f) * 0.18f,
                    yNorm = 0.35f + (Random.nextFloat() - 0.5f) * 0.12f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 0.3f,
                    rotation = Random.nextFloat() * 360f,
                    vRot = (Random.nextFloat() - 0.5f) * 340f,
                    sizeDp = 7f + Random.nextFloat() * 6f,
                    color = ConfettiColors.random(),
                    isRibbon = Random.nextBoolean(),
                )
            )
        }

        var lastNanos = 0L
        var elapsed = 0f
        while (isActive && elapsed < 2.8f && pieces.isNotEmpty()) {
            withFrameNanos { nanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                lastNanos = nanos
                elapsed += dt
                val iterator = pieces.listIterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.xNorm += p.vx * dt
                    p.yNorm += p.vy * dt
                    p.vy += 0.65f * dt
                    p.rotation += p.vRot * dt
                    if (p.yNorm > 1.15f) iterator.remove()
                }
                frameTick = nanos
            }
        }
        pieces.clear()
    }

    if (pieces.isNotEmpty()) {
        Canvas(modifier = modifier.fillMaxSize()) {
            frameTick.let { _ ->
                val w = size.width
                val h = size.height
                pieces.forEach { piece ->
                    val px = piece.xNorm * w
                    val py = piece.yNorm * h
                    val dim = piece.sizeDp.dp.toPx()
                    rotate(degrees = piece.rotation, pivot = Offset(px, py)) {
                        if (piece.isRibbon) {
                            drawRect(
                                color = piece.color,
                                topLeft = Offset(px - dim * 0.6f, py - dim * 0.22f),
                                size = Size(dim * 1.2f, dim * 0.44f),
                            )
                        } else {
                            drawCircle(
                                color = piece.color,
                                radius = dim * 0.36f,
                                center = Offset(px, py),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawFourPointStar(
    center: Offset,
    radius: Float,
    color: Color,
) {
    val inner = radius * 0.35f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + inner, center.y - inner)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + inner, center.y + inner)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - inner, center.y + inner)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - inner, center.y - inner)
        close()
    }
    drawPath(path = path, color = color)
}
