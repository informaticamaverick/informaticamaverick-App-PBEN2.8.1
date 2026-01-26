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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresupuestosScreen(
    onArchivoClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onChatClick: (String) -> Unit = {},
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Presupuestos de Licitaciones", "Presupuestos Generales")

    var sortOption by remember { mutableStateOf(SortOption.FECHA) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showOnlyActive by remember { mutableStateOf(true) } // Filtro activado por defecto para licitaciones
    
    // Filtros
    var searchQuery by remember { mutableStateOf("") }
    var selectedServiceCategories by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Usar datos de PresupuestoSampleDataFalso
    // No usamos 'remember' para la lista en sí para que reaccione a cambios en el objeto global si ocurren
    // Pero si queremos filtrar localmente, podemos derivar de ahí.
    // Para simplificar y permitir la modificación local (como finalizar licitación), usamos remember con mutableState
    var presupuestos by remember { mutableStateOf(PresupuestoSampleDataFalso.presupuestos) }
    
    var showEndLicitacionDialog by remember { mutableStateOf(false) }
    var presupuestoToEnd by remember { mutableStateOf<PresupuestoFalso?>(null) }

    // Obtener categorías únicas disponibles para filtros
    val availableServiceCategories = remember(presupuestos, selectedTabIndex) {
        val currentTab = tabs[selectedTabIndex]
        presupuestos.filter { it.categoria == currentTab }.map { it.servicioCategoria }.distinct().sorted()
    }

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
                            Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = "Ordenar", tint = Color.White)
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
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar presupuesto o empresa...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = "Borrar") } }
                } else null,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

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

            // Filtros de Categoría de Servicio (Chips)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableServiceCategories) { category ->
                    val isSelected = category in selectedServiceCategories
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedServiceCategories = if (isSelected) {
                                selectedServiceCategories - category
                            } else {
                                selectedServiceCategories + category
                            }
                        },
                        label = { Text(category) },
                        leadingIcon = if (isSelected) {
                            { Icon(imageVector = Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null
                    )
                }
            }

            val selectedCategory = tabs[selectedTabIndex]
            val presupuestosFiltrados = remember(presupuestos, selectedCategory, showOnlyActive, searchQuery, selectedServiceCategories) {
                var list = presupuestos.filter { it.categoria == selectedCategory }
                
                // Filtro "Activas"
                if (selectedCategory == "Presupuestos de Licitaciones" && showOnlyActive) {
                    list = list.filter { isLicitacionActiva(it.fechaInicioLicitacion, it.fechaFinLicitacion) }
                }
                
                // Filtro Búsqueda
                if (searchQuery.isNotEmpty()) {
                    val query = searchQuery.lowercase()
                    list = list.filter { 
                        it.nombre.lowercase().contains(query) || 
                        it.empresaNombre.lowercase().contains(query) 
                    }
                }
                
                // Filtro Categorías de Servicio
                if (selectedServiceCategories.isNotEmpty()) {
                    list = list.filter { it.servicioCategoria in selectedServiceCategories }
                }
                
                list
            }

            val sortedPresupuestos = remember(presupuestosFiltrados, sortOption) {
                when (sortOption) {
                    SortOption.FECHA -> presupuestosFiltrados.sortedByDescending { it.fecha.split("/").reversed().joinToString("") }
                    SortOption.MONTO -> presupuestosFiltrados.sortedByDescending { it.precio.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0 }
                    SortOption.EMPRESA -> presupuestosFiltrados.sortedBy { it.empresaNombre }
                }
            }

            val agrupadosPorServicio = sortedPresupuestos.groupBy { it.servicioCategoria }
            var expandedServiceCategories by remember(selectedTabIndex) { mutableStateOf(agrupadosPorServicio.keys) }
            
            // Asegurarse de expandir categorías nuevas si cambian los filtros
            LaunchedEffect(agrupadosPorServicio.keys) {
                expandedServiceCategories = expandedServiceCategories + agrupadosPorServicio.keys
            }

            if (sortedPresupuestos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No se encontraron presupuestos", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    agrupadosPorServicio.forEach { (servicio, archivosEnServicio) ->
                        // Eliminamos el stickyHeader con el título de categoría para cumplir con la solicitud anterior de "sacar los separadores"
                        // pero mantendremos la lista plana organizada por servicio implícitamente

                        items(archivosEnServicio, key = { it.id }) { presupuesto ->
                            // Como ya no hay header expandible, mostramos siempre
                            ArchivoItem(
                                archivo = presupuesto,
                                categoriaInfo = getCategoriaInfo(presupuesto.categoria),
                                onClick = { onArchivoClick(presupuesto.id) },
                                onProfileClick = { onProfileClick(presupuesto.empresaId) },
                                onChatClick = { onChatClick(presupuesto.empresaId) },
                                onLongClick = {
                                    if (presupuesto.categoria == "Presupuestos de Licitaciones") {
                                        presupuestoToEnd = presupuesto
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

    if (showEndLicitacionDialog && presupuestoToEnd != null) {
        AlertDialog(
            onDismissRequest = { showEndLicitacionDialog = false },
            title = { Text("Finalizar Licitación") },
            text = { Text("¿Estás seguro de que quieres finalizar la licitación para '${presupuestoToEnd!!.nombre}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        val yesterday = LocalDate.now().minusDays(1).format(formatter)

                        // Actualizar la lista local.
                        // Nota: En una app real, esto debería actualizar la fuente de datos (Base de datos / API)
                        val index = presupuestos.indexOfFirst { it.id == presupuestoToEnd!!.id }
                        if (index != -1) {
                            presupuestos[index] = presupuestos[index].copy(
                                fechaFinLicitacion = yesterday,
                                status = "Finalizada"
                            )
                        }

                        showEndLicitacionDialog = false
                        presupuestoToEnd = null
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArchivoItem(
    archivo: PresupuestoFalso,
    categoriaInfo: CategoriaInfo,
    onClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit,
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
                // Icono Categoria
                Icon(imageVector = categoriaInfo.icon, contentDescription = "Icono de archivo", tint = categoriaInfo.color)
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    // Titulo y Estado
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = archivo.nombre, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val isActiva = isLicitacionActiva(archivo.fechaInicioLicitacion, archivo.fechaFinLicitacion)
                        val statusColor = if (isActiva) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                        val statusText = if (isActiva) "ACTIVA" else archivo.status.uppercase()
                        
                        if (statusText.isNotEmpty()) {
                            Surface(
                                color = statusColor,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = statusText,
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
            
            // Acciones: Perfil y Chat
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Imagen Perfil
                Box(modifier = Modifier.clickable { onProfileClick() }) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = archivo.empresaImagenUrl,
                            placeholder = painterResource(id = R.drawable.logo_app)
                        ),
                        contentDescription = "Logo de ${archivo.empresaNombre}",
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Boton Chat
                IconButton(
                    onClick = onChatClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = "Chat con ${archivo.empresaNombre}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
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
