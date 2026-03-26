package com.example.myapplication.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.repository.ForecastDay
import com.example.myapplication.presentation.client.BeBrainViewModel
import com.example.myapplication.presentation.client.LocationOption
import com.example.myapplication.presentation.client.Screen

// ==================================================================================
// --- SECCIÓN 1: COMPONENTES DE CABECERA PRINCIPAL ---
// ==================================================================================

/**
 * Sección superior que organiza los widgets tácticos (Clima, Location, Perfil).
 * Centraliza la visualización de la barra superior de la HomeScreen.
 */
@Composable
fun TopHeaderSection(
    navController: NavHostController,
    user: UserEntity?,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    cityName: String,
    currentLocationState: LocationOption,
    onWeatherClick: () -> Unit,
    onRefreshLocation: () -> Unit,
    onLocationSelected: (LocationOption) -> Unit,
    onLogout: () -> Unit,
    beViewModel: BeBrainViewModel
) {
    val cardGradientBrush = Brush.verticalGradient(listOf(Color.White.copy(0.15f), Color.White.copy(0.03f)))

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp)
        .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        
        // Widget de Clima
        Box(Modifier.weight(0.8f)) {
            WeatherWidget(temperature, weatherEmoji, cityName, onWeatherClick, cardGradientBrush)
        }
        
        // Selector de Ubicación
        Box(Modifier.weight(1.6f)) {
            LocationSelector(
                user = user,
                currentLocation = currentLocationState,
                onRefresh = onRefreshLocation,
                onLocationSelected = onLocationSelected,
                brush = cardGradientBrush
            )
        }
        
        // Sección de Perfil
        Box(Modifier.weight(0.8f)) {
            ProfileSection(
                user = user,
                navController = navController,
                onAddressSelected = onLocationSelected,
                onLogout = onLogout,
                brush = cardGradientBrush,
                beViewModel = beViewModel
            )
        }
    }
}

/**
 * Widget compacto de clima que muestra temperatura y estado actual.
 */
@Composable
fun WeatherWidget(temp: String, emoji: String, city: String, onClick: () -> Unit, brush: Brush) {
    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Box(Modifier
            .fillMaxSize()
            .background(brush)
            .padding(4.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(emoji, fontSize = 22.sp)
                Text(temp, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(city, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White.copy(0.7f))
            }
        }
    }
}

/**
 * Selector de ubicación con menú desplegable que incluye direcciones personales y de empresa.
 */
@Composable
fun LocationSelector(
    user: UserEntity?,
    currentLocation: LocationOption,
    onRefresh: () -> Unit,
    onLocationSelected: (LocationOption) -> Unit,
    brush: Brush
) {
    var expanded by remember { mutableStateOf(false) }
    val (linea1, linea2, linea3) = when (currentLocation) {
        is LocationOption.Gps -> Triple("UBICACIÓN ACTUAL", currentLocation.address, "GPS Activo")
        is LocationOption.Personal -> Triple("MI CASA / PERSONAL", "${currentLocation.address} ${currentLocation.number}", currentLocation.locality)
        is LocationOption.Business -> Triple(currentLocation.companyName.uppercase(), currentLocation.branchName, "${currentLocation.address} ${currentLocation.number}")
    }
    
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(end = 6.dp)) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(0.15f))) {
            Box(modifier = Modifier
                .background(brush)
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 46.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = linea1, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF22D3EE), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = linea2, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = linea3, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        
        Surface(modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 8.dp, y = (-14).dp),
            shape = CircleShape,
            color = Color(0xFF1E1E1E),
            border = BorderStroke(1.dp, Color(0xFF22D3EE).copy(alpha = 0.6f)),
            shadowElevation = 6.dp) {
            IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Actualizar GPS", tint = Color(0xFF22D3EE), modifier = Modifier.size(20.dp))
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color(0xFF0D1117).copy(alpha = 0.95f))
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
        ) {
            DropdownMenuItem(
                text = { Text("Usar GPS Actual", color = Color(0xFF22D3EE), fontWeight = FontWeight.Bold) },
                onClick = { onRefresh(); expanded = false },
                leadingIcon = { Icon(Icons.Default.MyLocation, null, tint = Color(0xFF22D3EE)) }
            )
            HorizontalDivider(color = Color.White.copy(0.1f))
            
            user?.personalAddresses?.forEach { addr ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text("${addr.calle} ${addr.numero}", color = Color.White)
                            Text(addr.localidad, fontSize = 10.sp, color = Color.Gray)
                        }
                    },
                    onClick = {
                        onLocationSelected(LocationOption.Personal(addr.calle, addr.numero, addr.localidad))
                        expanded = false
                    }
                )
            }
            
            if (user?.companies?.isNotEmpty() == true) {
                HorizontalDivider(color = Color.White.copy(0.1f))
                user.companies.forEach { company ->
                    company.branches.forEach { branch ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("${company.name} - ${branch.name}", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("${branch.address.calle} ${branch.address.numero}", fontSize = 11.sp, color = Color.Gray)
                                }
                            },
                            onClick = {
                                onLocationSelected(
                                    LocationOption.Business(
                                        companyName = company.name,
                                        branchName = branch.name,
                                        address = branch.address.calle,
                                        number = branch.address.numero,
                                        locality = branch.address.localidad
                                    )
                                )
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sección de perfil que muestra el avatar del usuario y abre el panel de gestión de cuenta.
 */
@Composable
fun ProfileSection(
    user: UserEntity?,
    navController: NavHostController,
    onAddressSelected: (LocationOption) -> Unit,
    onLogout: () -> Unit,
    brush: Brush,
    beViewModel: BeBrainViewModel
) {
    var showPopup by remember { mutableStateOf(false) }
    
    val userFromBrain by beViewModel.userState.collectAsStateWithLifecycle()
    val finalUser = userFromBrain ?: user

    // IMPORTANTE: Asegúrate de que displayName use displayName o name según lo que tengas en la DB
    val displayName = remember(finalUser) {
        finalUser?.name?.ifBlank { null }?.trim()?.split(" ")?.firstOrNull()?.uppercase()
            ?: finalUser?.displayName?.split(" ")?.firstOrNull()?.uppercase()
            ?: "PERFIL"
    }

    //val displayName = remember(finalUser) { finalUser?.name?.trim()?.split(" ")?.firstOrNull()?.uppercase() ?: "PERFIL" }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(brush, RoundedCornerShape(16.dp))
        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .clickable { showPopup = true },
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(4.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp)) {
                if (finalUser?.photoUrl != null) {
                    AsyncImage(
                        model = finalUser.photoUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .border(1.0.dp, Color(0xFF22D3EE), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = displayName, style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }

    if (showPopup) {
        Dialog(onDismissRequest = { showPopup = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showPopup = false }) {
                
                var animateIn by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { animateIn = true }
                
                Box(modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 70.dp, end = 12.dp)
                    .width(340.dp)
                    .clickable(enabled = false) {}) {
                    
                    AnimatedVisibility(
                        visible = animateIn,
                        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn() + slideInVertically { -40 },
                        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                    ) {
                        if (finalUser != null) {
                            UserProfilePopup(
                                user = finalUser,
                                onClose = { showPopup = false },
                                onLogout = { showPopup = false; onLogout() },
                                onAddressSelected = { onAddressSelected(it); showPopup = false },
                                onProfileClick = { showPopup = false; navController.navigate(Screen.PerfilCliente.route) }
                            )
                        } else {
                            Box(Modifier.fillMaxWidth().height(200.dp).background(Color.Black, RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF22D3EE))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN 2: POPUP DE PERFIL Y ÁRBOL DE DIRECCIONES ---
// ==================================================================================

/**
 * Popup de perfil con estilo Cyberpunk que muestra datos del usuario y árbol de direcciones.
 */
@Composable
fun UserProfilePopup(
    user: UserEntity,
    onClose: () -> Unit,
    onLogout: () -> Unit,
    onAddressSelected: (LocationOption) -> Unit,
    onProfileClick: () -> Unit
) {
    val cyberCyan = Color(0xFF22D3EE)
    val cyberMagenta = Color(0xFFE91E63)
    val cyberPurple = Color(0xFF9B51E0)
    val deepGlass = Color(0xFF0D1117).copy(alpha = 0.92f)
    
    var personalExpanded by remember { mutableStateOf(true) }
    var businessExpanded by remember { mutableStateOf(true) }

    GeminiCyberWrapper(
        modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp, horizontal = 1.dp),
        cornerRadius = 24.dp,
        isAnimated = true,
        showGlow = true
    ) {
        Column(modifier = Modifier
            .background(deepGlass)
            .fillMaxWidth()
            .heightIn(max = 650.dp)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)) {
            
            // Header del Popup
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "DATOS DE USUARIO V4", style = MaterialTheme.typography.labelSmall, color = cyberCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text(text = "STATUS: ACTIVE_SESSION", fontSize = 8.sp, color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Card de Usuario
            Box(modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .clickable { onProfileClick() }
                .padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier
                            .size(64.dp)
                            .border(1.dp, cyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)))
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, cyberCyan, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${user.name} ${user.lastName}".uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                        Text(text = "UID: ${user.email}", style = MaterialTheme.typography.labelSmall, color = cyberCyan.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Icon(Icons.Default.QrCodeScanner, null, tint = cyberCyan, modifier = Modifier.size(20.dp).graphicsLayer { alpha = 0.5f })
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Árbol de Direcciones Personales
            CyberTreeDirectory(
                title = "DIR_PERSONALES",
                icon = Icons.Default.FolderOpen,
                accentColor = cyberCyan,
                isExpanded = personalExpanded,
                onToggle = { personalExpanded = !personalExpanded }
            ) {
                user.personalAddresses.forEach { addr ->
                    CyberTreeLeaf(
                        icon = Icons.Default.LocationOn,
                        title = "${addr.calle} ${addr.numero}",
                        subtitle = "${addr.localidad}, ${addr.provincia}",
                        accentColor = cyberCyan,
                        onClick = {
                            onAddressSelected(LocationOption.Personal(address = addr.calle, number = addr.numero, locality = addr.localidad))
                            onClose()
                        }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Árbol de Direcciones de Empresa
            if (user.companies.isNotEmpty()) {
                CyberTreeDirectory(
                    title = "DIR_EMPRESA/COMERCIO",
                    icon = Icons.Default.Dns,
                    accentColor = cyberPurple,
                    isExpanded = businessExpanded,
                    onToggle = { businessExpanded = !businessExpanded }
                ) {
                    user.companies.forEach { company ->
                        var companyItemExpanded by remember { mutableStateOf(false) }
                        CyberTreeDirectory(
                            title = company.name.uppercase(),
                            icon = Icons.Default.Business,
                            accentColor = cyberPurple.copy(alpha = 0.8f),
                            isExpanded = companyItemExpanded,
                            isNested = true,
                            onToggle = { companyItemExpanded = !companyItemExpanded }
                        ) {
                            company.branches.forEach { branch ->
                                CyberTreeLeaf(
                                    icon = Icons.Default.Storefront,
                                    title = branch.name,
                                    subtitle = "${branch.address.calle} ${branch.address.numero}",
                                    accentColor = cyberPurple,
                                    onClick = {
                                        onAddressSelected(
                                            LocationOption.Business(
                                                companyName = company.name,
                                                branchName = branch.name,
                                                address = branch.address.calle,
                                                number = branch.address.numero,
                                                locality = branch.address.localidad
                                            )
                                        )
                                        onClose()
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Botón de Cerrar Sesión
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(cyberMagenta.copy(alpha = 0.05f))
                .border(1.dp, Brush.horizontalGradient(listOf(cyberMagenta, Color.Transparent)), RoundedCornerShape(12.dp))
                .clickable { onLogout() }
                .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PowerSettingsNew, null, tint = cyberMagenta, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(text = "Cerrar_Sesion", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 2.sp)
                }
                Box(modifier = Modifier.align(Alignment.CenterEnd).size(8.dp).background(cyberMagenta, CircleShape).blur(4.dp))
            }
        }
    }
}

/**
 * Carpeta expandible en el árbol de perfiles.
 */
@Composable
fun CyberTreeDirectory(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    isExpanded: Boolean,
    isNested: Boolean = false,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f)
    Column(modifier = Modifier.padding(start = if (isNested) 16.dp else 0.dp)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = accentColor.copy(alpha = 0.5f), modifier = Modifier.size(16.dp).rotate(rotation))
            Spacer(Modifier.width(8.dp))
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
        }
        AnimatedVisibility(visible = isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Box(modifier = Modifier
                .padding(start = 7.dp)
                .drawWithCache {
                    onDrawWithContent {
                        drawLine(
                            color = accentColor.copy(alpha = 0.2f),
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawContent()
                    }
                }) { Column { content() } }
        }
    }
}

/**
 * Hoja (ítem final) en el árbol de perfiles.
 */
@Composable
fun CyberTreeLeaf(icon: ImageVector, title: String, subtitle: String, accentColor: Color, onClick: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(vertical = 6.dp)
        .drawWithCache {
            onDrawWithContent {
                drawLine(
                    color = accentColor.copy(alpha = 0.2f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(15.dp.toPx(), size.height / 2),
                    strokeWidth = 1.dp.toPx()
                )
                drawContent()
            }
        }
        .padding(start = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(32.dp)
            .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ==================================================================================
// --- SECCIÓN 3: COMPONENTES DE CLIMA EXPANDIDO ---
// ==================================================================================

/**
 * Card expandible que muestra el pronóstico y detalles adicionales del clima.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherExpandedCard(
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    cityName: String,
    forecastDays: List<ForecastDay>
) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(cityName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(weatherDescription, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(weatherEmoji, fontSize = 64.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(temperature, fontSize = 64.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
