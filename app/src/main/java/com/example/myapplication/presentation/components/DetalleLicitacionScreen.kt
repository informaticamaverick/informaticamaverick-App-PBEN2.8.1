package com.example.myapplication.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.local.TenderEntity
import java.text.SimpleDateFormat
import java.util.*

private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)
private val PremiumPink = Color(0xFFE91E63)

/**
 * --- POPUP DE DETALLES DE LICITACIÓN ---
 * Muestra información extendida de una licitación y permite gestionar su estado.
 */
@Composable
fun TenderDetailPopup(
    tender: TenderEntity,
    onClose: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onContactProvider: (String) -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val creationDate = dateFormat.format(Date(tender.dateTimestamp))
    val endDate = if (tender.endDate > 0) dateFormat.format(Date(tender.endDate)) else "No definida"
    
    var showCancelWarning by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📋", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text("Resumen de Licitación", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Estado y Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(tender.status)
                    if (!tender.isActive && tender.status == "ABIERTA") {
                        Spacer(Modifier.width(8.dp))
                        Text("(Inactiva)", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Título y Descripción
                DetailSection(Icons.AutoMirrored.Filled.Assignment, "TÍTULO", tender.title, MaverickBlue)
                Spacer(Modifier.height(12.dp))
                DetailSection(Icons.Default.Description, "MEMORIA DESCRIPTIVA", tender.description, Color.LightGray)
                Spacer(Modifier.height(16.dp))

                // Ubicación
                if (tender.locationAddress != null) {
                    DetailSection(
                        Icons.Default.LocationOn, 
                        "UBICACIÓN DEL PROYECTO", 
                        "${tender.locationAddress} ${tender.locationNumber ?: ""}\n${tender.locationLocality ?: ""}", 
                        Color.White
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Carrusel de Imágenes
                if (tender.imageUrls.isNotEmpty()) {
                    Text("FOTOS ADJUNTAS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(tender.imageUrls) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Cláusulas
                if (tender.requiresVisit || tender.requiresPaymentMethod || tender.requiresWorkGuarantee || tender.requiresProviderDoc) {
                    Text("REQUISITOS FORMALES", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OptInFlowRow {
                        if (tender.requiresVisit) RequirementChip(Icons.Default.Visibility, "Visita")
                        if (tender.requiresPaymentMethod) RequirementChip(Icons.Default.Payments, "Pagos")
                        if (tender.requiresWorkGuarantee) RequirementChip(Icons.Default.VerifiedUser, "Garantía")
                        if (tender.requiresProviderDoc) RequirementChip(Icons.AutoMirrored.Filled.Article, "Papeles")
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Fila de Fechas
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        DetailSection(Icons.Default.CalendarToday, "INICIO", creationDate, Color.Gray)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        DetailSection(Icons.Default.CalendarToday, "CIERRE", endDate, Color.Gray)
                    }
                }
                Spacer(Modifier.height(16.dp))

                // ADJUDICACIÓN
                if (tender.status == "ADJUDICADA" && tender.awardedProviderName != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaverickBlue.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, MaverickBlue.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaverickBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Handshake, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("ADJUDICADO A", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaverickBlue)
                                Text(tender.awardedProviderName, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            IconButton(onClick = { onContactProvider(tender.awardedProviderId ?: "") }) {
                                Icon(Icons.AutoMirrored.Filled.Chat, "Mensaje", tint = MaverickBlue)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ACCIONES SEGÚN ESTADO
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))

                if (tender.status == "ABIERTA") {
                    Button(
                        onClick = { showCancelWarning = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.1f)),
                        border = BorderStroke(1.dp, Color.Red.copy(0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Cancel, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("CANCELAR LICITACIÓN", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else if (tender.status == "CERRADA") {
                    Text(
                        "Esta licitación ha finalizado por vencimiento de plazo.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (tender.status == "CANCELADA") {
                    Text(
                        "Licitación finalizada por cancelación forzosa.",
                        color = Color.Red.copy(0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("ENTENDIDO", color = MaverickBlue, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CardSurface,
        titleContentColor = Color.White,
        textContentColor = Color.LightGray
    )

    // --- DIÁLOGO DE ADVERTENCIA DE CANCELACIÓN ---
    if (showCancelWarning) {
        AlertDialog(
            onDismissRequest = { showCancelWarning = false },
            containerColor = Color(0xFF1A1C1E),
            icon = { Icon(Icons.Default.Warning, null, tint = Color.Yellow, modifier = Modifier.size(40.dp)) },
            title = { Text("¿Terminar de manera abrupta?", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { 
                Text(
                    "Estás por CANCELAR esta licitación de forma forzosa. " +
                    "Los prestadores que ya enviaron presupuestos serán notificados y ya no se podrán recibir nuevas ofertas. " +
                    "\n\n¿Deseas continuar?",
                    color = Color.Gray,
                    fontSize = 14.sp
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStatus("CANCELADA")
                        showCancelWarning = false
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("SÍ, CANCELAR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelWarning = false }) {
                    Text("VOLVER", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
private fun DetailSection(icon: ImageVector, label: String, value: String, valueColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (color, icon) = when (status) {
        "ABIERTA" -> Color(0xFF4CAF50) to Icons.Default.CheckCircle
        "CERRADA" -> Color.Gray to Icons.Default.Lock
        "ADJUDICADA" -> MaverickBlue to Icons.Default.Stars
        "CANCELADA" -> Color.Red to Icons.Default.Cancel
        else -> Color.White to Icons.Default.Info
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(status, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RequirementChip(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(0.05f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = PremiumPink, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptInFlowRow(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}
