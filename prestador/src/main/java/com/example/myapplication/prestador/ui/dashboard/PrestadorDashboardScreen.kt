package com.example.myapplication.prestador.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Preview(showBackground = true)
@Composable
fun PreviewDashboard() {
    PrestadorDashboardScreen()
}

@Composable
fun PrestadorDashboardScreen() {
    var selectedTab by remember { mutableStateOf(1) } // Inicia en Home (1)

    Scaffold(
        bottomBar = {
            PrestadorBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFFF8F3))
        ) {
            // Contenido según el tab seleccionado
            when (selectedTab) {
                0 -> CalendarioContent()
                1 -> InicioContent()
                2 -> ChatContent()
                3 -> NotificacionesContent()
            }
        }
    }
}

@Composable
fun PrestadorBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    // Animación para el botón central
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    BottomAppBar(
        modifier = Modifier.height(70.dp),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Calendario (Izquierda)
            BottomNavItem(
                icon = Icons.Default.DateRange,
                label = "Calendario",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )

            // Espaciador
            Spacer(modifier = Modifier.width(8.dp))

            // Inicio (Centro - Botón destacado)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .scale(if (selectedTab == 1) pulseScale else 1f),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = { onTabSelected(1) },
                    containerColor = if (selectedTab == 1) {
                        Color(0xFFFF6B35) // Naranja intenso
                    } else {
                        Color(0xFFFF9F66) // Naranja medio
                    },
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Inicio",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Espaciador
            Spacer(modifier = Modifier.width(8.dp))

            // Chat (Derecha centro)
            BottomNavItem(
                icon = Icons.Default.Email,
                label = "Chat",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                showBadge = true,
                badgeCount = 3
            )

            // Notificaciones (Extremo derecha)
            BottomNavItem(
                icon = Icons.Default.Notifications,
                label = "Alertas",
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                showBadge = true,
                badgeCount = 5
            )
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    showBadge: Boolean = false,
    badgeCount: Int = 0
) {
    val selectedColor = Color(0xFFFF6B35) // Naranja
    val unselectedColor = Color(0xFF9CA3AF) // Gris

    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            BadgedBox(
                badge = {
                    if (showBadge && badgeCount > 0) {
                        Badge(
                            containerColor = Color(0xFFEF4444), // Rojo
                            contentColor = Color.White
                        ) {
                            Text(
                                text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            ) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) selectedColor else unselectedColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) selectedColor else unselectedColor
            )
        }
    }
}

// ==================== CONTENIDO DE CADA TAB ====================

@Composable
fun InicioContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Indicador visual con gradiente naranja
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF6B35),
                            Color(0xFFFF9F66)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Inicio",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6B35)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Panel principal del prestador",
            fontSize = 16.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun CalendarioContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF6B35),
                            Color(0xFFFF9F66)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Calendario",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6B35)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Gestiona tus citas y horarios",
            fontSize = 16.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun ChatContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF6B35),
                            Color(0xFFFF9F66)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Chat",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6B35)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Conversa con tus clientes",
            fontSize = 16.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Badge de mensajes pendientes
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEF4444).copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tienes 3 mensajes sin leer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
fun NotificacionesContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF6B35),
                            Color(0xFFFF9F66)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Notificaciones",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6B35)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Mantente al día con alertas importantes",
            fontSize = 16.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Badge de notificaciones pendientes
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFF6B35).copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF6B35),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tienes 5 notificaciones nuevas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF6B35)
                )
            }
        }
    }
}
