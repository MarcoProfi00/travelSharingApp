package com.example.travelsharingapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.travelsharingapp.ui.theme.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeRepository(context: Context) {
    private val appContext = context.applicationContext

    val currentThemeFlow: Flow<ThemeSetting> = appContext.dataStoreInstance.data.map { preferences ->
        ThemeSetting.valueOf(preferences[ThemePreferenceKeys.THEME_KEY] ?: ThemeSetting.SYSTEM.name)
    }

    suspend fun setTheme(theme: ThemeSetting) {
        appContext.dataStoreInstance.edit { settings ->
            settings[ThemePreferenceKeys.THEME_KEY] = theme.name
        }
    }
}