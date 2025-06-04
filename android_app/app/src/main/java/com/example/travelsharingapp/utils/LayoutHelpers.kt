package com.example.travelsharingapp.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.travelsharingapp.ui.screens.main.LocalWindowSizeClass

@Composable
fun shouldUseTabletLayout(): Boolean {
    val windowSizeClass = LocalWindowSizeClass.current

    return windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium &&
            windowSizeClass.heightSizeClass > WindowHeightSizeClass.Compact
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}