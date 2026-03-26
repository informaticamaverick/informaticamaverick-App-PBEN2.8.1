package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val onClick: () -> Unit = {}
)

@Composable
fun BeSmallActionsBuilder(
    isVisible: Boolean,
    actions: List<BeSmallActionModel>,
    shouldShowBottomBar: Boolean = true,
    toolboxKey: String = "default" // 🔥 USADO PARA AJUSTES CONTEXTUALES
) {
    val visibleActions = actions.filter { it.isVisible }
    val isProfileContext = toolboxKey.startsWith("profile")
    
    // --- AJUSTE DE ALTURA Y PADDING SEGÚN CONTEXTO (PERFIL) ---
    val toolboxHeight = if (isProfileContext) 110.dp else if (shouldShowBottomBar) 135.dp else 170.dp
    val sidePadding = if (isProfileContext) 0.dp else 20.dp

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomStart) {
        // --- 1. FONDO OSCURO ---
        AnimatedVisibility(
            visible = isVisible && visibleActions.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(toolboxHeight)
                    .graphicsLayer { if (!shouldShowBottomBar && !isProfileContext) translationY = 30f }
                    .blur(15.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.98f), Color.Black, Color.Black)
                        )
                    )
            )
        }

        // --- 2. CONTENEDOR CON ANIMACIÓN DE CAMBIO DE CONTENIDO ---
        AnimatedVisibility(
            visible = isVisible && visibleActions.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(dampingRatio = 0.65f)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AnimatedContent(
                targetState = toolboxKey,
                transitionSpec = {
                    (slideInVertically(tween(500)) { it } + fadeIn(tween(500)))
                        .togetherWith(slideOutVertically(tween(400)) { it } + fadeOut(tween(400)))
                },
                label = "ToolboxChangeAnimation"
            ) { _ ->
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .then(if (!shouldShowBottomBar) Modifier.navigationBarsPadding() else Modifier),
                    contentPadding = PaddingValues(horizontal = sidePadding), // 🔥 APLICAMOS PADDING DINÁMICO
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(visibleActions, key = { it.id }) { action ->
                        Box(modifier = Modifier.animateItem()) {
                            SmallActionButton(action)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BeDefaultActionsBand(
    isVisible: Boolean,
    actions: List<BeSmallActionModel>,
    shouldShowBottomBar: Boolean = true,
    toolboxKey: String = "default" 
) {
    val defaultActions = actions.filter { it.isDefault && it.isVisible }
    val isProfileContext = toolboxKey.startsWith("profile")
    
    // --- AJUSTE DE ALTURA Y PADDING SEGÚN CONTEXTO (PERFIL) ---
    val toolboxHeight = if (isProfileContext) 110.dp else if (shouldShowBottomBar) 135.dp else 170.dp
    val sidePadding = if (isProfileContext) 0.dp else 20.dp

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomStart) {
        // --- 1. FONDO ---
        AnimatedVisibility(
            visible = isVisible && defaultActions.isNotEmpty(),
            enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut(tween(300)) + slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(toolboxHeight)
                    .graphicsLayer { if (!shouldShowBottomBar && !isProfileContext) translationY = 30f }
                    .blur(15.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.98f), Color.Black, Color.Black)
                        )
                    )
            )
        }

        // --- 2. ICONOS CON ANIMACIÓN DE CAMBIO ---
        AnimatedVisibility(
            visible = isVisible && defaultActions.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(dampingRatio = 0.65f)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AnimatedContent(
                targetState = toolboxKey,
                transitionSpec = {
                    (slideInVertically(tween(500)) { it } + fadeIn(tween(500)))
                        .togetherWith(slideOutVertically(tween(400)) { it } + fadeOut(tween(400)))
                },
                label = "DefaultActionsChangeAnimation"
            ) { _ ->
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .then(if (!shouldShowBottomBar) Modifier.navigationBarsPadding() else Modifier),
                    contentPadding = PaddingValues(horizontal = sidePadding), // 🔥 APLICAMOS PADDING DINÁMICO
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(defaultActions, key = { it.id }) { action ->
                        Box(modifier = Modifier.animateItem()) {
                            SmallActionButton(action)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmallActionButton(action: BeSmallActionModel) {
    if (action.id.startsWith("divider_v")) {
        Box(modifier = Modifier.width(12.dp).height(46.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))
        }
        return
    }

    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    val scale by animateFloatAsState(
        targetValue = if (action.isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "Scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(46.dp)) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .scale(scale)
                .graphicsLayer { rotationZ = rotation.value }
                .shadow(if (action.isSelected) 10.dp else 0.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF22D3EE))
                .clip(RoundedCornerShape(12.dp))
                .background(if (action.isSelected) Color(0xFF22D3EE).copy(alpha = 0.25f) else Color(0xFF1A1F26))
                .border(1.dp, (if (action.isSelected) Color(0xFF22D3EE) else Color.White).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .clickable {
                    scope.launch {
                        rotation.animateTo(15f, tween(50))
                        rotation.animateTo(-15f, tween(50))
                        rotation.animateTo(0f, tween(50))
                        action.onClick()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (action.emoji != null) {
                Text(text = action.emoji, fontSize = 22.sp)
            } else {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.label,
                    tint = if (action.isSelected) Color(0xFF22D3EE) else action.tint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = action.label,
            color = if(action.isSelected) Color(0xFF22D3EE) else Color.White.copy(alpha = 0.8f),
            fontSize = 9.sp,
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
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp), contentAlignment = Alignment.BottomStart) {
            BeSmallActionsBuilder(isVisible = true, actions = sampleActions)
        }
    }
}
