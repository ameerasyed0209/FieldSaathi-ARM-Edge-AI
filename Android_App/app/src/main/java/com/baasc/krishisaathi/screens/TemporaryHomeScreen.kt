package com.baasc.krishisaathi.screens

import androidx.compose.runtime.Composable
import com.baasc.krishisaathi.data.RegionLanguageData

@Composable
fun TemporaryHomeScreen(
    selectedRegion: String = RegionLanguageData.DEFAULT_REGION,
    selectedLanguageCode: String = RegionLanguageData.DEFAULT_LANGUAGE,
    onLogout: () -> Unit
) {
    HomeScreen(
        selectedRegion = selectedRegion,
        selectedLanguageCode = selectedLanguageCode,
        onNavigateToScan = {},
        onNavigateToAddDetails = {},
        onLogout = onLogout
    )
}
