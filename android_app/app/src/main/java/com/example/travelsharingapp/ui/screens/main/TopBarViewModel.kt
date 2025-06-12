package com.example.travelsharingapp.ui.screens.main

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TopBarConfig(
    val title: String = "",
    val navigationIcon: (@Composable () -> Unit)? = null,
    val actions: (@Composable RowScope.() -> Unit)? = null,
    val floatingActionButton: (@Composable () -> Unit)? = null,
    val isVisible: Boolean = true
)

class TopBarViewModel : ViewModel() {
    private val _config = MutableStateFlow(TopBarConfig())
    val config: StateFlow<TopBarConfig> = _config.asStateFlow()

    private val _onNavigateAway = MutableStateFlow<((() -> Unit) -> Unit)?>(null)
    val onNavigateAway: StateFlow<((() -> Unit) -> Unit)?> = _onNavigateAway.asStateFlow()

    fun setConfig(
        title: String,
        navigationIcon: (@Composable () -> Unit)? = null,
        actions: (@Composable RowScope.() -> Unit)? = null,
        floatingActionButton: (@Composable () -> Unit)? = null,
        isVisible: Boolean = true
    ) {
        _config.value = TopBarConfig(title, navigationIcon, actions, floatingActionButton, isVisible)
    }

    fun setNavigateAwayAction(action: ((() -> Unit) -> Unit)?) {
        _onNavigateAway.value = action
    }
}