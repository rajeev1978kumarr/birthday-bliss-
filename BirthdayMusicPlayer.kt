package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class BirthdayMusicPlayer {
    private var playJob: Job? = null

    fun playHappyBirthday(
        scope: CoroutineScope,
        onComplete: () -> Unit = {},
    ) {
        stop()
        playJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val notes = listOf(
                // "Happy birthday to you"
                261.63 to 240, 261.63 to 180, 293.66 to 420, 261.63 to 420, 349.23 to 420, 329.63 to 720,
                // "Happy birthday to you"
                261.63 to 240, 261.63 to 180, 293.66 to 420, 261.63 to 420, 392.00 to 420, 349.23 to 720,
                // "Happy birthday dear friend"
                261.63 to 240, 261.63 to 180, 523.25 to 420, 440.00 to 420, 349.23 to 420, 329.63 to 420, 293.66 to 620,
                // "Happy birthday to you"
                466.16 to 240, 466.16 to 180, 440.00 to 420, 349.23 to 420, 392.00 to 420, 349.23 to 820,
            )

            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            ).coerceAtLeast(2048)

            val track = runCatching {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            }.getOrNull() ?: run {
                onComplete()
                return@launch
            }

            try {
                track.play()
                for ((freq, durationMs) in notes) {
                    if (!currentCoroutineContext().isActive) break
                    val totalSamples = (sampleRate * durationMs) / 1000
                    val buffer = ShortArray(totalSamples)
                    for (i in 0 until totalSamples) {
                        val t = i.toDouble() / sampleRate
                        val attack = (t / 0.015).coerceAtMost(1.0)
                        val decay = exp(-3.5 * t)
                        val fundamental = sin(2.0 * PI * freq * t)
                        val harmonic = 0.25 * sin(2.0 * PI * freq * 2.0 * t)
                        val sample = (fundamental + harmonic) * attack * decay * 0.35
                        buffer[i] = (sample * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    track.write(buffer, 0, buffer.size)
                    delay(28)
                }
            } finally {
                runCatching {
                    track.stop()
                    track.release()
                }
                onComplete()
            }
        }
    }

    fun stop() {
        playJob?.cancel()
        playJob = null
    }
}
