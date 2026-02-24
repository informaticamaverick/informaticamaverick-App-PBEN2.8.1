package com.example.myapplication.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
//Colores de fondo y texto - MODO OSCUOR
val BackgroundDark = Color(0xFF0F172A) //Fondo oscuro principal
val SurfaceDark = Color(0XFF1E293B) //sURFACE OSCURO(cards, inuts)
val SurfaceDarkElevated = Color(0xFF335155) //Surface Elevado
val BorderDark = Color(0xFF475569) //Bordes en modo oscuro
val TextPrimaryLigth = Color (0xFFF1F5F9)
val TextSecondaryLigth = Color(0xFF94A3B8)
val DividerDark = Color(0xFF7C2D12)
val ChipTextDark = Color(0xFFFED7AA)

// Esta función ahora es un alias para getThemeColors() en Theme.kt
// Mantenida por compatibilidad con código existente
@Composable
fun getAppColors(): AppColors {
    return getThemeColors()
}
