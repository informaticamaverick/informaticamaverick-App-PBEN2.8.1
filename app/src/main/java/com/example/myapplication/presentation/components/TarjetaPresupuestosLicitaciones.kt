package com.example.myapplication.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// --- PALETA DE COLORES PREMIUM ---
private val DarkBackground = Color(0xFF05070A)
private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)
private val MaverickPurple = Color(0xFF9B51E0)
private val StatusActive = Color(0xFF38BDF8)
private val StatusFinished = Color(0xFF34D399)
private val StatusWarning = Color(0xFFF87171)
private val NeonCyber = Color(0xFF00FFC2)

/**
 * Tarjeta de Presupuesto Optimizada (Estilo Premium Grid)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TarjetaPresupuestoPremium(
    providerName: String,
    companyName: String?,
    amount: Double,
    budgetId: String,
    photoUrl: String?,
    isOnline: Boolean = false,
    isSubscribed: Boolean = false,
    isSelected: Boolean = false,
    onViewClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaverickBlue else Color.White.copy(alpha = 0.08f)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A1F26), Color(0xFF0A0E14))
    )

    Surface(
        modifier = modifier
            .width(145.dp)
            .height(175.dp)
            .combinedClickable(onClick = onViewClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (isSelected) 12.dp else 4.dp
    ) {
        Box(modifier = Modifier.background(backgroundBrush)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // --- CABECERA (Imagen + Textos) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp)) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(0.1f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        if (isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color(0xFF00E676), CircleShape)
                                    .border(1.5.dp, Color(0xFF0A0E14), CircleShape)
                            )
                        }
                        if (isSubscribed) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.BottomEnd)
                                    .background(MaverickPurple, CircleShape)
                                    .border(1.5.dp, Color(0xFF0A0E14), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(8.dp))
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = providerName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = (companyName ?: "Independiente").uppercase(),
                            color = MaverickBlue,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // --- INFO CENTRAL ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ID #${budgetId.takeLast(6).uppercase()}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$ ${String.format(Locale.getDefault(), "%,.0f", amount)}",
                        color = StatusFinished,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // --- BOTONES ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = onChatClick,
                        modifier = Modifier.size(width = 40.dp, height = 32.dp),
                        color = Color.White.copy(0.05f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(0.1f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("💬", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = onViewClick,
                        modifier = Modifier.weight(1f).height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(isSelected) MaverickBlue else MaverickBlue.copy(0.15f),
                            contentColor = if(isSelected) Color.White else MaverickBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if(isSelected) "VER" else "👁️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Carpeta de Licitación (Estilo Folder Premium) Reordenada
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LicitacionFolderPremium(
    title: String,
    category: String,
    categoryIcon: String = "📋",
    categoryColor: Color = Color.Gray,
    tenderId: String,
    status: String,
    startDate: Long,
    endDate: Long,
    budgetCount: Int,
    unreadCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val remainingDays = TimeUnit.MILLISECONDS.toDays(endDate - System.currentTimeMillis()).coerceAtLeast(0)
    val df = SimpleDateFormat("dd MMM", Locale.getDefault())
    
    // El color del borde ahora es el de la categoría (o MaverickBlue si está seleccionado)
    val borderColor = if (isSelected) MaverickBlue else categoryColor.copy(alpha = 0.6f)

    Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
        
        // --- 1. PESTAÑA DEL FOLDER (Categoría + Emoji) ---
        Surface(
            modifier = Modifier
                .offset(y = (-24).dp)
                .width(140.dp)
                .height(28.dp),
            color = categoryColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 24.dp),
            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(categoryIcon, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    category.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = categoryColor,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // --- 2. ÁREA SUPERIOR DERECHA (Estado y No Leídos superpuestos) ---
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-16).dp)
                .zIndex(2f),
            horizontalAlignment = Alignment.End
        ) {
            StatusPillPremium(status)
            if (unreadCount > 0) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = NeonCyber,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(2.dp, DarkBackground)
                ) {
                    Text(
                        "$unreadCount",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkBackground
                    )
                }
            }
        }

        // --- 3. CUERPO DE LA CARPETA ---
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            color = CardSurface,
            shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp, bottomStart = 28.dp),
            border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
            shadowElevation = if (isSelected) 16.dp else 4.dp
        ) {
            Column(Modifier.padding(20.dp)) {
                // Título e ID debajo
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "#${tenderId.takeLast(8).uppercase()}",
                        color = MaverickBlue.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(0.05f))
                Spacer(Modifier.height(16.dp))

                // Reordenamiento de Fechas (Izquierda) y Presupuestos (Derecha)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Lado Izquierdo: Fechas y Días restantes
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("INICIO: ", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(df.format(Date(startDate)), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EventAvailable, null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("CIERRE: ", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(df.format(Date(endDate)), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (remainingDays < 3) StatusWarning else MaverickBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (remainingDays > 0) "Quedan $remainingDays días" else "Finalizado",
                                fontSize = 11.sp,
                                color = if (remainingDays < 3) StatusWarning else MaverickBlue,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Lado Derecho: Presupuestos Recibidos
                    Surface(
                        color = MaverickBlue.copy(0.1f), 
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaverickBlue.copy(0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = budgetCount.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaverickBlue
                            )
                            Text(
                                "RECIBIDOS",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black,
                                color = MaverickBlue.copy(0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPillPremium(status: String) {
    val color = when(status.uppercase()) {
        "ACTIVO", "ABIERTA" -> StatusActive
        "ADJUDICADO", "ADJUDICADA" -> MaverickPurple
        "TERMINADO", "CERRADA" -> StatusFinished
        else -> Color.Gray
    }
    Surface(
        color = color.copy(0.1f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Text(
            status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}
