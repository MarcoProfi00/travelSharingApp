package com.example.travelsharingapp.utils

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import com.example.travelsharingapp.ui.screens.main.LocalWindowSizeClass

@Composable
fun shouldUseTabletLayout(): Boolean {
    val windowSizeClass = LocalWindowSizeClass.current

    return windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium &&
            windowSizeClass.heightSizeClass > WindowHeightSizeClass.Compact
}