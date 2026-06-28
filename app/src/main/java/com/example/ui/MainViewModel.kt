package com.example.ui

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.audio.AudioEngine
import com.example.data.Content
import com.example.data.GenerateContentRequest
import com.example.data.GenerationConfig
import com.example.data.Part
import com.example.data.PrebuiltVoiceConfig
import com.example.data.Presets
import com.example.data.RetrofitClient
import com.example.data.SpeechConfig
import com.example.data.VoiceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val audioEngine = AudioEngine(application.applicationContext)

    // Form inputs
    val productName = MutableStateFlow("")
    val productDescription = MutableStateFlow("")
    val mainBenefit = MutableStateFlow("")
    val targetAudience = MutableStateFlow("")
    val callToAction = MutableStateFlow("")

    val selectedDuration = MutableStateFlow(Presets.Durations.first())
    val selectedVoiceStyle = MutableStateFlow(Presets.VoiceStyles.first())

    // Advanced settings
    val speechRate = MutableStateFlow(1.0f)
    val bgmEnabled = MutableStateFlow(true)
    val bgmVolume = MutableStateFlow(0.4f)
    val voiceVolume = MutableStateFlow(1.0f)
    val customVibe = MutableStateFlow("natural")
    val customBgmStyle = MutableStateFlow("upbeat")

    // UI Status states
    private val _isGeneratingScript = MutableStateFlow(false)
    val isGeneratingScript: StateFlow<Boolean> = _isGeneratingScript.asStateFlow()

    private val _isGeneratingVoice = MutableStateFlow(false)
    val isGeneratingVoice: StateFlow<Boolean> = _isGeneratingVoice.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _generatedScript = MutableStateFlow("")
    val generatedScript: StateFlow<String> = _generatedScript.asStateFlow()

    private val _editedScript = MutableStateFlow("")
    val editedScript: StateFlow<String> = _editedScript.asStateFlow()

    private val _isScriptEditingActive = MutableStateFlow(false)
    val isScriptEditingActive: StateFlow<Boolean> = _isScriptEditingActive.asStateFlow()

    private val _isAudioReady = MutableStateFlow(false)
    val isAudioReady: StateFlow<Boolean> = _isAudioReady.asStateFlow()

    private val _isRecordingUsed = MutableStateFlow(false)
    val isRecordingUsed: StateFlow<Boolean> = _isRecordingUsed.asStateFlow()

    // Interactive waveform states
    private val _waveformPeaks = MutableStateFlow(List(30) { 0.1f })
    val waveformPeaks: StateFlow<List<Float>> = _waveformPeaks.asStateFlow()

    private var playbackAnimationJob: Job? = null

    init {
        // Automatically sync advanced options when voice style changes
        viewModelScope.launch {
            selectedVoiceStyle.collect { preset ->
                speechRate.value = when (preset.tempo) {
                    "fast" -> 1.15f
                    "medium-fast" -> 1.05f
                    "normal" -> 0.95f
                    else -> 1.00f
                }
                customVibe.value = preset.vibe
                customBgmStyle.value = preset.bgmStyle
            }
        }
    }

    fun setProductName(value: String) { productName.value = value }
    fun setProductDescription(value: String) { productDescription.value = value }
    fun setMainBenefit(value: String) { mainBenefit.value = value }
    fun setTargetAudience(value: String) { targetAudience.value = value }
    fun setCallToAction(value: String) { callToAction.value = value }
    fun updateEditedScript(value: String) { _editedScript.value = value }

    fun clearStatus() {
        _statusMessage.value = null
        _errorMessage.value = null
    }

    /**
     * Step 1: Generate script automatically
     */
    fun handleGenerateAdScript() {
        if (productName.value.isBlank()) {
            _errorMessage.value = "Nama Produk wajib diisi!"
            return
        }
        if (productDescription.value.isBlank()) {
            _errorMessage.value = "Penjelasan Produk wajib diisi!"
            return
        }
        if (mainBenefit.value.isBlank()) {
            _errorMessage.value = "Manfaat Utama wajib diisi!"
            return
        }

        clearStatus()
        _isGeneratingScript.value = true

        viewModelScope.launch {
            val key = BuildConfig.GEMINI_API_KEY
            val isKeyConfigured = key.isNotBlank() && key != "MY_GEMINI_API_KEY"

            val target = targetAudience.value.ifBlank { "orang awam dan calon pengguna umum" }
            val cta = callToAction.value.ifBlank { "Coba sekarang" }

            val prompt = """
                Buat naskah voice-over iklan bahasa Indonesia.

                Tujuan:
                Naskah ini akan dipakai untuk video iklan digital.

                Data produk:
                * Nama produk: ${productName.value}
                * Penjelasan produk: ${productDescription.value}
                * Manfaat utama: ${mainBenefit.value}
                * Target penonton: $target
                * Ajakan akhir: $cta

                Durasi iklan:
                ${selectedDuration.value.label}

                Panjang naskah:
                ${selectedDuration.value.minWords} sampai ${selectedDuration.value.maxWords} kata.

                Gaya suara:
                ${selectedVoiceStyle.value.label}

                Arahan gaya bicara:
                ${selectedVoiceStyle.value.instruction}

                Aturan penting:
                * Output hanya naskah voice-over final.
                * Jangan sertakan judul.
                * Jangan sertakan bullet point.
                * Jangan sertakan catatan.
                * Jangan sertakan penjelasan tambahan.
                * Jangan terlalu formal.
                * Jangan memakai istilah teknis yang sulit dipahami.
                * Jangan membuat klaim berlebihan.
                * Gunakan kalimat pendek.
                * Buat naskah enak dibacakan.
                * Buat hook awal yang langsung menarik perhatian.
                * Buat naskah cocok untuk TikTok, Reels, Shorts, YouTube Shorts, dan iklan digital.
                * Sesuaikan panjang naskah dengan durasi.
                * Jangan menambahkan fakta yang tidak diberikan user.
                * Jika data kurang lengkap, buat naskah tetap aman, umum, dan mudah dipahami.

                Struktur naskah berdasarkan durasi:

                Untuk 30 Detik:
                * Hook pembuka
                * Masalah singkat
                * Solusi produk
                * Manfaat utama
                * Ajakan akhir

                Untuk 1 Menit:
                * Hook pembuka
                * Masalah audiens
                * Perkenalan produk
                * Cara kerja singkat
                * Manfaat utama
                * Ajakan akhir

                Untuk 1,5 Menit:
                * Hook pembuka
                * Masalah utama
                * Perkenalan produk
                * Cara kerja
                * Manfaat utama
                * Contoh penggunaan
                * Ajakan akhir
            """.trimIndent()

            var scriptResult = ""
            if (isKeyConfigured) {
                try {
                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt))))
                    )
                    val response = RetrofitClient.service.generateScript(key, request)
                    val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (text != null && text.isNotBlank()) {
                        scriptResult = text.trim()
                    } else {
                        Log.e("MainViewModel", "Empty script response from Gemini")
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Failed to generate script via Gemini REST", e)
                }
            }

            // Fallback to local script generator if key failed/not present
            if (scriptResult.isBlank()) {
                scriptResult = generateOfflineScript(
                    productName.value,
                    productDescription.value,
                    mainBenefit.value,
                    targetAudience.value,
                    callToAction.value,
                    selectedDuration.value.id
                )
                _statusMessage.value = "Naskah dibuat offline (Kunci API Gemini tidak terdeteksi)."
            } else {
                _statusMessage.value = "Naskah berhasil dibuat dengan kecerdasan buatan Gemini!"
            }

            _generatedScript.value = scriptResult
            _editedScript.value = scriptResult
            _isScriptEditingActive.value = true
            _isAudioReady.value = false // invalidate old audio
            _isGeneratingScript.value = false
        }
    }

    private fun generateOfflineScript(
        productName: String,
        productDescription: String,
        mainBenefit: String,
        targetAudience: String,
        callToAction: String,
        durationId: String
    ): String {
        val target = if (targetAudience.isBlank()) "orang awam dan calon pengguna umum" else targetAudience
        val cta = if (callToAction.isBlank()) "Coba sekarang" else callToAction

        return when (durationId) {
            "30s" -> {
                "Bosan dengan masalah yang itu-itu saja? Memperkenalkan $productName! Solusi terbaik untuk $productDescription. Dengan $productName, kamu bisa langsung merasakan manfaat luar biasa seperti $mainBenefit. Sangat cocok buat kamu para $target yang mendambakan kepraktisan sehari-hari. Jangan tunda lagi, yuk $cta!"
            }
            "1m" -> {
                "Apakah kamu merasa lelah menghadapi tantangan sehari-hari yang menghambat produktivitasmu? Tenang, kamu tidak sendirian. Kini telah hadir inovasi terbaru yang dirancang khusus untukmu: $productName! Ini adalah jawaban lengkap untuk mengatasi $productDescription. Cara kerjanya sangat sederhana dan cepat memberikan hasil nyata. Kamu bisa menikmati $mainBenefit secara instan tanpa ribet. Produk luar biasa ini didesain spesial untuk memenuhi kebutuhan $target. Dapatkan kenyamanan dan efisiensi terbaik sekarang juga. Jangan lewatkan kesempatan emas ini, mari $cta!"
            }
            else -> {
                "Setiap orang pasti menginginkan kehidupan yang lebih mudah dan bebas stres. Tapi seringkali, hambatan seperti $productDescription merusak hari-hari indahmu. Sekarang, kamu bisa tersenyum lebar karena ada $productName! Produk revolusioner yang siap mengubah cara hidupmu menjadi jauh lebih baik. Dengan teknologi mutakhir yang sangat mudah dipahami, $productName bekerja secara maksimal memberikan solusi instan. Rasakan kenyamanan luar biasa dari $mainBenefit yang akan langsung meringankan beban tugasmu. Bayangkan betapa terbantunya aktivitas harianmu mulai hari ini. Sangat direkomendasikan untuk seluruh $target di Indonesia yang ingin maju selangkah lebih cepat. Tunggu apa lagi? Ambil langkah pertamamu dan langsung $cta!"
            }
        }
    }

    /**
     * Step 2: Generate Voice-over audio from script
     */
    fun handleGenerateVoiceover() {
        if (_editedScript.value.isBlank()) {
            _errorMessage.value = "Naskah kosong! Silakan buat naskah otomatis terlebih dahulu."
            return
        }

        clearStatus()
        _isGeneratingVoice.value = true
        _isRecordingUsed.value = false

        viewModelScope.launch {
            val key = BuildConfig.GEMINI_API_KEY
            val isKeyConfigured = key.isNotBlank() && key != "MY_GEMINI_API_KEY"

            var audioGenerated = false

            // Try Cloud synthesis if key is present
            if (isKeyConfigured) {
                try {
                    val voicePrompt = """
                        Buat voice-over bahasa Indonesia untuk konten iklan.

                        Gaya suara:
                        ${selectedVoiceStyle.value.label}

                        Arahan suara:
                        ${selectedVoiceStyle.value.instruction}

                        Tempo:
                        ${selectedVoiceStyle.value.tempo}

                        Nuansa:
                        ${selectedVoiceStyle.value.vibe}

                        Aturan pembacaan:
                        - Bacakan naskah dengan jelas, natural, dan mudah dipahami.
                        - Jangan terdengar seperti robot.
                        - Jangan terlalu formal, kecuali gaya suara membutuhkan kesan profesional.
                        - Jangan membaca tanda baca secara berlebihan.
                        - Beri jeda pendek setelah kalimat penting.
                        - Jangan menambahkan kata baru di luar naskah.
                        - Cocok untuk video pendek, iklan digital, dan konten promosi.

                        Naskah:
                        ${_editedScript.value}
                    """.trimIndent()

                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = voicePrompt)))),
                        generationConfig = GenerationConfig(
                            responseModalities = listOf("AUDIO"),
                            speechConfig = SpeechConfig(
                                voiceConfig = VoiceConfig(
                                    prebuiltVoiceConfig = PrebuiltVoiceConfig(voiceName = selectedVoiceStyle.value.voiceName)
                                )
                            )
                        )
                    )

                    val response = RetrofitClient.service.generateSpeech(key, request)
                    val base64Audio = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.inlineData?.data

                    if (base64Audio != null) {
                        val audioBytes = Base64.decode(base64Audio, Base64.DEFAULT)
                        withContext(Dispatchers.IO) {
                            if (audioEngine.ttsFile.exists()) {
                                audioEngine.ttsFile.delete()
                            }
                            FileOutputStream(audioEngine.ttsFile).use { fos ->
                                fos.write(audioBytes)
                            }
                        }
                        audioGenerated = true
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Cloud TTS call failed", e)
                }
            }

            // Fallback to local TTS engine
            if (!audioGenerated) {
                val localSynthesised = audioEngine.synthesizeSpeechToFile(
                    _editedScript.value,
                    speechRate.value
                )
                if (localSynthesised) {
                    audioGenerated = true
                    _statusMessage.value = "Voice-over disintesis offline menggunakan mesin ucapan lokal."
                } else {
                    _errorMessage.value = "Gagal menyintesis voice-over. Silakan coba lagi."
                }
            } else {
                _statusMessage.value = "Voice-over sukses dibuat dengan Gemini AI Cloud Voice!"
            }

            if (audioGenerated) {
                // Generate and mix BGM if enabled
                if (bgmEnabled.value) {
                    _statusMessage.value = "Menyintesis musik latar dan menggabungkan audio..."
                    withContext(Dispatchers.IO) {
                        audioEngine.generateBgmTrack(customVibe.value, customBgmStyle.value)
                        audioEngine.mixVoiceAndBgm(
                            isRecordingUsed = false,
                            bgmVolume = bgmVolume.value,
                            voiceVolume = voiceVolume.value
                        )
                    }
                } else {
                    // Copy pure TTS file as the final output
                    withContext(Dispatchers.IO) {
                        if (audioEngine.mixedFile.exists()) audioEngine.mixedFile.delete()
                        audioEngine.ttsFile.copyTo(audioEngine.mixedFile)
                    }
                }
                _isAudioReady.value = true
            }

            _isGeneratingVoice.value = false
        }
    }

    /**
     * Play synthesized or recorded ad
     */
    fun playAd() {
        if (!_isAudioReady.value && !_isRecordingUsed.value) return
        clearStatus()
        _isPlaying.value = true
        audioEngine.startPlaying(
            isRecordingUsed = _isRecordingUsed.value,
            bgmEnabled = bgmEnabled.value,
            bgmVolume = bgmVolume.value,
            voiceVolume = voiceVolume.value,
            onCompletion = {
                _isPlaying.value = false
                stopWaveformAnimation()
            }
        )
        startWaveformAnimation()
    }

    /**
     * Pause / Stop active audio
     */
    fun stopAd() {
        _isPlaying.value = false
        audioEngine.stopPlaying()
        stopWaveformAnimation()
    }

    /**
     * Reset Voice-over
     */
    fun resetAd() {
        stopAd()
        _isAudioReady.value = false
        _isRecordingUsed.value = false
        _statusMessage.value = "Studio di-reset. Siap membuat audio baru."
    }

    /**
     * Microphone Recording operations
     */
    fun startMicRecording() {
        clearStatus()
        stopAd()
        _isRecording.value = true
        _isRecordingUsed.value = true
        _isAudioReady.value = false

        audioEngine.startRecording()
        startWaveformAnimation()
        _statusMessage.value = "Merekam suara dari mic..."
    }

    fun stopMicRecording() {
        if (!_isRecording.value) return
        _isRecording.value = false
        audioEngine.stopRecording()
        stopWaveformAnimation()

        // Combine mic recording with BGM if music is enabled
        viewModelScope.launch(Dispatchers.IO) {
            if (bgmEnabled.value) {
                audioEngine.generateBgmTrack(customVibe.value, customBgmStyle.value)
                audioEngine.mixVoiceAndBgm(
                    isRecordingUsed = true,
                    bgmVolume = bgmVolume.value,
                    voiceVolume = voiceVolume.value
                )
            } else {
                if (audioEngine.mixedFile.exists()) audioEngine.mixedFile.delete()
                audioEngine.recordFile.copyTo(audioEngine.mixedFile)
            }
            _isAudioReady.value = true
            _statusMessage.value = "Rekaman mic disimpan & digabungkan dengan musik latar!"
        }
    }

    /**
     * Download the WAV file to public Downloads directory
     */
    fun downloadWavFile() {
        val fileName = audioEngine.downloadWavFileToDownloads()
        if (fileName != null) {
            _statusMessage.value = "Audio berhasil didownload! File disimpan di folder Downloads dengan nama: $fileName"
        } else {
            _errorMessage.value = "Gagal mendownload WAV. Pastikan audio sudah dibuat."
        }
    }

    // Dynamic Waveform Animation
    private fun startWaveformAnimation() {
        playbackAnimationJob?.cancel()
        playbackAnimationJob = viewModelScope.launch {
            while (_isPlaying.value || _isRecording.value) {
                // Generate highly organic dynamic wave peaks
                _waveformPeaks.value = List(30) {
                    val multiplier = if (_isRecording.value) 0.85f else 0.7f
                    (Math.random().toFloat() * multiplier + 0.15f).coerceIn(0.1f, 1.0f)
                }
                delay(120)
            }
        }
    }

    private fun stopWaveformAnimation() {
        playbackAnimationJob?.cancel()
        playbackAnimationJob = null
        // Reset to neat baseline
        _waveformPeaks.value = List(30) { 0.15f }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
