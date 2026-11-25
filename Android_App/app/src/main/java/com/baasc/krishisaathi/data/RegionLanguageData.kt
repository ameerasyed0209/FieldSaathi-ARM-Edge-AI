package com.baasc.krishisaathi.data

data class AppLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String
) {
    val displayName: String
        get() = if (englishName == nativeName) englishName else "$englishName ($nativeName)"
}

data class Region(
    val name: String,
    val supportedLanguageCodes: List<String>
)

object RegionLanguageData {

    val ALL_LANGUAGES: Map<String, AppLanguage> = listOf(
        AppLanguage("kn", "Kannada", "ಕನ್ನಡ"),
        AppLanguage("en", "English", "English"),
        AppLanguage("hi", "Hindi", "हिन्दी"),
        AppLanguage("mr", "Marathi", "मराठी"),
        AppLanguage("ta", "Tamil", "தமிழ்"),
        AppLanguage("te", "Telugu", "తెలుగు"),
        AppLanguage("ml", "Malayalam", "മലയാളം"),
        AppLanguage("gu", "Gujarati", "ગુજરાતી"),
        AppLanguage("ur", "Urdu", "اردو"),
        AppLanguage("pa", "Punjabi", "ਪੰਜਾਬੀ"),
        AppLanguage("ar", "Arabic", "العربية"),
        AppLanguage("kok", "Konkani", "कोंकणी"),
        AppLanguage("sd", "Sindhi", "سنڌي"),
        AppLanguage("raj", "Rajasthani", "राजस्थानी"),
        AppLanguage("bho", "Bhojpuri", "भोजपुरी"),
        AppLanguage("awa", "Awadhi", "अवधी")
    ).associateBy { it.code }

    // Exactly 10 States with specific language lists as requested
    val REGIONS: List<Region> = listOf(
        Region(
            name = "Karnataka",
            supportedLanguageCodes = listOf("kn", "en", "hi", "ur", "ta", "te", "mr", "kok")
        ),
        Region(
            name = "Maharashtra",
            supportedLanguageCodes = listOf("mr", "hi", "en", "ur", "gu", "kok")
        ),
        Region(
            name = "Tamil Nadu",
            supportedLanguageCodes = listOf("ta", "en", "te", "kn", "ml", "ur")
        ),
        Region(
            name = "Andhra Pradesh",
            supportedLanguageCodes = listOf("te", "en", "ur", "hi", "ta", "kn")
        ),
        Region(
            name = "Telangana",
            supportedLanguageCodes = listOf("te", "ur", "en", "hi", "mr", "kn")
        ),
        Region(
            name = "Kerala",
            supportedLanguageCodes = listOf("ml", "en", "ta", "kn", "hi", "ar")
        ),
        Region(
            name = "Gujarat",
            supportedLanguageCodes = listOf("gu", "hi", "en", "ur", "mr", "sd")
        ),
        Region(
            name = "Rajasthan",
            supportedLanguageCodes = listOf("hi", "en", "raj", "ur", "pa", "sd")
        ),
        Region(
            name = "Madhya Pradesh",
            supportedLanguageCodes = listOf("hi", "en", "mr", "ur", "gu")
        ),
        Region(
            name = "Uttar Pradesh",
            supportedLanguageCodes = listOf("hi", "ur", "en", "pa", "bho", "awa")
        )
    )

    const val DEFAULT_REGION = "Karnataka"
    const val DEFAULT_LANGUAGE = "kn"

    fun getRegion(name: String): Region? {
        return REGIONS.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    fun getLanguagesForRegion(regionName: String): List<AppLanguage> {
        val region = getRegion(regionName) ?: REGIONS.first()
        return region.supportedLanguageCodes.mapNotNull { ALL_LANGUAGES[it] }
    }

    fun getLanguageByCode(code: String): AppLanguage {
        return ALL_LANGUAGES[code] ?: ALL_LANGUAGES["en"] ?: AppLanguage("en", "English", "English")
    }
}
