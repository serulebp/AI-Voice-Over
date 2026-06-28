package com.example.data

data class AdDurationPreset(
    val id: String,
    val label: String,
    val description: String,
    val minWords: Int,
    val maxWords: Int
)

data class VoiceStylePreset(
    val id: String,
    val label: String,
    val description: String,
    val character: String,
    val voiceName: String,
    val tempo: String,
    val vibe: String,
    val bgmStyle: String,
    val instruction: String
)

object Presets {
    val Durations = listOf(
        AdDurationPreset(
            id = "30s",
            label = "30 Detik",
            description = "Cocok untuk TikTok, Reels, Shorts, dan iklan cepat",
            minWords = 65,
            maxWords = 80
        ),
        AdDurationPreset(
            id = "1m",
            label = "1 Menit",
            description = "Cocok untuk iklan produk yang butuh penjelasan ringan",
            minWords = 130,
            maxWords = 160
        ),
        AdDurationPreset(
            id = "1.5m",
            label = "1,5 Menit",
            description = "Cocok untuk demo produk atau penjelasan lebih lengkap",
            minWords = 200,
            maxWords = 230
        )
    )

    val VoiceStyles = listOf(
        VoiceStylePreset(
            id = "friendly",
            label = "Ramah Natural",
            description = "Default. Cocok untuk hampir semua iklan.",
            character = "ramah, jelas, natural, mudah dipercaya",
            voiceName = "Kore",
            tempo = "medium-fast",
            vibe = "natural",
            bgmStyle = "upbeat",
            instruction = "Bacakan seperti marketer ramah yang menjelaskan produk dengan natural, jelas, dan mudah dipahami orang awam. Jangan terlalu formal dan jangan terlalu agresif."
        ),
        VoiceStylePreset(
            id = "promo",
            label = "Promo Semangat",
            description = "Cocok untuk promo, diskon, dan konten video pendek cepat.",
            character = "lebih antusias, cepat, punchy, menarik perhatian",
            voiceName = "Kore",
            tempo = "fast",
            vibe = "excited",
            bgmStyle = "upbeat",
            instruction = "Bacakan dengan gaya iklan pendek yang energik dan menarik perhatian. Tempo cepat, antusias, tetapi tetap jelas dan tidak berlebihan."
        ),
        VoiceStylePreset(
            id = "premium",
            label = "Premium Tenang",
            description = "Cocok untuk produk premium, hotel, interior, atau company profile.",
            character = "tenang, elegan, profesional, terdengar mahal",
            voiceName = "Kore",
            tempo = "normal",
            vibe = "calm",
            bgmStyle = "tech",
            instruction = "Bacakan dengan suara tenang, elegan, dan profesional. Jangan terlalu cepat. Buat terdengar rapi, premium, dan meyakinkan."
        )
    )
}
