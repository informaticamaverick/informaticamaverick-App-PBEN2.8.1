package com.example.myapplication.prestador.ui.dashboard

import android.provider.CalendarContract
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.chat.PrestadorChatScreen
import com.example.myapplication.prestador.ui.calendar.PrestadorCalendarScreen


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewDashboard() {
    MaterialTheme {
        PrestadorDashboardScreen(
            onNavigateToEditProfile = {},
            onNavigateToServiceConfig = {},
            onLogout = {}
        )
    }
}

@Composable
fun PrestadorDashboardScreen(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToServiceConfig: () -> Unit = {},
    onLogout: () -> Unit = {}, // Nuevo parametro
    onNavigateToPresupuesto: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(2) } // Inicia en Home (2)
    var isInConversation by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            // Ocultar barra de navegación cuando se está en una conversación individual
            if (!(selectedTab == 3 && isInConversation)) {
                PrestadorBottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFFF8F3))
        ) {
            // Animación suave al cambiar de tab
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300)) +
                        scaleOut(targetScale = 0.92f, animationSpec = tween(300))
                },
                label = "tab_transition"
            ) { currentTab ->
                // Contenido según el tab seleccionado
                when (currentTab) {
                    0 -> PresupuestoContent()
                    1 -> PrestadorCalendarScreen(
                        onBack = { selectedTab = 2 }
                    )
                    2 -> InicioContent(
                        onNavigateToEditProfile = onNavigateToEditProfile,
                        onNavigateToServiceConfig = onNavigateToServiceConfig,
                        onLogout = onLogout
                    )
                    3 -> PrestadorChatScreen(
                        onBack = { selectedTab = 2 },
                        onInConversationChange = { isInConversation = it },
                        onNavigateToPresupuesto = onNavigateToPresupuesto
                    )
                    4 -> NotificacionesContent()
                }
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
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Presupuesto (Extremo izquierdo)
            BottomNavItem(
                icon = Icons.Default.Edit,
                label = "Presupuesto",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )

            // Calendario
            BottomNavItem(
                icon = Icons.Default.DateRange,
                label = "Calendario",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )

            // Inicio (Centro - Botón destacado)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .scale(if (selectedTab == 2) pulseScale else 1f),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = { onTabSelected(2) },
                    containerColor = if (selectedTab == 2) {
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

            // Chat
            BottomNavItem(
                icon = Icons.Default.Email,
                label = "Chat",
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                showBadge = true,
                badgeCount = 3
            )

            // Notificaciones (Extremo derecha)
            BottomNavItem(
                icon = Icons.Default.Notifications,
                label = "Alertas",
                isSelected = selectedTab == 4,
                onClick = { onTabSelected(4) },
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
                            containerColor = Color(0xFFFF6B35), // Rojo
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
fun PresupuestoContent() {
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
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Presupuesto",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6B35)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Gestiona tus cotizaciones",
            fontSize = 16.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun InicioContent(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToServiceConfig: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F3))
    ) {
        // Header naranja con avatar y menú
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF6B35),
                            Color(0xFFFF9F66)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 48.dp)
        ) {
            Column {
                // Fila superior: Avatar + Nombre a la izquierda
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nombre y estado (IZQUIERDA)
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Hola, Prestador", // TODO: Obtener nombre del usuario
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF10B981), shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Disponible para Fast",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    
                    // Avatar a la derecha
                    Box {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { showMenu = !showMenu }
                            ) {
                                Text(
                                    text = "P", // TODO: Obtener inicial del usuario
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        // Menú desplegable
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFFFF6B35),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Editar Perfil",
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToEditProfile()
                                }
                            )
                            
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            tint = Color(0xFFFF6B35),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Configurar Servicio",
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToServiceConfig()
                                }
                            )
                            
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Cerrar Sesión",
                                            fontSize = 14.sp,
                                            color = Color(0xFFEF4444)
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onLogout() //Llamar al callback
                                    // TODO: Cerrar sesión
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Contenido del dashboard (temporal)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Contenido del Dashboard",
                fontSize = 16.sp,
                color = Color(0xFF6B7280)
            )
        }
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
