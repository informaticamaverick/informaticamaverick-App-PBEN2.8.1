package com.example.myapplication.prestador.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.util.TableInfo
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.DashboardUiState
import com.example.myapplication.prestador.viewmodel.DashboardViewModel
import kotlinx.coroutines.delay

@Composable
fun InicioContent(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToServiceConfig: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToCreatePromo: () -> Unit = {},
    onNavigateToPromotionList: () -> Unit = {},
    onNavigateToThemeDemo: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToPresupuesto: () -> Unit = {},
    onNavigateToPresupuestos: () -> Unit = {},
    onNavigateToChat: (clientId: String) -> Unit = {}
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    InicioScreen(
        state = state,
        onNavigateToEditProfile = onNavigateToEditProfile,
        onNavigateToServiceConfig = onNavigateToServiceConfig,
        onLogout = onLogout,
        onNavigateToCalendar = onNavigateToCalendar,
        onNavigateToPresupuesto = onNavigateToPresupuesto,
        onNavigateToPresupuestos = onNavigateToPresupuestos,
        onNavigateToChat = onNavigateToChat
    )
}

@Composable
private fun InicioScreen(
    state: DashboardUiState,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToServiceConfig: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToPresupuesto: () -> Unit,
    onNavigateToPresupuestos: () -> Unit,
    onNavigateToChat: (clientId: String) -> Unit
) {
    val colors = getPrestadorColors()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.primaryOrange,
                            colors.primaryOrange.copy(alpha = 0.85f)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${state.saludo}, Prestador",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(colors.success, CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Disponible para Fast",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Box {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Text(
                                    text = "P",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(colors.surfaceColor)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Editar Perfil", fontSize = 14.sp, color = colors.textPrimary)
                                    }
                                },
                                onClick = { showMenu = false; onNavigateToEditProfile() }
                            )
                            HorizontalDivider(color = colors.divider)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Settings, null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Configurar Servicio", fontSize = 14.sp, color = colors.textPrimary)
                                    }
                                },
                                onClick = { showMenu = false; onNavigateToServiceConfig() }
                            )
                            HorizontalDivider(color = colors.divider)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ExitToApp, null, tint = colors.error, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("Cerrar Sesion", fontSize = 14.sp, color = colors.error)
                                    }
                                },
                                onClick = { showMenu = false; onLogout() }
                            )
                        }
                    }
                }

                // Subtitulo
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "¡Vamos a trabajar!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Tienes ${state.citasHoy} trabajo${if (state.citasHoy != 1) "s" else ""} programado${if (state.citasHoy != 1) "s" else ""} para hoy",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // GANANCIAS + CALIFICACION
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("GANANCIAS (SEM)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
                        Icon(Icons.Default.AttachMoney, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "$ ${"%,.0f".format(state.gananciasSemanales)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CALIFICACION", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "4.9 \u2605",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // CARRUSEL
        val cardSuscripcion = when (state.serviceType)
        {
            "PROFESSIONAL" -> CarruselItem(
                titulo = "Agenda Pro",
                subtitulo = "Reservas online 24/7, recordatorios autmaticos y estadisticas de tu agenda",
                icono = Icons.Default.CalendarMonth,
                gradiente = Brush.horizontalGradient(listOf(Color(0xFF5C6BC0), Color(0xFF798CB)))
            )
            "RENTAL" -> CarruselItem(
                titulo = "Vitrina Premium",
                subtitulo = "Destaca tus publicaciones, sube hasta 20 fotos y ve cuantos te visitan.",
                icono = Icons.Default.Star,
                gradiente = Brush.horizontalGradient(listOf(Color(0xFF26A69A), Color(0xFF4DB6AC)))
            )
            else -> CarruselItem(
                titulo = "Servicio Fast",
                subtitulo = "Aparece primero en urgencias y recibe solicitudes de clientes cercanos.",
                icono = Icons.Default.Bolt,
                gradiente = Brush.horizontalGradient(listOf(Color(0xFFFF6B35), Color(0xFFE53935)))
            )
        }
        val carruselItems = listOf(
            CarruselItem(
                titulo = "Consigue mas clientes",
                subtitulo = "Completa tu perfil al 100% hoy mismo.",
                icono = Icons.Default.TrendingUp,
                gradiente = Brush.horizontalGradient(listOf(Color(0xFF4A90D9), Color(0xFF7B4FD4)))
            ),
            CarruselItem(
                titulo = "Activa tus promociones",
                subtitulo = "Llega a mas usuarios con ofertas especiales.",
                icono = Icons.Default.LocalOffer,
                gradiente = Brush.horizontalGradient(listOf(Color(0xFFFF6B35), Color(0xFFFF8C42)))
            ),
            cardSuscripcion,
            CarruselItem(
                titulo = "Sin Anuncios",
                subtitulo = "Experiencia limpia, sin banners ni interrupciones. Suscripción mensual.",
                icono = Icons.Default.DoNotDisturb,
                gradiente = Brush.horizontalGradient(listOf(Color(0xFF37474F), Color(0xFF546E7A)))
            )
        )
        val pagerState = rememberPagerState(pageCount = { carruselItems.size })

        LaunchedEffect(pagerState) {
            while (true) {
                delay(4_000L)
                val nextPage = (pagerState.currentPage + 1) % carruselItems.size

                pagerState.animateScrollToPage(nextPage)
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val item = carruselItems[page]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(item.gradiente)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    //Icono decorativo de fondo
                    Icon(
                        imageVector = item.icono,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterEnd)
                    )

                    //Contenido
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = item.icono,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .padding(6.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = item.titulo,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.subtitulo,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Dots indicador
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(carruselItems.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (pagerState.currentPage == index) 20.dp else 6.dp, 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (pagerState.currentPage == index) colors.primaryOrange
                                else colors.primaryOrange.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // TU PROXIMO SERVICIO
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Tú proximo servicio",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(10.dp))
            if (state.proximaCita != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                    elevation = CardDefaults.cardElevation(4.dp),
                    onClick = onNavigateToCalendar
                ) {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        // Franja naranja borde izquierdo
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .fillMaxHeight()
                                .background(
                                    colors.primaryOrange,
                                    RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(52.dp)
                        ) {
                            Text(
                                text = "HOY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryOrange
                            )
                            Text(
                                text = state.proximaCita.time,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryOrange
                            )
                        }
                        // Línea divisoria gris
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .width(1.dp)
                                .height(52.dp)
                                .background(Color(0xFF424242).copy(alpha = 0.25f), RoundedCornerShape(1.dp))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.proximaCita.service,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Cliente: ${state.proximaCita.clientName}",
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (state.proximaCita.serviceType) {
                                    "TECHNICAL" -> {
                                        Icon(Icons.Default.LocationOn, null, tint = colors.primaryOrange, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = state.proximaCita.notes.ifBlank { "Sin direccion" },
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    "PROFESSIONAL" -> {
                                        Icon(Icons.Default.AccessTime, null, tint = Color(0xFF5C6BC0), modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Duracion: ${state.proximaCita.duration} min",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                    "RENTAL" -> {
                                        Icon(Icons.Default.People, null, tint = Color(0xFF26A69A), modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Personas: ${state.proximaCita.peopleCount ?: "-"}",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                    else -> {
                                        Icon(Icons.Default.LocationOn, null, tint = colors.textSecondary, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = state.proximaCita.notes.ifBlank { "Sin notas" },
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(onClick = { onNavigateToChat(state.proximaCita!!.clientId) }) {
                                Icon(Icons.Default.Chat, null, tint = colors.primaryOrange, modifier = Modifier.size(22.dp))
                            }
                            IconButton(onClick = {
                                val address = state.proximaCita!!.notes.ifBlank { state.proximaCita.clientName }
                                val uri = android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(address)}")
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    val webUri = android.net.Uri.parse("https://maps.google.com/?q=${android.net.Uri.encode(address)}")
                                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, webUri))
                                }
                            }) {
                                Icon(Icons.Default.Navigation, null, tint = colors.primaryOrange, modifier = Modifier.size(22.dp))
                            }
                        } // cierra Column botones
                        } // cierra Row contenido
                    } // cierra Row IntrinsicSize
                } // cierra Card
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                    elevation = CardDefaults.cardElevation(4.dp),
                    onClick = onNavigateToCalendar
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(colors.primaryOrange.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CalendarToday, null, tint = colors.primaryOrange, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Sin servicios pendientes",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Toca para ver tu calendario",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        //SOLICITUDES RECIENTES
        Spacer(Modifier.height(20.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Solicitudes recientes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                TextButton(onClick = onNavigateToCalendar) {
                    Text("Ver todas", color = colors.primaryOrange, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            if (state.solicitudesRecientes.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceColor)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin solicitudes pendientes",
                            color = colors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.solicitudesRecientes.forEach { cita ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                //Avatar con inicial
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(colors.primaryOrange.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cita.clientName.firstOrNull()?.uppercase() ?: "?",
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryOrange,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(Modifier.width(12.dp))

                                //Info central
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cita.service,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(Modifier.height(2.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        when (cita.serviceType) {
                                            "TECHNICAL" -> {
                                                Icon(
                                                    Icons.Default.LocationOn,
                                                    null,
                                                    tint = colors.primaryOrange,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(Modifier.width(3.dp))
                                                Text(
                                                    cita.notes.ifBlank { "Sin direccion" },
                                                    fontSize = 11.sp, color = colors.textSecondary,
                                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                                )

                                            }

                                            "PROFESSIONAL" -> {
                                                Icon(Icons.Default.AccessTime, null, tint = Color(0xFF5C6BC0), modifier = Modifier.size(11.dp))

                                                Spacer(Modifier.width(3.dp))

                                                Text("${cita.duration} min · ${cita.date}",
                                                    fontSize = 11.sp, color = colors.textSecondary)
                                            }

                                            "RENTAL" -> {
                                                Icon(Icons.Default.People, null, tint = Color(0xFF26A69A), modifier = Modifier.size(11.dp))

                                                Spacer(Modifier.width(3.dp))

                                                Text("${cita.peopleCount ?: "-"} personas",
                                                    fontSize = 11.sp, color = colors.textSecondary)
                                            }
                                            else -> {
                                                Text("Esperando confirmacion", fontSize = 11.sp, color = colors.textSecondary)
                                            }
                                        }
                                    }
                                    //Tiempo + flecha

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = cita.time,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                        Icon(
                                            Icons.Default.ChevronRight, null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
        // ACCESOS RAPIDOS
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Accesos rapidos",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AccesoRapidoButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CalendarToday,
                    label = "Nueva cita",
                    color = colors.primaryOrange,
                    onClick = onNavigateToCalendar
                )
                AccesoRapidoButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Description,
                    label = "Presupuesto",
                    color = Color(0xFF5C6BC0),
                    onClick = onNavigateToPresupuesto
                )
                AccesoRapidoButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.History,
                    label = "Historial",
                    color = Color(0xFF26A69A),
                    onClick = onNavigateToPresupuestos
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

private data class CarruselItem(
    val titulo: String,
    val subtitulo: String,
    val gradiente: Brush,
    val icono: ImageVector = Icons.Default.Star
)

@Composable
private fun AccesoRapidoButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val colors = getPrestadorColors()
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
        }
    }
}
