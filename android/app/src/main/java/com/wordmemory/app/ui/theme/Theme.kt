package com.wordmemory.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Correct = Color(0xFF2E9E5B)
val Wrong = Color(0xFFE0533D)
val Star = Color(0xFFFFB300)

private val colors = lightColorScheme(
    primary = Color(0xFFFF8A3D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE3CC),
    onPrimaryContainer = Color(0xFF4A2200),
    secondary = Color(0xFF3D8BFF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8E7FF),
    onSecondaryContainer = Color(0xFF0B2A5C),
    tertiaryContainer = Color(0xFFD9F2E1),
    onTertiaryContainer = Color(0xFF0E3B21),
    background = Color(0xFFFFF8EE),
    onBackground = Color(0xFF2B2118),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2B2118),
    surfaceVariant = Color(0xFFF3E9DC),
    onSurfaceVariant = Color(0xFF6B5E51),
)

/** Large type throughout: the main user is a first grader on a 12.4" tablet (wiki/tech/target-device.md). */
private val typography = Typography(
    displayLarge = TextStyle(fontSize = 88.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
    displayMedium = TextStyle(fontSize = 56.sp, fontWeight = FontWeight.Bold),
    displaySmall = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 20.sp),
    bodyMedium = TextStyle(fontSize = 18.sp),
    labelLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
)

@Composable
fun WordMemoryTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
