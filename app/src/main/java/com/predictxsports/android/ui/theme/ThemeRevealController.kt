package com.predictxsports.android.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

object ThemeRevealController {
    var active by mutableStateOf(false)
        private set

    fun reveal() {
        active = true
    }

    fun end() { active = false }
}

@Composable
fun ThemeRevealOverlay(
    visible: Boolean = ThemeRevealController.active,
    onPeak: () -> Unit
) {
    if (!visible) return

    val anim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        anim.animateTo(1f, tween(180))
        onPeak()
        delay(80)
        anim.animateTo(0f, tween(220))
        ThemeRevealController.end()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawRect(Color.Black.copy(alpha = anim.value))
        }
    }
}