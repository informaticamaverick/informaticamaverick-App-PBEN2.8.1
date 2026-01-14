package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Colores adaptativos para modo oscuro
data class AppColors(
    val backgroundColor: Color,
    val surfaceColor: Color,
    val textPrimaryColor: Color,
    val textSecondaryColor: Color,
    val dividerColor: Color,
    val accentBlue: Color = Color(0xFF3B82F6),
    val accentYellow: Color = Color(0xFFFBBF24),
    val accentRed: Color = Color(0xFFEF4444),
    val accentGreen: Color = Color(0xFF10B981)
)

// Esta función ahora es un alias para getThemeColors() en Theme.kt
// Mantenida por compatibilidad con código existente
@Composable
fun getAppColors(): AppColors {
    return getThemeColors()
}