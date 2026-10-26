package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingTitle = MutableStateFlow<String?>(null)
    val currentPlayingTitle: StateFlow<String?> = _currentPlayingTitle.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var syntheticToneJob: Job? = null
    private var isPlayingSynthetic = false

    private val scope = CoroutineScope(Dispatchers.IO)

    fun playAdhan(voice: AdhanVoice, volume: Float = 0.8f) {
        stopAdhan()

        _currentPlayingTitle.value = voice.titleArabic
        _isPlaying.value = true

        if (voice.url.startsWith("builtin://")) {
            playSyntheticChime(voice.id, volume)
            return
        }

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(voice.url)
                setVolume(volume, volume)
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnCompletionListener {
                    stopAdhan()
                }
                setOnErrorListener { _, what, extra ->
                    Log.w("SoundManager", "MediaPlayer error ($what, $extra), falling back to offline synthesized chime")
                    playSyntheticChime(voice.id, volume)
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to start streaming adhan, falling back to offline synthesizer", e)
            playSyntheticChime(voice.id, volume)
        }
    }

    private fun playSyntheticChime(toneType: String, volume: Float) {
        syntheticToneJob?.cancel()
        isPlayingSynthetic = true

        syntheticToneJob = scope.launch {
            try {
                val sampleRate = 44100
                // Harmonic frequencies for an uplifting spiritual bell / chime melody
                val frequencies = if (toneType == "takbeerat") {
                    doubleArrayOf(392.0, 440.0, 523.25, 659.25, 783.99, 523.25) // G4, A4, C5, E5, G5, C5
                } else {
                    doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
                }

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(minBufferSize)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        AudioManager.STREAM_ALARM,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        minBufferSize,
                        AudioTrack.MODE_STREAM
                    )
                }

                audioTrack.play()

                val durationPerNote = 0.6 // seconds
                val noteSamples = (sampleRate * durationPerNote).toInt()
                val buffer = ShortArray(noteSamples)

                for (freq in frequencies) {
                    if (!isPlayingSynthetic) break
                    for (i in 0 until noteSamples) {
                        val time = i.toDouble() / sampleRate
                        val envelope = (1.0 - (i.toDouble() / noteSamples)).coerceIn(0.0, 1.0)
                        val angle = 2.0 * Math.PI * freq * time
                        val sample = (sin(angle) * envelope * volume * 0.8 * Short.MAX_VALUE).toInt()
                        buffer[i] = sample.toShort()
                    }
                    audioTrack.write(buffer, 0, noteSamples)
                }

                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                Log.e("SoundManager", "Synthetic audio error", e)
            } finally {
                isPlayingSynthetic = false
                _isPlaying.value = false
                _currentPlayingTitle.value = null
            }
        }
    }

    fun stopAdhan() {
        isPlayingSynthetic = false
        syntheticToneJob?.cancel()
        syntheticToneJob = null

        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error stopping MediaPlayer", e)
        }
        mediaPlayer = null

        _isPlaying.value = false
        _currentPlayingTitle.value = null
    }
}
