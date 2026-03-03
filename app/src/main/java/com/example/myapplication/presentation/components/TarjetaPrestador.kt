package com.example.myapplication.presentation.components

import android.R.attr.enabled
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.AddressProvider
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.data.model.Provider
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Clase de utilidad para representar los datos de un Badge con estado activo/inactivo.
 */
data class BadgeItem(
    val id: String,
    val icon: String,          // Emoji para estado activo
    val inactiveIcon: ImageVector, // Icono para estado inactivo
    val label: String,         // Descripción para el popup
    val isActive: Boolean      // Estado de la propiedad
)
    // --- MODELOS DE DATOS ---
    data class Provider(
val id: String,
val displayName: String,
val photoUrl: String,
val isOnline: Boolean,
val isVerified: Boolean,
val rating: Double,
val isFavorite: Boolean,
val companies: List<CompanyEntity>,
val categories: List<String>,
val works24h: Boolean = false,
val hasPhysicalLocation: Boolean = false,
val doesHomeVisits: Boolean = false,
val doesShipping: Boolean = false,
val acceptsAppointments: Boolean = false
)

data class CompanyEntity(val name: String)
data class CategoryEntity(val name: String, val icon: String)


// ==========================================================================================
// ----------TARJETA PRESTADOR HORIZONTAL ------------------------------
// ==========================================================================================

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PrestadorCard(
    provider: Provider,
    onClick: () -> Unit,
    onChat: (() -> Unit)? = null,
    onDeleteRequest: (() -> Unit)? = null,
    actionContent: @Composable (() -> Unit)? = null,
    onToggleFavorite: ((String, Boolean) -> Unit)? = null,
    viewMode: String = "Detallada",
    showAvatars: Boolean = true,
    showPreviews: Boolean = true,
    showBadges: Boolean = true,
    allCategories: List<CategoryEntity> = emptyList()
) {
    var showDetailSheet by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    var showFavoriteDialog by remember { mutableStateOf(false) }

    val staticBrush = geminiGradientBrush(isAnimated = false)
    val animateBrush = geminiGradientBrush(isAnimated = true)

    val activeColor = Color(0xFF22D3EE)
    val inactiveColor = Color.White.copy(alpha = 0.15f)
    val cyberBackground = Color(0xFF0A0E14)
    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1F26),
            Color(0xFF0A0E14)
        ))

    val mainCompany = provider.companies.firstOrNull()
    val companyName = mainCompany?.name ?: ""
    val servicesToShow = mainCompany?.categories ?: provider.categories

    val works24h = mainCompany?.works24h ?: provider.works24h
    val doesHomeVisits = mainCompany?.doesHomeVisits ?: provider.doesHomeVisits
    val hasPhysicalLocation = mainCompany?.hasPhysicalLocation ?: provider.hasPhysicalLocation
    val acceptsAppointments = mainCompany?.acceptsAppointments ?: provider.acceptsAppointments

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(staticBrush, RoundedCornerShape(26.dp))
            .padding(1.5.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.5.dp))
                .combinedClickable(
                    onClick = { showDetailSheet = true },
                    onLongClick = { showContextMenu = true }
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.5.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier
                    .background(cardGradient)
                    .matchParentSize().background(Color.White.copy(alpha = 0.05f)))

                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showAvatars) {
                        Box(contentAlignment = Alignment.TopStart) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .clickable { onClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = provider.photoUrl,
                                    contentDescription = "Foto de perfil",
                                    fallback = painterResource(id = R.drawable.iconapp),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (provider.isOnline && showBadges) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .offset(x = (-4).dp, y = (-4).dp)
                                        .background(Color(0xFF00E676), CircleShape)
                                        .border(2.dp, cyberBackground, CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onClick() }) {
                            Text(
                                text = provider.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (provider.isVerified && showBadges) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.Verified, null, tint = Color(0xFF9B51E0), modifier = Modifier.size(18.dp))
                            }
                        }

                        if (companyName.isNotEmpty()) {
                            Text(
                                text = companyName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF22D3EE).copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable { onClick() }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                Text(text = " ${provider.rating}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            if (showBadges) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (works24h) Icon(Icons.Default.AccessTimeFilled, "24Hs", modifier = Modifier.size(18.dp), tint = Color(0xFFFF9800))
                                    if (hasPhysicalLocation) Icon(Icons.Default.Storefront, "Local", modifier = Modifier.size(18.dp), tint = Color(0xFF2197F5))
                                    if (doesHomeVisits) Icon(Icons.Default.LocalShipping, "Visitas", modifier = Modifier.size(18.dp), tint = Color(0xFF9B51E0))
                                    if (acceptsAppointments) Icon(Icons.Default.EventAvailable, "Turnos", modifier = Modifier.size(18.dp), tint = Color(0xFF00FFC2))
                                }
                            }

                            IconButton(onClick = { showFavoriteDialog = true }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = if (provider.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (provider.isFavorite) Color.Red else Color.Gray)
                            }
                        }
                    }

                    if (actionContent != null) {
                        actionContent()
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onChat?.invoke() },
                            modifier = Modifier.size(44.dp).background(animateBrush, RoundedCornerShape(14.dp))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        DropdownMenu(expanded = showContextMenu, onDismissRequest = { showContextMenu = false }, offset = DpOffset(x = 16.dp, y = 0.dp)) {
            DropdownMenuItem(text = { Text("Ver Perfil Completo") }, leadingIcon = { Icon(Icons.Default.Person, null) }, onClick = { showContextMenu = false; onClick() })
            HorizontalDivider()
            DropdownMenuItem(text = { Text(if (provider.isFavorite) "Quitar de Favoritos" else "Añadir a Favoritos") }, leadingIcon = { Icon(imageVector = if (provider.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (provider.isFavorite) Color.Red else Color.Unspecified) }, onClick = { showContextMenu = false; showFavoriteDialog = true })
        }
    }

    if (showDetailSheet) {
        ModalBottomSheet(onDismissRequest = { showDetailSheet = false }, containerColor = cyberBackground, tonalElevation = 8.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = provider.photoUrl, contentDescription = null, modifier = Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(modifier = Modifier.width(16.dp)); Column {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(text = provider.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White); if (provider.isVerified) { Spacer(modifier = Modifier.width(6.dp)); Icon(Icons.Filled.Verified, null, tint = Color(0xFF2197F5), modifier = Modifier.size(24.dp)) } }
                    if (companyName.isNotEmpty()) { Text(text = companyName, style = MaterialTheme.typography.titleSmall, color = Color(0xFF22D3EE)) }
                    Text(text = provider.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                }
                Spacer(modifier = Modifier.height(24.dp)); Text("Servicios Ofrecidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Cyan)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    servicesToShow.forEach { service ->
                        val catColorLong = allCategories.find { it.name.equals(service, ignoreCase = true) }?.color
                        val tagColor = if (catColorLong != null) Color(catColorLong) else Color.White.copy(alpha = 0.15f)
                        ServiceTag(text = service, color = tagColor)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp)); HorizontalDivider(color = Color.White.copy(0.5f)); Spacer(modifier = Modifier.height(24.dp))
               // RowItemDetail(icon = Icons.Default.Schedule, text = "Disponible 24hs", isActive = works24h)
               // RowItemDetail(icon = Icons.Default.Home, text = "Visitas a Domicilio", isActive = doesHomeVisits)
               // RowItemDetail(icon = Icons.Default.Storefront, text = "Local Físico", isActive = hasPhysicalLocation)
               // RowItemDetail(icon = Icons.Default.Event, text = "Turnos / Citas", isActive = acceptsAppointments)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { showDetailSheet = false; onClick() }, modifier = Modifier.fillMaxWidth().height(36.dp).background(animateBrush, RoundedCornerShape(12.dp)), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), shape = RoundedCornerShape(28.dp)) {
                    Text("Ver Perfil Completo", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showFavoriteDialog) {
        AlertDialog(onDismissRequest = { showFavoriteDialog = false }, icon = { Icon(Icons.Default.Favorite, null, tint = Color.Red) }, title = { Text(if (provider.isFavorite) "Quitar de Favoritos" else "Añadir a Favoritos") }, text = { Text(if (provider.isFavorite) "¿Estás seguro de que quieres eliminar a este prestador de tus favoritos?" else "¿Quieres añadir a este prestador a tu lista de favoritos?") }, confirmButton = { TextButton(onClick = { onToggleFavorite?.invoke(provider.id, !provider.isFavorite); showFavoriteDialog = false }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = { showFavoriteDialog = false }) { Text("Cancelar") } })
    }
}

// ==========================================================================================
// ------------------ TARJETA PRESTADOR VERTICAL --------------------------------------
// ==========================================================================================
// Actualización en TarjetaPrestador.kt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)@Composable
fun PrestadorCardVertical(
    provider: Provider,
    onClick: () -> Unit, // Navegación a PerfilPrestadorScreen
    onChat: (() -> Unit)? = null,
    onToggleFavorite: ((String, Boolean) -> Unit)? = null,
    allCategories: List<CategoryEntity> = emptyList()
) {
    var expandedBadgeId by remember { mutableStateOf<String?>(null) }
    var showVerifiedPopup by remember { mutableStateOf(false) }

    val animateBrush = geminiGradientBrush(isAnimated = true)
    val baseColor = Color(0xFF22D3EE)

    val mainCompany = provider.companies.firstOrNull()
    val companyName = mainCompany?.name ?: ""

    val works24h = mainCompany?.works24h ?: provider.works24h
    val doesHomeVisits = mainCompany?.doesHomeVisits ?: provider.doesHomeVisits
    val acceptsAppointments = mainCompany?.acceptsAppointments ?: provider.acceptsAppointments
    val hasPhysicalLocation = mainCompany?.hasPhysicalLocation ?: provider.hasPhysicalLocation
    val doesShipping = provider.doesShipping

    // Ordenamiento: True primero
    val inferiorBadges = remember(provider) {
        listOf(
            BadgeItem("24h", "🕒", Icons.Default.AccessTimeFilled, "Atención las 24 Horas", works24h),
            BadgeItem("loc", "🏪", Icons.Default.Storefront, "Tiene Local Físico", hasPhysicalLocation),
            BadgeItem("visit", "🚚", Icons.Default.LocalShipping, "Servicio/Visitas TÉCNICAS a Domicilio", doesHomeVisits),
            BadgeItem("env", "📦", Icons.Default.LocalShipping, "Realiza Envíos", doesShipping),
            BadgeItem("date", "📅", Icons.Default.EventAvailable, "Turnos Disponibles", acceptsAppointments)
        ).sortedByDescending { it.isActive }
    }

    val superiorBadges = remember {
        listOf(
            BadgeItem("serv", "🛠️", Icons.Default.Build, "Ofrece Servicios Profesionales Especializados", true),
            BadgeItem("prod", "🛍️", Icons.Default.ShoppingBag, "Venta Directa de Productos y Accesorios", true)
        ).sortedByDescending { it.isActive }
    }

    Box(
        modifier = Modifier
            .width(185.dp)
            .padding(top = 15.dp, end = 15.dp, bottom = 15.dp)
    ) {
        // --- TARJETA BASE ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(245.dp) // Altura optimizada
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .combinedClickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Fondo Cyberpunk
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1A1F26), Color(0xFF0A0E14)))))

                // Glow superior
                Box(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(0.7f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(0.3f), Color.Transparent))))

                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //Spacer(modifier = Modifier.height(10.dp))

                    // 1. IMAGEN DE PERFIL CENTRADA
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(contentAlignment = Alignment.TopStart) {
                            AsyncImage(
                                model = provider.photoUrl,
                                contentDescription = "Perfil",
                                modifier = Modifier
                                    .size(95.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                    .clickable { onClick() },
                                contentScale = ContentScale.Crop
                            )
                            if (provider.isOnline) {
                                Box(modifier = Modifier.size(16.dp).offset((-4).dp, (-4).dp).background(Color(0xFF00E676), CircleShape).border(2.dp, Color(0xFF0A0E14), CircleShape))
                            }
                        }
                        if (provider.isVerified) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verificado",
                                tint = Color(0xFF9B51E0),
                                modifier = Modifier.size(30.dp).offset(x = 4.dp, y = 4.dp).background(Color(0xFF0A0E14), CircleShape).padding(2.dp).clickable { showVerifiedPopup = true }
                            )
                            DropdownMenu(expanded = showVerifiedPopup, onDismissRequest = { showVerifiedPopup = false }, modifier = Modifier.background(Color(0xFF161C24)).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))) {
                                Text("Prestador verificado por la app. Identidad y servicios validados.", modifier = Modifier.padding(12.dp).widthIn(max = 200.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. NOMBRE Y EMPRESA
                    Text(text = provider.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2, textAlign = TextAlign.Center, lineHeight = 18.sp)

                    if (companyName.isNotEmpty()) {
                        Text(text = companyName.uppercase(), style = MaterialTheme.typography.labelSmall, color = baseColor.copy(alpha = 0.7f), fontWeight = FontWeight.Black, fontSize = 8.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
                    // ESPACIO REDUCIDO
                    //Spacer(modifier = Modifier.height(8.dp))
                    // 3. RANKING Y FAVORITO
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                            Text(text = " ${provider.rating}", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Text(text = " VALORACIÓN", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { onToggleFavorite?.invoke(provider.id, !provider.isFavorite) }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = if (provider.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (provider.isFavorite) Color.Red else Color.Gray, modifier = Modifier.size(20.dp))
                        }
                    }
                    //Spacer(modifier = Modifier.weight(1f))
                    // 4. DIVIDER SUTIL
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.1f))

                    // Espacio para los badges inferiores superpuestos
                   // Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // --- BADGES SUPERIORES VERTICALES (SUPERPUESTOS AL BORDE IZQUIERDO) ---
        Column(
            modifier = Modifier.align(Alignment.CenterStart).offset(x = 4.dp, y = (-65).dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            superiorBadges.forEach { badge ->
                BadgeConPopup(
                    item = badge,
                    isExpanded = expandedBadgeId == badge.id,
                    onToggle = { expandedBadgeId = if (expandedBadgeId == badge.id) null else badge.id },
                    size = 30.dp,
                    fontSize = 15.sp
                )
            }
        }

        // --- BADGES INFERIORES (SUPERPUESTOS AL BORDE INFERIOR) ---
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-2).dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            inferiorBadges.forEach { badge ->
                BadgeConPopup(
                    item = badge,
                    isExpanded = expandedBadgeId == badge.id,
                    onToggle = { expandedBadgeId = if (expandedBadgeId == badge.id) null else badge.id }
                )
            }
        }

        // --- BOTÓN DE MENSAJE FLOTANTE (CORNER DERECHO SUPERIOR) ---
        Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp)) {
            IconButton(
                onClick = { onChat?.invoke() },
                modifier = Modifier.size(44.dp).background(animateBrush, CircleShape).border(1.5.dp, Color.White.copy(0.3f), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Email, contentDescription = "Mensaje", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
fun BadgeConPopup(
    item: BadgeItem,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    size: Dp = 26.dp,
    fontSize: TextUnit = 13.sp
) {
    Box {
        @Suppress("DEPRECATION")
        Box(
            modifier = Modifier
                .size(size)
                .background(Color(0xFF1C222A), CircleShape)
                // Borde más claro para diferenciar del fondo
                .border(1.2.dp, if (item.isActive) Color.White.copy(0.4f) else Color.White.copy(0.2f), CircleShape)
                .clip(CircleShape)
                .clickable { onToggle() }
                .drawWithContent {
                    drawContent()
                    // Si no está activo, dibujamos una línea diagonal (tachado)
                    if (!item.isActive) {
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.5f),
                            start = Offset(x = size.toPx() * 0.25f, y = size.toPx() * 0.25f),
                            end = Offset(x = size.toPx() * 0.75f, y = size.toPx() * 0.75f),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (item.isActive) {
                // Estado Activo: Emoji
                Text(text = item.icon, fontSize = fontSize)
            } else {
                // Estado Inactivo: Icono gris
                Icon(
                    imageVector = item.inactiveIcon,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.4f),
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }

        // Popup descriptivo al tocar el badge
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = onToggle,
            modifier = Modifier
                .background(Color(0xFF161C24))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (item.isActive) {
                    Text(text = item.icon, fontSize = 14.sp)
                } else {
                    Icon(item.inactiveIcon, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (item.isActive) item.label else "${item.label} (No disponible)",
                    color = if (item.isActive) Color.White else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (!item.isActive) TextDecoration.LineThrough else null,
                    modifier = Modifier.widthIn(max = 200.dp)
                )
            }
        }
    }
}
/**
 * Versión 2 de la Tarjeta de Prestador.
 * Implementa un modo compacto que se expande al tocarlo con animaciones premium.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun PrestadorCardVerticalV2(
    provider: Provider,
    isExpanded: Boolean = false, // 🔥 NUEVO: El estado viene de afuera
    onExpandToggle: () -> Unit = {}, // 🔥 NUEVO: Callback para avisar que se tocó
    onClick: () -> Unit,
    onChat: (() -> Unit)? = null,
    onToggleFavorite: ((String, Boolean) -> Unit)? = null,
    allCategories: List<CategoryEntity> = emptyList()
) {
   // var isExpanded by remember { mutableStateOf(false) }
    var expandedBadgeId by remember { mutableStateOf<String?>(null) }

    // --- ANIMACIONES DE TRANSICIÓN ---
    val transition = updateTransition(targetState = isExpanded, label = "ExpandCard")

    val cardWidth by transition.animateDp(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow) },
        label = "Width"
    ) { if (it) 230.dp else 125.dp }

    val cardHeight by transition.animateDp(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow) },
        label = "Height"
    ) { if (it) 355.dp else 155.dp }

    val baseColor = Color(0xFF22D3EE)
    val mainCompany = provider.companies.firstOrNull()?.name ?: ""

    // Badges de servicios
    val inferiorBadges = remember(provider) {
        listOf(
            BadgeItem("24h", "🕒", Icons.Default.AccessTimeFilled, "Atención las 24 Horas", provider.works24h),
            BadgeItem("loc", "🏪", Icons.Default.Storefront, "Tiene Local Físico", provider.hasPhysicalLocation),
            BadgeItem("visit", "🚚", Icons.Default.LocalShipping, "Servicio/Visitas TÉCNICAS a Domicilio", provider.doesHomeVisits),
            BadgeItem("env", "📦", Icons.Default.LocalShipping, "Realiza Envíos", provider.doesShipping),
            BadgeItem("date", "📅", Icons.Default.EventAvailable, "Turnos Disponibles", provider.acceptsAppointments)
        ).sortedByDescending { it.isActive }
    }

    Box(
        modifier = Modifier
            .width(cardWidth + 10.dp)
            .height(cardHeight + 20.dp)
            .padding(top = 10.dp, end = 10.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // --- TARJETA PRINCIPAL ---
        Card(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onExpandToggle, // 🔥 Usa la función que viene por parámetro
                    onLongClick = onClick // Navega al perfil con click largo

                // onClick = { isExpanded = !isExpanded },
                   // onLongClick = onClick
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Fondo Dark Cyberpunk
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1A1F26), Color(0xFF0A0E14)))))

                // Contenido
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. IMAGEN DE PERFIL (Sin escala ni offset al expandir)
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Box(contentAlignment = Alignment.TopStart) {
                            AsyncImage(
                                model = provider.photoUrl,
                                contentDescription = "Perfil",
                                modifier = Modifier
                                    .size(75.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                    .clickable(
                                        enabled = isExpanded, // Solo funciona si la tarjeta ya está abierta
                                        onClick = onClick // El parámetro que ya recibe la función

                                     ),
                            contentScale = ContentScale.Crop
                            )
                            if (provider.isOnline) {
                                Box(modifier = Modifier.size(14.dp).offset((-3).dp, (-3).dp).background(Color(0xFF00E676), CircleShape).border(2.dp, Color(0xFF0A0E14), CircleShape))
                            }
                        }
                        if (provider.isVerified) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verificado",
                                tint = Color(0xFF9B51E0),
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 4.dp, y = 4.dp)
                                    .background(Color(0xFF0A0E14), CircleShape)
                                    .padding(2.dp)
                            )
                        }
                    }

                    // 2. TEXTOS
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = provider.displayName,
                        style = if (isExpanded) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = if (isExpanded) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    if (mainCompany.isNotEmpty()) {
                        Text(
                            text = mainCompany.uppercase(),
                            style = if (isExpanded) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall,
                            color = baseColor.copy(alpha = 0.8f),
                            fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Black,
                            fontSize = if (isExpanded) 10.sp else 8.sp,
                            textAlign = TextAlign.Center
                        )
                        // REQUERIMIENTO: Agregar dirección
                        if (isExpanded) {
                            provider.address?.let { addr ->
                                Text(
                                    text = addr.fullString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    // 3. CONTENIDO ADICIONAL (Solo en expandido)
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Divider más claro (alpha 0.2f)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.8f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                    Text(text = " ${provider.rating}", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(text = " VALORACIÓN", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = { onToggleFavorite?.invoke(provider.id, !provider.isFavorite) }, modifier = Modifier.size(24.dp)) {
                                    Icon(imageVector = if (provider.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (provider.isFavorite) Color.Red else Color.Gray, modifier = Modifier.size(20.dp))
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.8f))
                            // Categorías (Máximo 4 en 2 filas)
                            // --- CATEGORÍAS EN CARRUSEL (LazyRow) ---
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(provider.categories) { categoryName ->
                                    val categoryData = allCategories.find { it.name.equals(categoryName, ignoreCase = true) }
                                    CategoryTag(name = categoryName, emoji = categoryData?.icon ?: "✨")
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.8f))
                            // Badges Inferiores dentro de la tarjeta expandida
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                inferiorBadges.forEach { badge ->
                                    BadgeConPopupV2(
                                        item = badge,
                                        isExpanded = expandedBadgeId == badge.id,
                                        onToggle = { expandedBadgeId = if (expandedBadgeId == badge.id) null else badge.id },
                                        size = 28.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- BOTÓN CHAT Y BADGES VERTICALES (REQUERIMIENTO) ---
        AnimatedVisibility(
            visible = isExpanded,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Icono de Chat
                IconButton(
                    onClick = { onChat?.invoke() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Brush.linearGradient(listOf(Color(0xFF22D3EE), Color(0xFF0284C7))), CircleShape)
                        .border(1.5.dp, Color.White.copy(0.3f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(22.dp))
                }

                // REQUERIMIENTO: Badges de Productos y Servicios superpuestos verticalmente
                BadgeConPopupV2(
                    item = BadgeItem("serv", "🛠️", Icons.Default.Build, "Servicios", true),
                    isExpanded = expandedBadgeId == "serv",
                    onToggle = { expandedBadgeId = if (expandedBadgeId == "serv") null else "serv" },
                    size = 32.dp
                )
                BadgeConPopupV2(
                    item = BadgeItem("prod", "🛍️", Icons.Default.ShoppingBag, "Productos", true),
                    isExpanded = expandedBadgeId == "prod",
                    onToggle = { expandedBadgeId = if (expandedBadgeId == "prod") null else "prod" },
                    size = 32.dp
                )
            }
        }
    }
}

@Composable
fun CategoryTag(name: String, emoji: String) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = name, color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BadgeConPopupV2(
    item: BadgeItem,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    size: Dp = 30.dp
) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size)
                .background(Color(0xFF1C222A), CircleShape)
                .border(1.2.dp, if (item.isActive) Color.White.copy(0.4f) else Color.White.copy(0.1f), CircleShape)
                .clip(CircleShape)
                .clickable { onToggle() }
                .drawWithContent {
                    drawContent()
                    if (!item.isActive) {
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.5f),
                            start = Offset(x = size.toPx() * 0.3f, y = size.toPx() * 0.3f),
                            end = Offset(x = size.toPx() * 0.7f, y = size.toPx() * 0.7f),
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (item.isActive) {
                Text(text = item.icon, fontSize = 14.sp)
            } else {
                Icon(item.inactiveIcon, null, tint = Color.Gray.copy(0.4f), modifier = Modifier.size(16.dp))
            }
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = onToggle,
            modifier = Modifier.background(Color(0xFF161C24)).border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(10.dp))
        ) {
            Text(
                text = if (item.isActive) item.label else "${item.label} (No disp.)",
                color = if (item.isActive) Color.White else Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

// ==========================================================================================
// ---------- PREVIEW
// ==========================================================================================
@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PrestadorCardVerticalV2Preview() {
    MyApplicationTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Vista Previa de Tarjetas", color = Color.White, fontWeight = FontWeight.Bold)

            // Estado Compacto
            PrestadorCardVerticalV2(
                provider = mockProviderPreview,
                onClick = {},
                allCategories = mockCategoriesPreview
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Nota: En Android Studio puedes interactuar con el clic para ver la expansión
            Text("Haz clic en la tarjeta en el Modo Interactivo", color = Color.Gray, fontSize = 12.sp)
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PrestadorCardPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PrestadorCard(
                provider = mockProviderPreview,
                onClick = {},
                allCategories = mockCategoriesPreview
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PrestadorCardVerticalPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PrestadorCardVertical(
                provider = mockProviderPreview,
                onClick = {},
                allCategories = mockCategoriesPreview
            )
        }
    }
}

private val mockProviderPreview = Provider(
    uid = "1001",
    email = "maverick@tech.com",
    displayName = "Maverick Informática",
    name = "Maximiliano",
    lastName = "Nanterne",
    phoneNumber = "381-1234567",
    matricula = "MP-9922",
    titulo = "Ingeniero de Software",
    cuilCuit = "20-30405060-7",
    address = AddressProvider(
        calle = "San Martín",
        numero = "450",
        localidad = "San Miguel de Tucumán"
    ),
    works24h = true,
    hasPhysicalLocation = true,
    doesHomeVisits = false, // <--- False para probar ordenamiento y tachado
    doesShipping = true,
    acceptsAppointments = false, // <--- False para probar ordenamiento y tachado
    isSubscribed = true,
    isVerified = true,
    isOnline = true,
    isFavorite = true,
    rating = 5.0f,
    categories = listOf("Informatica", "Desarrollo Móvil"),
    hasCompanyProfile = true,
    companies = listOf(
        CompanyProvider(
            name = "Maverick Tech S.A.",
            categories = listOf("Software", "Hardware"),
            works24h = true,
            hasPhysicalLocation = true,
            isVerified = true
        )
    ),
    photoUrl = "https://picsum.photos/seed/maverick/200/200",
    bannerImageUrl = null,
    createdAt = System.currentTimeMillis()
)

private val mockCategoriesPreview = listOf(
    CategoryEntity(
        name = "Informatica",
        icon = "💻",
        color = 0xFF2197F5,
        superCategory = "Tecnología",
        isNew = false,
        isNewPrestador = false,
        isAd = false,
        imageUrl = null
    ),
    CategoryEntity(
        name = "Desarrollo Móvil",
        icon = "📱",
        color = 0xFF9B51E0,
        superCategory = "Tecnología",
        isNew = false,
        isNewPrestador = false,
        isAd = false,
        imageUrl = null
    )
)

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun BadgeConPopupV2Preview() {
    MyApplicationTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Activo
            BadgeConPopupV2(
                item = BadgeItem("1", "🕒", Icons.Default.AccessTimeFilled, "Activo", true),
                isExpanded = false,
                onToggle = {}
            )
            // Inactivo
            BadgeConPopupV2(
                item = BadgeItem("2", "🕒", Icons.Default.AccessTimeFilled, "Inactivo", false),
                isExpanded = false,
                onToggle = {}
            )
            // Expandido
            BadgeConPopupV2(
                item = BadgeItem("3", "🕒", Icons.Default.AccessTimeFilled, "Expandido", true),
                isExpanded = true,
                onToggle = {}
            )
        }
    }
}
