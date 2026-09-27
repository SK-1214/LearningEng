package com.wordmemory.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/** Horizontal shake for a wrong answer (M5). Drive it with [shake] and apply [shakeOffset]. */
@Composable
fun rememberShake(): Animatable<Float, AnimationVector1D> = remember { Animatable(0f) }

suspend fun Animatable<Float, AnimationVector1D>.shake() {
    for (x in listOf(-28f, 28f, -20f, 20f, -10f, 10f, 0f)) animateTo(x, tween(durationMillis = 45))
}

fun Modifier.shakeOffset(shake: Animatable<Float, AnimationVector1D>): Modifier =
    offset { IntOffset(shake.value.roundToInt(), 0) }

/** Pops its content in with a bouncy scale when it first appears, e.g. "정답! 🎉" (M5). */
@Composable
fun PopIn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val scale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }
    Box(modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }) { content() }
}
