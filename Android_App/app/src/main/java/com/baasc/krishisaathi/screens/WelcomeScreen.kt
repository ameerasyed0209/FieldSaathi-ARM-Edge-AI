package com.baasc.krishisaathi.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baasc.krishisaathi.R
import com.baasc.krishisaathi.components.KrishiDropdownSelector
import com.baasc.krishisaathi.data.AppPreferencesRepository
import com.baasc.krishisaathi.data.RegionLanguageData
import com.baasc.krishisaathi.ui.theme.ButtonGreen
import com.baasc.krishisaathi.ui.theme.KrishiSaathiTheme
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    currentRegion: String,
    currentLanguageCode: String,
    onRegionAndLanguageChanged: (String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { AppPreferencesRepository.getInstance(context) }

    var selectedRegionName by remember(currentRegion) { mutableStateOf(currentRegion) }
    var selectedLanguageCode by remember(currentLanguageCode) { mutableStateOf(currentLanguageCode) }

    // List of 10 supported States
    val regionNames = remember { RegionLanguageData.REGIONS.map { it.name } }

    // Dynamic languages for the selected region
    val availableLanguages = remember(selectedRegionName) {
        RegionLanguageData.getLanguagesForRegion(selectedRegionName)
    }

    // Ensure selected language is valid for the current region
    LaunchedEffect(selectedRegionName) {
        val isStillSupported = availableLanguages.any { it.code == selectedLanguageCode }
        if (!isStillSupported) {
            val fallback = availableLanguages.firstOrNull { it.code == "en" }
                ?: availableLanguages.firstOrNull()
            if (fallback != null) {
                selectedLanguageCode = fallback.code
                onRegionAndLanguageChanged(selectedRegionName, fallback.code)
                scope.launch {
                    prefsRepo.saveRegionAndLanguage(selectedRegionName, fallback.code)
                }
            }
        }
    }

    val currentLanguage = RegionLanguageData.getLanguageByCode(selectedLanguageCode)

    fun handleRegionChange(newRegion: String) {
        selectedRegionName = newRegion
        val newAvailableLanguages = RegionLanguageData.getLanguagesForRegion(newRegion)
        val retainsLanguage = newAvailableLanguages.any { it.code == selectedLanguageCode }
        val newLangCode = if (retainsLanguage) {
            selectedLanguageCode
        } else {
            newAvailableLanguages.firstOrNull { it.code == "en" }?.code
                ?: newAvailableLanguages.first().code
        }
        selectedLanguageCode = newLangCode
        onRegionAndLanguageChanged(newRegion, newLangCode)
        scope.launch {
            prefsRepo.saveRegionAndLanguage(newRegion, newLangCode)
        }
    }

    fun handleLanguageChange(displayName: String) {
        val matchedLang = availableLanguages.firstOrNull { it.displayName == displayName }
        if (matchedLang != null) {
            selectedLanguageCode = matchedLang.code
            onRegionAndLanguageChanged(selectedRegionName, matchedLang.code)
            scope.launch {
                prefsRepo.saveLanguage(matchedLang.code)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen tropical foliage background
        Image(
            painter = painterResource(id = R.drawable.bg_tropical_leaves),
            contentDescription = "Tropical Leaves Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for contrast and readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.90f)
                        )
                    )
                )
        )

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top branding / logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_plant_leaf),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = R.string.app_name),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.6f),
                            offset = Offset(0f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Upper-middle headline text
            Text(
                text = stringResource(id = R.string.welcome_tagline),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 44.sp,
                    letterSpacing = (-0.5).sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(0f, 4f),
                        blurRadius = 8f
                    )
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Region and Language Selectors
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Region Dropdown
                KrishiDropdownSelector(
                    label = stringResource(id = R.string.select_region_label),
                    selectedValue = selectedRegionName,
                    options = regionNames,
                    onOptionSelected = { handleRegionChange(it) },
                    leadingIcon = Icons.Default.LocationOn
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Language Dropdown (dynamically filtered by Region)
                KrishiDropdownSelector(
                    label = stringResource(id = R.string.select_language_label),
                    selectedValue = currentLanguage.displayName,
                    options = availableLanguages.map { it.displayName },
                    onOptionSelected = { handleLanguageChange(it) },
                    leadingIcon = Icons.Default.Language
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Bottom actions area
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large rounded green "Sign In" button
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonGreen,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.sign_in),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.4.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "Create an account" clickable text
                Text(
                    text = stringResource(id = R.string.create_an_account),
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToRegister)
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
    KrishiSaathiTheme {
        WelcomeScreen(
            currentRegion = "Karnataka",
            currentLanguageCode = "kn",
            onRegionAndLanguageChanged = { _, _ -> },
            onNavigateToLogin = {},
            onNavigateToRegister = {}
        )
    }
}
