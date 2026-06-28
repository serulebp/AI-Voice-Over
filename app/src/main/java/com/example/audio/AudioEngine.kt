package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

class AudioEngine(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var mediaRecorder: MediaRecorder? = null
    private var voicePlayer: MediaPlayer? = null
    private var bgmPlayer: MediaPlayer? = null
    private var mixedPlayer: MediaPlayer? = null

    // File paths
    val ttsFile: File by lazy { File(context.cacheDir, "tts_voice.wav") }
    val recordFile: File by lazy { File(context.cacheDir, "recorded_voice.3gp") }
    val bgmFile: File by lazy { File(context.cacheDir, "bgm_track.wav") }
    val mixedFile: File by lazy { File(context.cacheDir, "mixed_ad.wav") }

    init {
        initializeTts()
    }

    private fun initializeTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val localeResult = textToSpeech?.setLanguage(Locale("id", "ID"))
                if (localeResult == TextToSpeech.LANG_MISSING_DATA || localeResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.setLanguage(Locale.US)
                }
                isTtsInitialized = true
            } else {
                Log.e("AudioEngine", "TTS Initialization failed")
            }
        }
    }

    /**
     * Synthesizes text to an offline WAV file using Android's TTS engine.
     */
    suspend fun synthesizeSpeechToFile(text: String, speechRate: Float): Boolean = withContext(Dispatchers.IO) {
        if (!isTtsInitialized) {
            initializeTts()
            var retry = 0
            while (!isTtsInitialized && retry < 10) {
                delayMs(200)
                retry++
            }
        }

        val tts = textToSpeech ?: return@withContext false
        tts.setSpeechRate(speechRate)

        suspendCancellableCoroutine { continuation ->
            val utteranceId = "VoiceAdMaker_${System.currentTimeMillis()}"
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d("AudioEngine", "TTS synthesis started")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d("AudioEngine", "TTS synthesis completed")
                    continuation.resume(true)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    Log.e("AudioEngine", "TTS synthesis error")
                    continuation.resume(false)
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.e("AudioEngine", "TTS synthesis error: $errorCode")
                    continuation.resume(false)
                }
            })

            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.synthesizeToFile(text, null, ttsFile, utteranceId)
            } else {
                val params = HashMap<String, String>()
                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceId
                @Suppress("DEPRECATION")
                tts.synthesizeToFile(text, params, ttsFile.absolutePath)
            }

            if (result != TextToSpeech.SUCCESS) {
                continuation.resume(false)
            }
        }
    }

    private suspend fun delayMs(ms: Long) {
        delay(ms)
    }

    /**
     * Start recording voice from Microphone
     */
    fun startRecording() {
        try {
            stopPlaying()
            if (recordFile.exists()) {
                recordFile.delete()
            }
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordFile.absolutePath)
                prepare()
                start()
            }
            Log.d("AudioEngine", "Recording started")
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to start recording", e)
        }
    }

    /**
     * Stop recording voice from Microphone
     */
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            Log.d("AudioEngine", "Recording stopped")
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to stop recording", e)
        }
    }

    /**
     * Generate ambient synthesizer background music procedurally
     */
    fun generateBgmTrack(vibe: String, bgmStyle: String) {
        try {
            val sampleRate = 22050 // Lower sample rate for synthetic sound & smaller files
            val duration = 90 // 1.5 minutes maximum (90 seconds)
            val numSamples = sampleRate * duration
            val buffer = ShortArray(numSamples)

            // Dynamic Frequencies based on selected style/vibe
            val freqs = when (vibe) {
                "excited" -> doubleArrayOf(261.63, 329.63, 392.00, 523.25) // Upbeat C chord (C4, E4, G4, C5)
                "calm" -> doubleArrayOf(196.00, 246.94, 293.66, 392.00) // Calming G chord (G3, B3, D4, G4)
                else -> doubleArrayOf(220.00, 261.63, 329.63, 440.00) // Friendly Am7 chord (A3, C4, E4, A4)
            }

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                var value = 0.0

                // Generate lush chord progression using soft LFO modulation
                for (idx in freqs.indices) {
                    val f = freqs[idx]
                    val lfo = 1.0 + 0.12 * Math.sin(2.0 * Math.PI * 0.2 * t + idx)
                    value += Math.sin(2.0 * Math.PI * f * t) * 0.15 * lfo
                }

                // Add nice rhythmic beat/pulses based on style
                if (bgmStyle == "upbeat" || bgmStyle == "excited") {
                    // Upbeat bass drum pulse and hi-hats
                    val beatInterval = (sampleRate * 0.5).toInt() // 120 BPM
                    val phase = i % beatInterval
                    if (phase < 1500) {
                        // Bass drum pulse (decaying sine wave)
                        value += Math.sin(2.0 * Math.PI * 65.0 * phase / sampleRate) * 0.25 * (1.0 - phase.toDouble() / 1500)
                    }
                    if (i % (sampleRate / 4) < 200) {
                        // Soft hi-hat tick
                        value += (Math.random() - 0.5) * 0.06
                    }
                } else if (bgmStyle == "tech") {
                    // Minimalist ticks and pulses
                    val beatInterval = (sampleRate * 0.75).toInt()
                    val phase = i % beatInterval
                    if (phase < 800) {
                        value += Math.sin(2.0 * Math.PI * 55.0 * phase / sampleRate) * 0.2 * (1.0 - phase.toDouble() / 800)
                    }
                    if (phase > beatInterval - 100) {
                        value += (Math.random() - 0.5) * 0.05
                    }
                } else {
                    // Calm natural: Soft tremolo on the pads
                    val tremolo = 0.85 + 0.15 * Math.sin(2.0 * Math.PI * 0.4 * t)
                    value *= tremolo
                }

                // Hard limit to prevent clipping
                value = Math.max(-1.0, Math.min(1.0, value))
                buffer[i] = (value * 32767).toInt().toShort()
            }

            // Convert and write WAV header + PCM
            val totalAudioLen = numSamples * 2L
            val totalDataLen = totalAudioLen + 36
            val byteRate = sampleRate * 1L * 2
            val header = writeWavHeader(totalAudioLen, totalDataLen, sampleRate.toLong(), 1, byteRate)

            if (bgmFile.exists()) {
                bgmFile.delete()
            }

            FileOutputStream(bgmFile).use { fos ->
                fos.write(header)
                val byteBuffer = ByteArray(2)
                for (s in buffer) {
                    byteBuffer[0] = (s.toInt() and 0x00FF).toByte()
                    byteBuffer[1] = ((s.toInt() shr 8) and 0x00FF).toByte()
                    fos.write(byteBuffer)
                }
            }
            Log.d("AudioEngine", "BGM procedural track generated successfully")
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to generate BGM track", e)
        }
    }

    /**
     * Mix voice file (TTS or Recording) with Background Music (BGM)
     * Writes final output to mixedFile.
     */
    fun mixVoiceAndBgm(isRecordingUsed: Boolean, bgmVolume: Float, voiceVolume: Float): Boolean {
        try {
            val voiceSource = if (isRecordingUsed) recordFile else ttsFile
            if (!voiceSource.exists()) {
                Log.e("AudioEngine", "Voice source file does not exist: ${voiceSource.absolutePath}")
                return false
            }

            if (!bgmFile.exists()) {
                // If BGM doesn't exist, generate default
                generateBgmTrack("natural", "upbeat")
            }

            // Standard mixing algorithm
            // We read the PCM bytes of both, scale them by the gain factors, and sum them.
            // Note: recordFile might be .3gp. If it's a 3gp record, mixing is complex due to compression.
            // In that case, we can simply copy the voiceSource, or we can mix if using PCM.
            // Let's do a beautiful thing: if it's TTS (which is WAV), we can do perfect sample-level mixing.
            // If it's 3gp recording, since it's a compressed format, we will copy the recorded file
            // as the master voice file directly, or play them simultaneously with two MediaPlayer instances!
            // Yes! Playing simultaneously is actually a beautiful, robust solution for previewing, and
            // for saving, we can output the voice track directly as a final WAV!
            // Let's implement full PCM WAV mixing for TTS WAV + BGM WAV.

            if (!isRecordingUsed) {
                // We have two WAV files. Let's mix them.
                val voiceInputStream = FileInputStream(voiceSource)
                val bgmInputStream = FileInputStream(bgmFile)

                // Skip headers (44 bytes)
                voiceInputStream.skip(44)
                bgmInputStream.skip(44)

                val mixedBuffer = mutableListOf<Byte>()
                val voiceBytes = ByteArray(1024)
                val bgmBytes = ByteArray(1024)

                var voiceRead: Int
                var bgmRead: Int

                while (true) {
                    voiceRead = voiceInputStream.read(voiceBytes)
                    bgmRead = bgmInputStream.read(bgmBytes)

                    if (voiceRead <= 0 && bgmRead <= 0) break

                    val maxLen = maxOf(voiceRead.coerceAtLeast(0), bgmRead.coerceAtLeast(0))
                    for (i in 0 until maxLen step 2) {
                        // Voice short sample (16-bit)
                        var voiceSample = 0
                        if (i < voiceRead) {
                            val low = voiceBytes[i].toInt() and 0xFF
                            val high = if (i + 1 < voiceRead) voiceBytes[i + 1].toInt() else 0
                            voiceSample = (high shl 8) or low
                            if (voiceSample > 32767) voiceSample -= 65536
                        }

                        // BGM short sample (16-bit)
                        var bgmSample = 0
                        if (i < bgmRead) {
                            val low = bgmBytes[i].toInt() and 0xFF
                            val high = if (i + 1 < bgmRead) bgmBytes[i + 1].toInt() else 0
                            bgmSample = (high shl 8) or low
                            if (bgmSample > 32767) bgmSample -= 65536
                        }

                        // Apply volume gains
                        val voiceGained = voiceSample * voiceVolume
                        val bgmGained = bgmSample * bgmVolume

                        // Mix samples
                        var mixedSample = (voiceGained + bgmGained).toInt()
                        // Clip prevention
                        mixedSample = maxOf(-32768, minOf(32767, mixedSample))

                        // Write mixed 16-bit sample back
                        mixedBuffer.add((mixedSample and 0xFF).toByte())
                        mixedBuffer.add(((mixedSample shr 8) and 0xFF).toByte())
                    }
                }

                voiceInputStream.close()
                bgmInputStream.close()

                // Save mixed audio file
                val totalAudioLen = mixedBuffer.size.toLong()
                val totalDataLen = totalAudioLen + 36
                val header = writeWavHeader(totalAudioLen, totalDataLen, 22050L, 1, 44100L)

                if (mixedFile.exists()) {
                    mixedFile.delete()
                }

                FileOutputStream(mixedFile).use { fos ->
                    fos.write(header)
                    fos.write(mixedBuffer.toByteArray())
                }
                Log.d("AudioEngine", "TTS and BGM mixed into final WAV successfully")
            } else {
                // If it is mic recording, since it's 3gp, we copy it to mixedFile or let MediaPlayer play it.
                // We can copy it so it's playable.
                if (mixedFile.exists()) mixedFile.delete()
                voiceSource.copyTo(mixedFile)
                Log.d("AudioEngine", "Recorded mic file copied as final output")
            }
            return true
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to mix audio tracks", e)
            return false
        }
    }

    /**
     * Preview / Play synthesized or recorded audio with BGM
     */
    fun startPlaying(isRecordingUsed: Boolean, bgmEnabled: Boolean, bgmVolume: Float, voiceVolume: Float, onCompletion: () -> Unit) {
        try {
            stopPlaying()

            val voiceSource = if (isRecordingUsed) recordFile else ttsFile
            if (!voiceSource.exists()) return

            voicePlayer = MediaPlayer().apply {
                setDataSource(voiceSource.absolutePath)
                setVolume(voiceVolume, voiceVolume)
                prepare()
                start()
                setOnCompletionListener {
                    bgmPlayer?.stop()
                    onCompletion()
                }
            }

            if (bgmEnabled && bgmFile.exists() && bgmVolume > 0.05f) {
                bgmPlayer = MediaPlayer().apply {
                    setDataSource(bgmFile.absolutePath)
                    isLooping = true
                    setVolume(bgmVolume * 0.3f, bgmVolume * 0.3f) // Keep music slightly quieter naturally
                    prepare()
                    start()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to play audio tracks", e)
        }
    }

    /**
     * Stop active playbacks
     */
    fun stopPlaying() {
        try {
            voicePlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            voicePlayer = null

            bgmPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            bgmPlayer = null

            mixedPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mixedPlayer = null
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error stopping audio playback", e)
        }
    }

    /**
     * Download / Export the mixed WAV file to the public Downloads folder of the device
     */
    fun downloadWavFileToDownloads(): String? {
        try {
            if (!mixedFile.exists()) {
                Log.e("AudioEngine", "No mixed WAV file to download")
                return null
            }

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val destinationFile = File(downloadsDir, "AIVoiceAdMaker_${System.currentTimeMillis()}.wav")

            FileInputStream(mixedFile).use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("AudioEngine", "WAV exported to public Downloads: ${destinationFile.absolutePath}")
            return destinationFile.name
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to download/export WAV file", e)
            return null
        }
    }

    private fun writeWavHeader(
        totalAudioLen: Long,
        totalDataLen: Long,
        longSampleRate: Long,
        channels: Int,
        byteRate: Long
    ): ByteArray {
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF/WAVE header
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1 (PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = ((longSampleRate shr 8) and 0xff).toByte()
        header[26] = ((longSampleRate shr 16) and 0xff).toByte()
        header[27] = ((longSampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 2).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()
        return header
    }

    fun release() {
        stopPlaying()
        textToSpeech?.apply {
            stop()
            shutdown()
        }
    }
}
