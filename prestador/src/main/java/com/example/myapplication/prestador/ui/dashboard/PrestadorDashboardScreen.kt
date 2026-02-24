package com.example.myapplication.prestador.ui.dashboard

import android.os.Build
import android.provider.CalendarContract
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.myapplication.prestador.ui.presupuesto.PresupuestosScreen
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewDashboard() {
    // Preview sin ViewModel (no es posible testearlo con Hilt)
    // MaterialTheme {
    //     PrestadorDashboardScreen(...)
    // }
}

@Composable
fun PrestadorDashboardScreen(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToServiceConfig: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToPresupuesto: () -> Unit = {},
    onNavigateToPresupuestoCita: (appointmentId: String) -> Unit = {},
    onNavigateToPresupuestos: () -> Unit = {},
    onNavigateToPromotion: () -> Unit = {},
    onNavigateToPromotionList: () -> Unit = {},
    onNavigateToThemeDemo: () -> Unit = {},
    chatSimulationViewModel: com.example.myapplication.prestador.viewmodel.ChatSimulationViewModel  // Pasar ViewModel
) {
    val colors = getPrestadorColors()
    var selectedTab by rememberSaveable { mutableStateOf(2) } // Inicia en Home (2), persiste en navegación
    var isInConversation by remember { mutableStateOf(false) }
    var targetChatUserId by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope() // Para manejar delays


    Scaffold(
        floatingActionButton = {
            // Mostrar FAB solo en Home (tab 2) y no en conversación
            if (selectedTab == 2 && !isInConversation) {
                FloatingActionButton(
                    onClick = onNavigateToPromotion,
                    containerColor = colors.primaryOrange,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Campaign,
                        contentDescription = "Crear promoción",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
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
                .background(colors.backgroundColor)
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
                    0 -> PresupuestoContent(onNavigateToPresupuesto = onNavigateToPresupuesto, onBackToHome = { selectedTab = 2 })
                    1 -> PrestadorCalendarScreen(
                        onBack = { selectedTab = 2 },
                        onNavigateToPresupuesto = onNavigateToPresupuestoCita,
                        onNavigateToChat = { clientId, clientName, newDate, newTime, existingAppointmentId ->
                            println("🔥 DASHBOARD: onNavigateToChat recibido")
                            println("🔥 ClientId: $clientId, Nombre: $clientName")
                            println("🔥 Nueva Fecha: $newDate, Nueva Hora: $newTime")
                            println("🔥 AppointmentId: $existingAppointmentId")
                            
                            // 🎯 USAR NUEVO MANAGER INMUTABLE
                            com.example.myapplication.prestador.viewmodel.AppointmentRescheduleManager.updateAppointmentProposal(
                                clientId = clientId,
                                appointmentId = existingAppointmentId,
                                newDate = newDate,
                                newTime = newTime
                            )
                            println("🔥 Mensaje actualizado a PENDING con nueva fecha/hora")

                            // Configurar qué chat abrir PRIMERO
                            targetChatUserId = clientId
                            println("🔥 targetChatUserId configurado: $targetChatUserId")
                            
                            // Delay para asegurar que el estado se actualiza antes de navegar
                            coroutineScope.launch {
                                delay(100) // Esperar 100ms
                                // Cambiar al tab de chat
                                selectedTab = 3
                                println("🔥 Tab cambiado a: $selectedTab (chat)")
                            }
                        }
                    )
                    2 -> InicioContent(
                        onNavigateToEditProfile = onNavigateToEditProfile,
                        onNavigateToServiceConfig = onNavigateToServiceConfig,
                        onLogout = onLogout,
                        onNavigateToPromotionList = onNavigateToPromotionList,
                        onNavigateToThemeDemo = onNavigateToThemeDemo
                    )
                    3 -> {
                        println("🔥 DASHBOARD: Renderizando tab de chat")
                        println("🔥 targetChatUserId actual: $targetChatUserId")
                        
                        // Efecto para manejar la navegación al chat específico
                        LaunchedEffect(targetChatUserId) {
                            if (targetChatUserId != null) {
                                println("🔥 LaunchedEffect: targetChatUserId = $targetChatUserId")
                                // Esperar un frame para que el chat se renderice
                                kotlinx.coroutines.delay(100)
                            }
                        }
                        
                        PrestadorChatScreen(
                            chatSimulationViewModel = chatSimulationViewModel,
                            onBack = {
                                selectedTab = 2
                                targetChatUserId = null
                            },
                            onInConversationChange = { isInConversation = it },
                            onNavigateToPresupuesto = onNavigateToPresupuesto,
                            initialChatUserId = targetChatUserId
                        )
                    }
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
    val colors = getPrestadorColors()
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
        modifier = Modifier
            .navigationBarsPadding(), // AÑADIDO: Respeta el espacio de gestos
        containerColor = colors.surfaceColor,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp) // Altura del contenido
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
                        colors.primaryOrange // Naranja intenso
                    } else {
                        colors.primaryOrange.copy(alpha = 0.7f) // Naranja medio
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
    val colors = getPrestadorColors()
    val selectedColor = colors.primaryOrange // Naranja
    val unselectedColor = colors.textSecondary // Gris

    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top, // Cambio: alineación superior
            modifier = Modifier
                .padding(top = 4.dp, bottom = 2.dp) // Menos padding
        ) {
            BadgedBox(
                badge = {
                    if (showBadge && badgeCount > 0) {
                        Badge(
                            containerColor = colors.primaryOrange,
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
                    modifier = Modifier.size(36.dp) // Más compacto
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) selectedColor else unselectedColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(0.dp)) // Sin espacio extra

            Text(
                text = label,
                fontSize = 10.sp, // Más pequeño
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) selectedColor else unselectedColor,
                maxLines = 1
            )
        }
    }
}

// ==================== CONTENIDO DE CADA TAB ====================

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PresupuestoContent(
    onNavigateToPresupuesto: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    // Mostrar directamente la lista de presupuestos sin TopBar
    PresupuestosScreen(
        onBack = onBackToHome, // Regresar al tab de Inicio
        onCrearNuevo = onNavigateToPresupuesto, // Navegar a crear presupuesto
        onVerDetalle = { presupuesto ->
            // TODO: Navegar a detalle
        },
        showTopBar = false // Ocultar TopBar porque ya está en el dashboard
    )
}

@Composable
fun InicioContent(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToServiceConfig: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToCreatePromo: () -> Unit = {},
    onNavigateToPromotionList: () -> Unit = {},
    onNavigateToThemeDemo: () -> Unit = {}
) {
    val colors = getPrestadorColors()
    var showMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        // Header naranja con avatar y menú
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.primaryOrange,
                            colors.primaryOrange.copy(alpha = 0.8f)
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
                                    .background(colors.success, shape = CircleShape)
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
                            modifier = Modifier.background(colors.surfaceColor)
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
                                            tint = colors.primaryOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Editar Perfil",
                                            fontSize = 14.sp,
                                            color = colors.textPrimary
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToEditProfile()
                                }
                            )
                            
                            HorizontalDivider(color = colors.divider)
                            
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            tint = colors.primaryOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Configurar Servicio",
                                            fontSize = 14.sp,
                                            color = colors.textPrimary
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToServiceConfig()
                                }
                            )
                            
                            HorizontalDivider(color = colors.divider)
                            
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = null,
                                            tint = colors.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Cerrar Sesión",
                                            fontSize = 14.sp,
                                            color = colors.error
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
                color = colors.textSecondary
            )
        }
    }
}

@Composable
fun CalendarioContent() {
    val colors = getPrestadorColors()
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
                            colors.primaryOrange,
                            colors.primaryOrange.copy(alpha = 0.7f)
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
            color = colors.primaryOrange
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Gestiona tus citas y horarios",
            fontSize = 16.sp,
            color = colors.textSecondary
        )
    }
}

@Composable
fun ChatContent() {
    val colors = getPrestadorColors()
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
                            colors.primaryOrange,
                            colors.primaryOrange.copy(alpha = 0.7f)
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
            color = colors.primaryOrange
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Conversa con tus clientes",
            fontSize = 16.sp,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Badge de mensajes pendientes
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.error.copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tienes 3 mensajes sin leer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.error
                )
            }
        }
    }
}

@Composable
fun NotificacionesContent() {
    val colors = getPrestadorColors()
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
                            colors.primaryOrange,
                            colors.primaryOrange.copy(alpha = 0.7f)
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
            color = colors.primaryOrange
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Mantente al día con alertas importantes",
            fontSize = 16.sp,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Badge de notificaciones pendientes
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.primaryOrange.copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tienes 5 notificaciones nuevas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.primaryOrange
                )
            }
        }
    }
}
