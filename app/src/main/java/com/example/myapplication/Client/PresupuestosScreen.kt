package com.example.myapplication.Client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// 1. Modelo de datos para representar un archivo
data class Archivo(
    val id: String,
    val nombre: String,
    val fecha: String,
    val categoria: String, // "Presupuestos Generales" o "Presupuestos de Licitaciones"
    val servicioCategoria: String, // E.g., "Albañilería", "Electricidad"
    val empresaId: String,
    val empresaNombre: String,
    val empresaImagenUrl: String,
    val precio: String,
    val fechaInicioLicitacion: String? = null,
    var fechaFinLicitacion: String? = null
)

// Estructura para la información de la categoría
data class CategoriaInfo(
    val color: Color,
    val icon: ImageVector
)

// Enum para las opciones de ordenamiento
enum class SortOption {
    FECHA, MONTO, EMPRESA
}

// Mapa para asociar categorías con su información visual
val categoriasVisules = mapOf(
    "Presupuestos Generales" to CategoriaInfo(Color(0xFF10B981), Icons.Filled.Email),
    "Presupuestos de Licitaciones" to CategoriaInfo(Color(0xFFF59E0B), Icons.Filled.Warning)
)

// Función para obtener la información de la categoría, con un valor por defecto
fun getCategoriaInfo(categoria: String): CategoriaInfo {
    return categoriasVisules.entries.find { it.key.equals(categoria.trim(), ignoreCase = true) }?.value
        ?: CategoriaInfo(Color.Gray, Icons.Filled.Email)
}

// Función para verificar si una licitación está activa
@RequiresApi(Build.VERSION_CODES.O)
fun isLicitacionActiva(fechaInicio: String?, fechaFin: String?): Boolean {
    if (fechaInicio == null || fechaFin == null) return false
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return try {
        val inicio = LocalDate.parse(fechaInicio, formatter)
        val fin = LocalDate.parse(fechaFin, formatter)
        val ahora = LocalDate.now()
        !ahora.isBefore(inicio) && !ahora.isAfter(fin)
    } catch (e: DateTimeParseException) {
        false
    }
}

// Datos de ejemplo iniciales
val initialArchivosDeEjemplo = listOf(
    Archivo("p1", "Presupuesto Cocina", "15/05/2024", "Presupuestos de Licitaciones", "Albañilería", "emp1", "Constructora A", "https://picsum.photos/seed/emp1/100", "$2,500.00", "10/05/2024", "25/05/2024"),
    Archivo("p2", "Presupuesto Baño", "12/05/2024", "Presupuestos Generales", "Plomería", "emp2", "Plomería Veloz", "https://picsum.photos/seed/emp2/100", "$800.00"),
    Archivo("p3", "Presupuesto Jardín", "09/05/2024", "Presupuestos Generales", "Jardinería", "emp3", "Jardines Verdes", "https://picsum.photos/seed/emp3/100", "$450.00"),
    Archivo("p4", "Reparación de Techo", "01/06/2024", "Presupuestos Generales", "Albañilería", "emp4", "Techos Seguros", "https://picsum.photos/seed/emp4/100", "$1,200.00"),
    Archivo("p5", "Instalación Eléctrica Completa", "28/05/2024", "Presupuestos de Licitaciones", "Electricidad", "emp5", "Electro-Max", "https://picsum.photos/seed/emp5/100", "$7,800.00", "20/05/2024", "10/06/2024"),
    Archivo("p6", "Pintura exterior edificio", "25/05/2024", "Presupuestos de Licitaciones", "Pintura", "emp1", "Constructora A", "https://picsum.photos/seed/emp1/100", "$15,000.00", "15/05/2024", "15/06/2024"),
    Archivo("p9", "Remodelación Oficina Principal", "18/05/2024", "Presupuestos de Licitaciones", "Albañilería", "emp6", "Ofi-Diseños", "https://picsum.photos/seed/emp6/100", "$12,300.00", "10/05/2024", "01/06/2024"),
    Archivo("p11", "Limpieza de Fachada", "16/05/2024", "Presupuestos de Licitaciones", "Limpieza", "emp8", "Clean-Glass", "https://picsum.photos/seed/emp8/100", "$3,400.00", "05/05/2024", "20/05/2024"),
    Archivo("p12", "Instalación de Cámaras de Seguridad", "14/05/2024", "Presupuestos de Licitaciones", "Electricidad", "emp9", "Seguridad Total", "https://picsum.photos/seed/emp9/100", "$4,200.00", "01/05/2024", "20/05/2024"),
    Archivo("p16", "Desarrollo de App Móvil", "18/01/2026", "Presupuestos de Licitaciones", "Software", "emp12", "Dev-Masters", "https://picsum.photos/seed/emp12/100", "$25,000.00", "01/01/2026", "19/01/2026")
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresupuestosScreen(
    onArchivoClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Presupuestos de Licitaciones", "Presupuestos Generales")

    var sortOption by remember { mutableStateOf(SortOption.FECHA) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showOnlyActive by remember { mutableStateOf(true) } // Filtro activado por defecto

    var archivos by remember { mutableStateOf(initialArchivosDeEjemplo) }
    var showEndLicitacionDialog by remember { mutableStateOf(false) }
    var archivoToEnd by remember { mutableStateOf<Archivo?>(null) }

    Scaffold(containerColor = Color.Transparent,

        topBar = {
            TopAppBar(
                title = { Text("Presupuestos Recibidos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás", tint = Color.White)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(imageVector = Icons.Filled.Sort, contentDescription = "Ordenar", tint = Color.White)
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            DropdownMenuItem(text = { Text("Por Fecha (más reciente)") }, onClick = { sortOption = SortOption.FECHA; showSortMenu = false })
                            DropdownMenuItem(text = { Text("Por Monto (mayor a menor)") }, onClick = { sortOption = SortOption.MONTO; showSortMenu = false })
                            DropdownMenuItem(text = { Text("Por Empresa (A-Z)") }, onClick = { sortOption = SortOption.EMPRESA; showSortMenu = false })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title) })
                }
            }

            if (selectedTabIndex == 0) { // Solo para Presupuestos de Licitaciones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mostrar solo activas", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = showOnlyActive, onCheckedChange = { showOnlyActive = it })
                }
            }

            val selectedCategory = tabs[selectedTabIndex]
            val archivosFiltrados = remember(archivos, selectedCategory, showOnlyActive) {
                val initialList = archivos.filter { it.categoria == selectedCategory }
                if (selectedCategory == "Presupuestos de Licitaciones" && showOnlyActive) {
                    initialList.filter { isLicitacionActiva(it.fechaInicioLicitacion, it.fechaFinLicitacion) }
                } else {
                    initialList
                }
            }

            val sortedArchivos = remember(archivosFiltrados, sortOption) {
                when (sortOption) {
                    SortOption.FECHA -> archivosFiltrados.sortedByDescending { it.fecha.split("/").reversed().joinToString("") }
                    SortOption.MONTO -> archivosFiltrados.sortedByDescending { it.precio.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0 }
                    SortOption.EMPRESA -> archivosFiltrados.sortedBy { it.empresaNombre }
                }
            }

            val agrupadosPorServicio = sortedArchivos.groupBy { it.servicioCategoria }
            var expandedServiceCategories by remember(selectedTabIndex) { mutableStateOf(agrupadosPorServicio.keys) }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                agrupadosPorServicio.forEach { (servicio, archivosEnServicio) ->
                    if (servicio.isNotBlank()) {
                        stickyHeader {
                            ServiceCategoryHeader(
                                titulo = servicio,
                                count = archivosEnServicio.size,
                                isExpanded = servicio in expandedServiceCategories,
                                onClick = {
                                    expandedServiceCategories = if (servicio in expandedServiceCategories) {
                                        expandedServiceCategories - servicio
                                    } else {
                                        expandedServiceCategories + servicio
                                    }
                                }
                            )
                        }
                    }
                    items(archivosEnServicio, key = { it.id }) { archivo ->
                        AnimatedVisibility(
                            visible = servicio.isBlank() || servicio in expandedServiceCategories,
                            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                            exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                        ) {
                            ArchivoItem(
                                archivo = archivo,
                                categoriaInfo = getCategoriaInfo(archivo.categoria),
                                onClick = { onArchivoClick(archivo.id) },
                                onProfileClick = { onProfileClick(archivo.empresaId) },
                                onLongClick = {
                                    if (archivo.categoria == "Presupuestos de Licitaciones") {
                                        archivoToEnd = archivo
                                        showEndLicitacionDialog = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEndLicitacionDialog && archivoToEnd != null) {
        AlertDialog(
            onDismissRequest = { showEndLicitacionDialog = false },
            title = { Text("Finalizar Licitación") },
            text = { Text("¿Estás seguro de que quieres finalizar la licitación para '${archivoToEnd!!.nombre}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val yesterday = LocalDate.now().minusDays(1).format(formatter)
                        archivos = archivos.map {
                            if (it.id == archivoToEnd!!.id) {
                                it.copy(fechaFinLicitacion = yesterday)
                            } else {
                                it
                            }
                        }
                        showEndLicitacionDialog = false
                        archivoToEnd = null
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndLicitacionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServiceCategoryHeader(titulo: String, count: Int, isExpanded: Boolean, onClick: () -> Unit) {
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) -180f else 0f, label = "rotationAngle")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Expandir/Contraer", modifier = Modifier.rotate(rotationAngle))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArchivoItem(
    archivo: Archivo,
    categoriaInfo: CategoriaInfo,
    onClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, categoriaInfo.color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(imageVector = categoriaInfo.icon, contentDescription = "Icono de archivo", tint = categoriaInfo.color)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = archivo.nombre, fontWeight = FontWeight.SemiBold)
                        val isActiva = isLicitacionActiva(archivo.fechaInicioLicitacion, archivo.fechaFinLicitacion)
                        if (isActiva) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ACTIVA",
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(text = "Recibido: ${archivo.fecha}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    if (archivo.fechaInicioLicitacion != null && archivo.fechaFinLicitacion != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Licitación: ${archivo.fechaInicioLicitacion} - ${archivo.fechaFinLicitacion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = archivo.precio, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onProfileClick() }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(archivo.empresaImagenUrl),
                    contentDescription = "Logo de ${archivo.empresaNombre}",
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = archivo.empresaNombre, style = MaterialTheme.typography.bodySmall, maxLines = 1, fontSize = 10.sp)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PresupuestosScreenPreview() {
    PresupuestosScreen(onBack = {})

}