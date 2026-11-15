package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "recap_settings"
)

data class AppSettings(
    val backendUrl: String,
    val creatorName: String,
    val defaultQuality: String,
    val compressBeforeUpload: Boolean
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val BACKEND_URL = stringPreferencesKey("backend_url")
        val CREATOR_NAME = stringPreferencesKey("creator_name")
        val DEFAULT_QUALITY = stringPreferencesKey("default_quality")
        val COMPRESS_BEFORE_UPLOAD = booleanPreferencesKey("compress_before_upload")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        val url = if (prefs.contains(Keys.BACKEND_URL)) {
            prefs[Keys.BACKEND_URL].orEmpty()
        } else {
            BuildConfig.BASE_URL
        }
        AppSettings(
            backendUrl = url,
            creatorName = prefs[Keys.CREATOR_NAME] ?: "Recap ဖန်တီးသူ",
            defaultQuality = prefs[Keys.DEFAULT_QUALITY] ?: "720",
            compressBeforeUpload = prefs[Keys.COMPRESS_BEFORE_UPLOAD] ?: true
        )
    }

    suspend fun setBackendUrl(url: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.BACKEND_URL] = url.trim().trimEnd('/')
        }
    }

    suspend fun clearBackendUrl() {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.BACKEND_URL] = ""
        }
    }

    suspend fun resetBackendUrlToDefault() {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.BACKEND_URL] = BuildConfig.BASE_URL.trim().trimEnd('/')
        }
    }

    suspend fun setCreatorName(name: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.CREATOR_NAME] = name.trim().ifEmpty { "Recap ဖန်တီးသူ" }
        }
    }

    suspend fun setDefaultQuality(quality: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.DEFAULT_QUALITY] = if (quality == "480") "480" else "720"
        }
    }

    suspend fun setCompressBeforeUpload(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.COMPRESS_BEFORE_UPLOAD] = enabled
        }
    }
}
