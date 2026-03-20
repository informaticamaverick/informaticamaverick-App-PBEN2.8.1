package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

/**
 * Representa una herramienta individual del Asistente Be.
 */
data class BeSmallActionModel(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val emoji: String? = null,
    val isVisible: Boolean = true,
    val isSelected: Boolean = false,
    val isDefault: Boolean = false,
    val tint: Color = Color.White,
    val onClick: () -> Unit
)

/**
 * Constructor de Herramientas Extendidas (BeBuild).
 * Implementa estabilización de fondo y alineación a la izquierda con efecto rebote.
 */
// --------------------------------FUNCION CON LONG PRESS DE BE -----------------------------
//-----------------------AQUI VAN LAS HERRAMIENTAS COMO COMPARTIR , ETC --------------------


@Composable
fun BeSmallActionsBuilder(
    isVisible: Boolean,
    actions: List<BeSmallActionModel>
) {
    val visibleActions = actions.filter { it.isVisible }

    // 🔥 ALINEACIÓN: Izquierda (BottomStart) para cumplir con el diseño solicitado
   // Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomStart) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        // --- 1. FONDO OSCURO (STABLE - SIN REBOTE) ---
        AnimatedVisibility(
            visible = isVisible && visibleActions.isNotEmpty(),
            // [ESTABILIZACIÓN] Tween lineal para evitar que el fondo herede el rebote de los iconos
            enter = fadeIn(animationSpec = tween(400, easing = LinearEasing)), 
            exit = fadeOut(animationSpec = tween(300, easing = LinearEasing))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp) // <-- [ALTURA] Ajusta la cobertura vertical del degradado
                    .offset(y = (-1).dp) // <-- [POSICIÓN] Eleva el fondo para que flote justo sobre el NavigationBar
                    .blur(15.dp)    // <-- [BLUR] Intensidad del desenfoque de fondo
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent, 
                                Color.Black.copy(alpha = 0.98f), // <-- [OSCURIDAD] 98% para máximo resalte de iconos
                                Color.Black 
                            )
                        )
                    )
            )
        }

        // --- 2. CONTENEDOR DE ICONOS (CON REBOTE DESDE ABAJO) ---
        AnimatedVisibility(
            visible = isVisible && visibleActions.isNotEmpty(),
            // 🔥 EFECTO REBOTE: Slide + Spring para que los iconos emerjan elásticamente desde abajo
            enter = slideInVertically(
                initialOffsetY = { it }, 
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow)
            ) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.Top
            ) {
                visibleActions.forEach { action -> SmallActionButton(action) }
            }
        }
    }
}
/*** Constructor de Barra por Defecto (Fast, Lic, Fav).* ACTUALIZADO: Ahora utiliza la misma configuración de fondo y altura que BeSmallActionsBuilder.*/
@Composable
fun BeDefaultActionsBand(
    isVisible: Boolean,
    actions: List<BeSmallActionModel>
) {
    val defaultActions = actions.filter { it.isDefault && it.isVisible }

    // 🔥 ALINEACIÓN: Izquierda (BottomStart) - IGUAL A SMALLACTIONS
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomStart) {

        // --- 1. FONDO OSCURO PARA DEFAULT (IGUALADO A SMALLACTIONS) ---
        AnimatedVisibility(
            visible = isVisible && defaultActions.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(400, easing = LinearEasing)),
            exit = fadeOut(animationSpec = tween(300, easing = LinearEasing))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp) // <-- [ALTURA] Antes 105.dp, ahora igualado a 135.dp
                    .offset(y = (-1).dp) // <-- [POSICIÓN] Mismo offset
                    .blur(15.dp)    // <-- [BLUR] Antes 12.dp, ahora igualado a 15.dp
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.98f), // <-- [OSCURIDAD] 98%
                                Color.Black
                            )
                        )
                    )
            )
        }
        // --- 2. ICONOS CON EFECTO REBOTE (IGUALADO A SMALLACTIONS) ---
        AnimatedVisibility(
            visible = isVisible && defaultActions.isNotEmpty(),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow) // <-- [REBOTE] Igualado a 0.65f
            ) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
                Row(
                   // modifier = Modifier
                     //   .fillMaxWidth() // Ocupa todo el ancho de la pantalla
                       // .padding(horizontal = 25.dp, vertical = 10.dp), // Más padding lateral para que no toquen los bordes
                  //  horizontalArrangement = Arrangement.SpaceBetween, // 🔥 Los separa equitativamente
                    modifier = Modifier.padding(6.dp), // <-- [MISMO PADDING INTERNO]
                   horizontalArrangement = Arrangement.spacedBy(5.dp), // <-- [MISMO ESPACIO] Antes 12.dp, ahora 14.dp
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    defaultActions.forEach { action -> SmallActionButton(action) }
                }
            }
        }
    }
/*** Botón individual con Emoji, Descripción y Sacudida (Shake).*/
//  ESTE ES EL BOTON BASE, LO USAN LAS 2 FUNCIONES ANTERIORES
@Composable
fun SmallActionButton(action: BeSmallActionModel) {
    if (action.id == "divider_v") {
        Box(
            modifier = Modifier
                .width(12.dp) // Reducido para mejor integración con el espaciado de 2dp
                .height(46.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
        return
    }

    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    val scale by animateFloatAsState(
        targetValue = if (action.isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "Scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(46.dp) // <-- Ajustado a 46.dp para que con spacedBy(2.dp) la separación sea exacta
    ) {
        Box(
            modifier = Modifier
                .size(46.dp) // <-- [TAMAÑO BOTÓN]
                .scale(scale)
                .graphicsLayer { rotationZ = rotation.value } // Aplica animación de sacudida
                .shadow(if (action.isSelected) 10.dp else 0.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF22D3EE))
                .clip(RoundedCornerShape(12.dp))
                .background(if (action.isSelected) Color(0xFF22D3EE).copy(alpha = 0.25f) else Color(0xFF1A1F26)) // <-- [FONDO BOTÓN]
                .border(1.dp, (if (action.isSelected) Color(0xFF22D3EE) else Color.White).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .clickable {
                    scope.launch {
                        // SECUENCIA DE SACUDIDA RÁPIDA
                        rotation.animateTo(15f, tween(50, easing = LinearEasing))
                        rotation.animateTo(-15f, tween(50, easing = LinearEasing))
                        rotation.animateTo(10f, tween(50, easing = LinearEasing))
                        rotation.animateTo(-10f, tween(50, easing = LinearEasing))
                        rotation.animateTo(0f, tween(50, easing = LinearEasing))
                        action.onClick()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (action.emoji != null) {
                Text(text = action.emoji, fontSize = 22.sp) // <-- [TAMAÑO EMOJI]
            } else {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.label,
                    tint = if (action.isSelected) Color(0xFF22D3EE) else action.tint,
                    modifier = Modifier.size(24.dp) // <-- [TAMAÑO ICONO VECTORIAL]
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = action.label,
            color = if(action.isSelected) Color(0xFF22D3EE) else Color.White.copy(alpha = 0.8f), // <-- [COLOR TEXTO]
            fontSize = 9.sp, // <-- [TAMAÑO TEXTO DESCRIPCIÓN]
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun BeSmallActionsBuilderPreview() {
    val sampleActions = listOf(
        BeSmallActionModel("4", Icons.Default.Share, "Compartir", emoji = "📤") {},
        BeSmallActionModel("5", Icons.Default.Delete, "Borrar", emoji = "🗑️", tint = Color.Red) {}
    )
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            BeSmallActionsBuilder(
                isVisible = true,
                actions = sampleActions
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun BeDefaultActionsBandPreview() {
    val sampleActions = listOf(
        BeSmallActionModel("1", Icons.Default.FlashOn, "Fast", emoji = "⚡", isDefault = true) {},
        BeSmallActionModel("2", Icons.Default.Gavel, "Licitación", emoji = "⚖️", isDefault = true) {},
        BeSmallActionModel("3", Icons.Default.Favorite, "Favoritos", emoji = "⭐", isDefault = true) {}
    )
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            BeDefaultActionsBand(
                isVisible = true,
                actions = sampleActions
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun SmallActionButtonPreview() {
    MyApplicationTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SmallActionButton(
                action = BeSmallActionModel(
                    id = "1",
                    icon = Icons.Default.FlashOn,
                    label = "Normal",
                    onClick = {}
                )
            )
            SmallActionButton(
                action = BeSmallActionModel(
                    id = "2",
                    icon = Icons.Default.Favorite,
                    label = "Selected",
                    isSelected = true,
                    onClick = {}
                )
            )
            SmallActionButton(
                action = BeSmallActionModel(
                    id = "3",
                    icon = Icons.Default.Share,
                    label = "With Emoji",
                    emoji = "🚀",
                    onClick = {}
                )
            )
        }
    }
}
