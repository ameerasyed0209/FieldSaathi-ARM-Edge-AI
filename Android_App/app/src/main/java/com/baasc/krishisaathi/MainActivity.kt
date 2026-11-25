package com.baasc.krishisaathi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.baasc.krishisaathi.data.AppPreferencesRepository
import com.baasc.krishisaathi.data.RegionLanguageData
import com.baasc.krishisaathi.navigation.KrishiSaathiNavGraph
import com.baasc.krishisaathi.ui.theme.CardBackground
import com.baasc.krishisaathi.ui.theme.KrishiSaathiTheme
import com.baasc.krishisaathi.util.LocalizedApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefsRepo = AppPreferencesRepository.getInstance(applicationContext)

        setContent {
            val selectedRegion by prefsRepo.selectedRegionFlow.collectAsState(
                initial = RegionLanguageData.DEFAULT_REGION
            )
            val selectedLanguage by prefsRepo.selectedLanguageFlow.collectAsState(
                initial = RegionLanguageData.DEFAULT_LANGUAGE
            )
            val scope = rememberCoroutineScope()

            LocalizedApp(languageCode = selectedLanguage) {
                KrishiSaathiTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = CardBackground
                    ) {
                        KrishiSaathiNavGraph(
                            currentRegion = selectedRegion,
                            currentLanguageCode = selectedLanguage,
                            onRegionAndLanguageChanged = { region, lang ->
                                scope.launch {
                                    prefsRepo.saveRegionAndLanguage(region, lang)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
