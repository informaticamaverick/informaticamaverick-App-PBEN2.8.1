package com.example.myapplication.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Representa una herramienta individual del Asistente Be.
 * @param isSelected: Define si la herramienta está activa (ej.: multiselección activada).
 * @param isVisible: El ViewModel decide si mostrarla según la pantalla.
 * @param isDefault: Indica si es una acción que aparece por defecto (fuera de la caja BeBuild).
 */
data class BeSmallActionModel(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val isVisible: Boolean = true,
    val isSelected: Boolean = false, // Maneja el estado visual de "Activo"
    val isDefault: Boolean = false, // 🔥 Identifica acciones persistentes (fast, licit, fav)
    val tint: Color = Color.White,
    val onClick: () -> Unit
)

/**
 * Constructor de Herramientas de Be (SmallActions).
 * Aparece siempre a la IZQUIERDA de Be.
 * Si hay más de 4 herramientas, se organiza automáticamente en 2 filas.
 */
@Composable
fun BeSmallActionsBuilder(
    isVisible: Boolean, // Controlado por LongPress o estado de pantalla
    actions: List<BeSmallActionModel>
) {
    // Filtramos solo las que el ViewModel dice que deben verse
    val visibleActions = actions.filter { it.isVisible }

    AnimatedVisibility(
        visible = isVisible && visibleActions.isNotEmpty(),
        // Aparece expandiéndose desde la derecha hacia la izquierda (donde está Be)
        enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End) + scaleIn(transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f)),
        exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End) + scaleOut(transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f))
    ) {
        // Contenedor principal de herramientas
        Surface(
            modifier = Modifier
                .padding(end = 12.dp) // Separación de seguridad con el cuerpo de Be
                .shadow(12.dp, RoundedCornerShape(20.dp)),
            color = Color(0xFF0A0E14).copy(alpha = 0.85f), // Fondo oscuro traslúcido
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            // Lógica de filas: Si hay más de 4, usamos un FlowRow o Column de Rows
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Dividimos la lista en grupos de 4 para crear las filas
                val rows = visibleActions.chunked(4)

                rows.forEach { rowActions ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowActions.forEach { action ->
                            SmallActionButton(action)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Botón individual con animaciones de estado
 * MODIFICACIÓN: Cambiado a public para uso en BeAssistant.kt
 */
@Composable
fun SmallActionButton(action: BeSmallActionModel) {
    // --- ANIMACIONES VISUALES ---

    // 1. Escala: Se infla un 20% si está seleccionado (isSelected)
    val scale by animateFloatAsState(
        targetValue = if (action.isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "Scale"
    )

    // 2. Color de fondo: Cambia a Cyan si está activo
    val backgroundColor by animateColorAsState(
        targetValue = if (action.isSelected) Color(0xFF22D3EE).copy(alpha = 0.2f) else Color(0xFF1A1F26),
        label = "BgColor"
    )

    // 3. Color del icono y borde: Brilla si está activo
    val contentColor by animateColorAsState(
        targetValue = if (action.isSelected) Color(0xFF22D3EE) else action.tint,
        label = "ContentColor"
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .scale(scale) // Aplicamos la escala animada
            .shadow(if (action.isSelected) 8.dp else 0.dp, CircleShape)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = contentColor.copy(alpha = if (action.isSelected) 0.6f else 0.15f),
                shape = CircleShape
            )
            .clickable { action.onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Efecto de brillo exterior si está seleccionado
        if (action.isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.radialGradient(listOf(Color(0xFF22D3EE).copy(0.15f), Color.Transparent)))
            )
        }

        Icon(
            imageVector = action.icon,
            contentDescription = action.label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun BeSmallActionsBuilderPreview() {
    val sampleActions = listOf(
        BeSmallActionModel(
            id = "1",
            icon = Icons.Default.Add,
            label = "Añadir",
            onClick = {}
        ),
        BeSmallActionModel(
            id = "2",
            icon = Icons.Default.Settings,
            label = "Configurar",
            isSelected = true,
            onClick = {}
        ),
        BeSmallActionModel(
            id = "3",
            icon = Icons.Default.Person,
            label = "Perfil",
            onClick = {}
        ),
        BeSmallActionModel(
            id = "4",
            icon = Icons.Default.Search,
            label = "Buscar",
            onClick = {}
        ),
        BeSmallActionModel(
            id = "5",
            icon = Icons.Default.Notifications,
            label = "Alertas",
            onClick = {}
        )
    )

    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF05070A))
                .padding(20.dp),
            contentAlignment = Alignment.Center
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
fun SmallActionButtonPreview() {
    MyApplicationTheme {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SmallActionButton(
                action = BeSmallActionModel(
                    id = "1",
                    icon = Icons.Default.Settings,
                    label = "Normal",
                    onClick = {}
                )
            )
            SmallActionButton(
                action = BeSmallActionModel(
                    id = "2",
                    icon = Icons.Default.Settings,
                    label = "Selected",
                    isSelected = true,
                    onClick = {}
                )
            )
        }
    }
}
