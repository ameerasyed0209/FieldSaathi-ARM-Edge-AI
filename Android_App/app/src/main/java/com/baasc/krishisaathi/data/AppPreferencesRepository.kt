package com.baasc.krishisaathi.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "krishisaathi_prefs")

class AppPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_SELECTED_REGION = stringPreferencesKey("selected_region")
        val KEY_SELECTED_LANGUAGE = stringPreferencesKey("selected_language")

        @Volatile
        private var INSTANCE: AppPreferencesRepository? = null

        fun getInstance(context: Context): AppPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val selectedRegionFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_REGION] ?: RegionLanguageData.DEFAULT_REGION
    }

    val selectedLanguageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_LANGUAGE] ?: RegionLanguageData.DEFAULT_LANGUAGE
    }

    suspend fun getSelectedRegion(): String {
        return context.dataStore.data.first()[KEY_SELECTED_REGION] ?: RegionLanguageData.DEFAULT_REGION
    }

    suspend fun getSelectedLanguage(): String {
        return context.dataStore.data.first()[KEY_SELECTED_LANGUAGE] ?: RegionLanguageData.DEFAULT_LANGUAGE
    }

    suspend fun saveRegion(region: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_REGION] = region
        }
    }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_LANGUAGE] = languageCode
        }
    }

    suspend fun saveRegionAndLanguage(region: String, languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_REGION] = region
            preferences[KEY_SELECTED_LANGUAGE] = languageCode
        }
    }
}
