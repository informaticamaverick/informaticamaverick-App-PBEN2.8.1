package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import org.json.JSONArray

private const val MAX_CATEGORIAS = 5

data class CategoriaItem(val nombre: String, val icono: ImageVector)

private val GRUPOS_CATEGORIAS = listOf(
    "Servicios del Hogar" to listOf(
        CategoriaItem("Plomería",      Icons.Default.Water),
        CategoriaItem("Electricidad",    Icons.Default.ElectricBolt),
        CategoriaItem("Carpintería",    Icons.Default.Handyman),
        CategoriaItem("Pintura",         Icons.Default.FormatPaint),
        CategoriaItem("Albañilería",    Icons.Default.Construction),
        CategoriaItem("Cerrajería",     Icons.Default.Lock),
        CategoriaItem("Gas",             Icons.Default.LocalFireDepartment),
        CategoriaItem("Techismo",       Icons.Default.Roofing),
        CategoriaItem("Herrería",       Icons.Default.Hardware),
        CategoriaItem("Climatización",  Icons.Default.AcUnit)
    ),
    "Exteriores" to listOf(
        CategoriaItem("Jardinería",  Icons.Default.Yard),
        CategoriaItem("Limpieza",     Icons.Default.CleaningServices)
    ),
    "Tecnología y Profesionales" to listOf(
        CategoriaItem("Informática", Icons.Default.Computer),
        CategoriaItem("Diseño",      Icons.Default.Brush),
        CategoriaItem("Fotografía",  Icons.Default.PhotoCamera),
        CategoriaItem("Seguridad",    Icons.Default.Security)
    ),
    "Eventos y Otros" to listOf(
        CategoriaItem("Eventos",      Icons.Default.Celebration),
        CategoriaItem("Gastronomía", Icons.Default.Restaurant),
        CategoriaItem("Mudanzas",     Icons.Default.LocalShipping),
        CategoriaItem("Otro",         Icons.Default.Category)
    )
)

private val TODAS_LAS_CATEGORIAS = GRUPOS_CATEGORIAS.flatMap { (_, items) -> items }


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriasSelector(
    categoriasJson: String,
    onCategoriasActualizadas: (String) -> Unit,
    serviceType: com.example.myapplication.prestador.data.model.ServiceType = com.example.myapplication.prestador.data.model.ServiceType.TECHNICAL
) {
    val colors = getPrestadorColors()
    var seleccionadas by remember(categoriasJson) {
        mutableStateOf(categoriasJsonToSet(categoriasJson))
    }
    val lleno = seleccionadas.size >= MAX_CATEGORIAS

    var busqueda by remember { mutableStateOf("") }
    var mostrarsugerencias by remember { mutableStateOf(false) }
    val sugerencias = remember  (busqueda, seleccionadas) {
        val gruposPorTipo = when (serviceType) {
            com.example.myapplication.prestador.data.model.ServiceType.PROFESSIONAL -> listOf(
                "Salud" to listOf("Medicina General","Odontología","Psicología","Nutrición","Kinesiología","Fonoaudiología","Terapia Ocupacional","Psicopedagogía","Veterinaria"),
                "Legal y Financiero" to listOf("Abogacía","Asesoría Contable","Asesoría Impositiva","Recursos Humanos"),
                "Diseño y Tecnología" to listOf("Arquitectura","Diseño Gráfico","Diseño Web","Marketing Digital","Coaching")
            )
            else -> GRUPOS_CATEGORIAS.map { (grupo, items) -> grupo to items.map { it.nombre } }
        }
        val todasPorTipo = gruposPorTipo.flatMap { (_, items) -> items }
        if (busqueda.isBlank()) emptyList<CategoriaItem>()
        else todasPorTipo.filter { it.contains(busqueda, ignoreCase = true) && it !in seleccionadas }
            .map { nombre -> TODAS_LAS_CATEGORIAS.find { it.nombre == nombre } ?: CategoriaItem(nombre, Icons.Default.Category) }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {

        // Barra de progreso
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${seleccionadas.size}/$MAX_CATEGORIAS seleccionadas",
                fontSize = 12.sp,
                color = if (lleno) MaterialTheme.colorScheme.error else colors.textSecondary
            )
        }
        LinearProgressIndicator(
            progress = { seleccionadas.size / MAX_CATEGORIAS.toFloat() },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = if (lleno) MaterialTheme.colorScheme.error else colors.primaryOrange,
            trackColor = colors.primaryOrange.copy(alpha = 0.15f)
        )
        if (lleno) {
            Text("Límite alcanzado. Deseleccioná una para cambiar.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(4.dp))

        //Campo de busqueda con autocomplete
        Box {
            OutlinedTextField(
                value = busqueda,
                onValueChange = {
                    busqueda = it
                    mostrarsugerencias = it.isNotBlank()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar categoria") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary) },
                trailingIcon = {
                    if (busqueda.isNotBlank())
                    {
                        IconButton(onClick = {
                            busqueda = ""; mostrarsugerencias = false }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = colors.textSecondary)
                        }
                    }
                },
                enabled = !lleno,
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    focusedLabelColor = colors.primaryOrange,
                    unfocusedBorderColor = colors.border
                )
            )

            if (mostrarsugerencias && sugerencias.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column {
                        sugerencias.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.nombre, fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = item.icono,
                                        contentDescription = null,
                                        tint = colors.primaryOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    seleccionadas = seleccionadas + item.nombre
                                    onCategoriasActualizadas(setToJsonArray(seleccionadas))
                                    busqueda = ""
                                    mostrarsugerencias = false
                                }
                            )
                        }
                    }
                }
            }
        }

        //Chips de categorias seleccionadas
        if (seleccionadas.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                seleccionadas.forEach { nombre ->
                    val item = TODAS_LAS_CATEGORIAS.find { it.nombre == nombre }

                    FilterChip(
                        selected = true,
                        onClick = {
                            seleccionadas = seleccionadas - nombre
                            onCategoriasActualizadas(setToJsonArray(seleccionadas))
                        },
                        label = { Text(text = nombre, fontSize = 12.sp)},
                        leadingIcon = {
                            item?.let {
                                Icon(
                                    imageVector = it.icono,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Quitar",
                                modifier = Modifier.size(14.dp)

                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primaryOrange,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

private fun categoriasJsonToSet(json: String): Set<String> {
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { arr.getString(it) }.toSet()
    } catch (e: Exception) { emptySet() }
}

private fun setToJsonArray(set: Set<String>): String {
    val arr = JSONArray()
    set.forEach { arr.put(it) }
    return arr.toString()
}
