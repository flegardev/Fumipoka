package com.example.domain

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.AmbientSoundType
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * 100% offline procedural ambient sound synthesizer for Returnmark Focus Studio.
 * Requires zero network access and zero external audio files.
 */
class AmbientSoundSynthesizer {
    @Volatile
    private var isPlaying = false
    @Volatile
    private var currentType: AmbientSoundType = AmbientSoundType.NONE
    private var workerThread: Thread? = null

    fun setSound(type: AmbientSoundType, active: Boolean) {
        currentType = type
        if (!active || type == AmbientSoundType.NONE) {
            stop()
            return
        }
        if (isPlaying) return
        isPlaying = true
        workerThread = thread(start = true, isDaemon = true, name = "ReturnmarkAmbientSynth") {
            runSynthLoop()
        }
    }

    fun stop() {
        isPlaying = false
        workerThread = null
    }

    private fun runSynthLoop() {
        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)

        val track = try {
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
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (_: Throwable) {
            isPlaying = false
            return
        }

        try {
            track.play()
            val samples = ShortArray(1024)
            var brownState = 0.0
            var phase1 = 0.0
            var phase2 = 0.0
            var lfoPhase = 0.0

            while (isPlaying && currentType != AmbientSoundType.NONE) {
                val mode = currentType
                for (i in samples.indices) {
                    val sampleValue: Double = when (mode) {
                        AmbientSoundType.RAIN_CANOPY -> {
                            val white = Random.nextDouble(-1.0, 1.0)
                            brownState = (brownState + 0.06 * white) / 1.06
                            (brownState * 0.55 + white * 0.08) * 2800.0
                        }
                        AmbientSoundType.WARM_TAPE -> {
                            val white = Random.nextDouble(-1.0, 1.0)
                            brownState = (brownState + 0.025 * white) / 1.025
                            brownState * 4200.0
                        }
                        AmbientSoundType.BINAURAL_DRONE -> {
                            phase1 += (2.0 * PI * 110.0) / sampleRate
                            phase2 += (2.0 * PI * 164.81) / sampleRate
                            (sin(phase1) * 0.6 + sin(phase2) * 0.4) * 1600.0
                        }
                        AmbientSoundType.FOREST_WIND -> {
                            lfoPhase += (2.0 * PI * 0.18) / sampleRate
                            val mod = 0.45 + 0.35 * sin(lfoPhase)
                            val white = Random.nextDouble(-1.0, 1.0)
                            brownState = (brownState + 0.04 * white) / 1.04
                            brownState * mod * 3600.0
                        }
                        AmbientSoundType.NONE -> 0.0
                    }
                    samples[i] = sampleValue.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                track.write(samples, 0, samples.size)
            }
        } catch (_: Throwable) {
            // Ignore audio hardware interruptions in unit test / headless environments
        } finally {
            try {
                track.stop()
                track.release()
            } catch (_: Throwable) {
            }
        }
    }
}
