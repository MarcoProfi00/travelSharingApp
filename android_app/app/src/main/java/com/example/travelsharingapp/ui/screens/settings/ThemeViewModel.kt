package com.example.travelsharingapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.travelsharingapp.data.repository.ThemeRepository
import com.example.travelsharingapp.ui.theme.ThemeSetting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val themeRepository: ThemeRepository) : ViewModel() {

    val themeSetting: StateFlow<ThemeSetting> = themeRepository.currentThemeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeSetting.SYSTEM
        )

    fun updateThemeSetting(newThemeSetting: ThemeSetting) {
        viewModelScope.launch {
            themeRepository.setTheme(newThemeSetting)
        }
    }
}

class ThemeViewModelFactory(
    private val themeRepository: ThemeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel > create(modelClass: Class<T>): T {
        return modelClass
            .getConstructor(ThemeRepository::class.java)
            .newInstance(themeRepository)
    }
}