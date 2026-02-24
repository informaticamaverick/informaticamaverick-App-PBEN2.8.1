package com.example.myapplication.prestador.ui.presupuesto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.myapplication.prestador.ui.BudgetPreviewPDFDialog
import com.example.myapplication.prestador.data.PPrestadorProfileFalso
import com.example.myapplication.prestador.data.PPrestadorSampleDataFalso
import com.example.myapplication.prestador.ui.BudgetItem
import com.example.myapplication.prestador.ui.BudgetTax
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import androidx.hilt.navigation.compose.hiltViewModel


// Enums para estado del presupuesto
enum class PresupuestoEstado(val displayName: String, val color: Color) {
    PENDIENTE("Pendiente", Color(0xFFFFA726)),
    ACEPTADO("Aceptado", Color(0xFF66BB6A)),
    RECHAZADO("Rechazado", Color(0xFFEF5350)),
    ENVIADO("Enviado", Color(0xFF42A5F5))
}

// Data class para presupuesto
data class Presupuesto(
    val id: String,
    val numeroPresupuesto: String,
    val clienteNombre: String,
    val fecha: LocalDate,
    val monto: Double,
    val estado: PresupuestoEstado,
    val descripcion: String = ""
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestosScreen(
    onBack: () -> Unit = {},
    onCrearNuevo: () -> Unit = {},
    onVerDetalle: (Presupuesto) -> Unit = {},
    showTopBar: Boolean = true,
    viewModel: com.example.myapplication.prestador.viewmodel.PresupuestoViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val colors = getPrestadorColors()
    // Estado para el filtro
    var filtroEstado by remember { mutableStateOf<PresupuestoEstado?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var presupuestoSeleccionado by remember { mutableStateOf<Presupuesto?>(null) }

    // DATOS REALES DE LA BD
    val presupuestosDB by viewModel.presupuestos.collectAsState()
    
    // SIMPLIFICADO: Sin cargar clientes por ahora
    // val clientesDB by viewModel.clientes.collectAsState()
    
    // Convertir PresupuestoEntity a Presupuesto para la UI
    val presupuestos = try {
        presupuestosDB.map { entity ->
            Presupuesto(
                id = entity.id,
                numeroPresupuesto = entity.numeroPresupuesto,
                clienteNombre = "Cliente", // Nombre fijo por ahora
                fecha = LocalDate.parse(entity.fecha),
                monto = entity.total,
                estado = when(entity.estado) {
                    "Aceptado" -> PresupuestoEstado.ACEPTADO
                    "Rechazado" -> PresupuestoEstado.RECHAZADO
                    "Enviado" -> PresupuestoEstado.ENVIADO
                    else -> PresupuestoEstado.PENDIENTE
                },
                descripcion = entity.notas.ifEmpty { "Sin descripción" }
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    // Filtrar presupuestos
    val presupuestosFiltrados = if (filtroEstado != null) {
        presupuestos.filter { it.estado == filtroEstado }
    } else {
        presupuestos
    }

    Scaffold(
        containerColor = colors.backgroundColor,
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text("Presupuestos") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                    // Botón de filtro
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Badge(
                                modifier = Modifier.offset(x = 8.dp, y = (-8).dp),
                                containerColor = if (filtroEstado != null) MaterialTheme.colorScheme.primary else Color.Transparent
                            ) {
                                if (filtroEstado != null) {
                                    Text("1", fontSize = 8.sp)
                                }
                            }
                            Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                        }
                        
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = {
                                    filtroEstado = null
                                    showFilterMenu = false
                                },
                                leadingIcon = {
                                    if (filtroEstado == null) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                            HorizontalDivider()
                            PresupuestoEstado.values().forEach { estado ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(estado.color)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(estado.displayName)
                                        }
                                    },
                                    onClick = {
                                        filtroEstado = estado
                                        showFilterMenu = false
                                    },
                                    leadingIcon = {
                                        if (filtroEstado == estado) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCrearNuevo,
                containerColor = colors.primaryOrange,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Presupuesto") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header personalizado cuando está en el tab (sin TopBar)
            if (!showTopBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colors.primaryOrange,
                                    Color(0xFFFF9F66)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón de volver atrás
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = "Presupuestos",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        // Botón de filtro
                        Box {
                            IconButton(onClick = { showFilterMenu = true }) {
                                Badge(
                                    containerColor = if (filtroEstado != null) Color.White else Color.Transparent
                                ) {
                                    if (filtroEstado != null) {
                                        Text("1", fontSize = 8.sp, color = colors.primaryOrange)
                                    }
                                }
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filtrar",
                                    tint = Color.White
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos") },
                                    onClick = {
                                        filtroEstado = null
                                        showFilterMenu = false
                                    },
                                    leadingIcon = {
                                        if (filtroEstado == null) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    }
                                )
                                HorizontalDivider()
                                PresupuestoEstado.values().forEach { estado ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(estado.color)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(estado.displayName)
                                            }
                                        },
                                        onClick = {
                                            filtroEstado = estado
                                            showFilterMenu = false
                                        },
                                        leadingIcon = {
                                            if (filtroEstado == estado) {
                                                Icon(Icons.Default.Check, contentDescription = null)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Contenido
            if (presupuestosFiltrados.isEmpty()) {
                // Mensaje vacío
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = colors.textSecondary
                        )
                        Text(
                            if (filtroEstado != null) "No hay presupuestos ${filtroEstado?.displayName?.lowercase()}"
                            else "No hay presupuestos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(presupuestosFiltrados) { presupuesto ->
                        PresupuestoCard(
                            presupuesto = presupuesto,
                            onClick = { onVerDetalle(presupuesto) },
                            onVerPreview = { 
                                presupuestoSeleccionado = presupuesto
                                showPreviewDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo de vista previa
    if (showPreviewDialog && presupuestoSeleccionado != null) {
        // Datos de ejemplo para la vista previa
        val sampleItems = listOf(
            BudgetItem(
                code = "001",
                description = "Artículo de ejemplo",
                unitPrice = presupuestoSeleccionado!!.monto * 0.6,
                quantity = 1,
                taxPercentage = 21.0,
                discountPercentage = 0.0
            )
        )
        
        val sampleTaxes = listOf(
            BudgetTax(
                description = "IVA 21%",
                amount = presupuestoSeleccionado!!.monto * 0.21
            )
        )
        
        val subtotalAmount = presupuestoSeleccionado!!.monto / 1.21
        val taxAmount = presupuestoSeleccionado!!.monto - subtotalAmount
        
        BudgetPreviewPDFDialog(
            prestador = PPrestadorSampleDataFalso.pprestadores.firstOrNull() ?: PPrestadorProfileFalso(
                id = "1",
                name = "Prestador",
                lastName = "Demo",
                profileImageUrl = "",
                bannerImageUrl = "",
                rating = 5.0f,
                isVerified = true,
                isOnline = true,
                services = listOf("Servicio Demo"),
                companyName = "Empresa Demo",
                address = "Dirección Demo",
                email = "demo@email.com",
                doesHomeVisits = true,
                hasPhysicalLocation = true,
                works24h = false,
                galleryImages = emptyList(),
                isSubscribed = false
            ),
            items = sampleItems,
            services = emptyList(),
            professionalFees = emptyList(),
            miscExpenses = emptyList(),
            taxes = sampleTaxes,
            grandTotal = presupuestoSeleccionado!!.monto,
            subtotal = subtotalAmount,
            taxAmount = taxAmount,
            discountAmount = 0.0,
            onDismiss = { showPreviewDialog = false }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PresupuestoCard(
    presupuesto: Presupuesto,
    onClick: () -> Unit,
    onVerPreview: () -> Unit = {}
){
    val colors = getPrestadorColors()
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Número y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = presupuesto.numeroPresupuesto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = presupuesto.estado.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = presupuesto.estado.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = presupuesto.estado.color.copy(alpha = 1f) // Color sólido
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cliente
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colors.textSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = presupuesto.clienteNombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Descripción
            if (presupuesto.descripcion.isNotBlank()) {
                Text(
                    text = presupuesto.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Footer: Fecha y Monto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = presupuesto.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$ ${String.format("%,.2f", presupuesto.monto )}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.primaryOrange
                    )
                }

                //Boton ver vista previa
                IconButton(
                    onClick = onVerPreview,
                    modifier = Modifier
                        .background(
                            color = colors.primaryOrange,
                            shape = RoundedCornerShape(12.dp))
                        .size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Ver Vista Prevvia",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
