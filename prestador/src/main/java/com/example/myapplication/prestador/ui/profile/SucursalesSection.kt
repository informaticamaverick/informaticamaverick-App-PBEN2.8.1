package com.example.myapplication.prestador.ui.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import com.example.myapplication.prestador.viewmodel.SucursalesViewModel

@Composable
fun SucursalesSection(
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    viewModel: SucursalesViewModel = hiltViewModel()
) {
    val sucursales by viewModel.sucursales.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val businessId by viewModel.businessId.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var sucursalToEdit by remember { mutableStateOf<SucursalEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<SucursalEntity?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showErrorMessage by remember { mutableStateOf<String?>(null) }
    
    // Refrescar businessId cada vez que se muestra la sección
    LaunchedEffect(Unit) {
        viewModel.refreshBusinessId()
    }

    // Handle UI state
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SucursalesViewModel.UiState.Success -> {
                showAddDialog = false
                sucursalToEdit = null
                showDeleteDialog = null
                successMessage = state.message
                showSuccessMessage = true
                viewModel.resetState()
                kotlinx.coroutines.delay(3000)
                showSuccessMessage = false
            }
            is SucursalesViewModel.UiState.Error -> {
                showErrorMessage = state.message
                viewModel.resetState()
                kotlinx.coroutines.delay(3000)
                showErrorMessage = null
            }
            else -> {}
        }
    }

    Column {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Otras Sucursales",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${sucursales.size} registrada${if (sucursales.size != 1) "s" else ""}",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
            
            OutlinedButton(
                onClick = {
                    sucursalToEdit = null
                    showAddDialog = true
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.primaryOrange
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryOrange),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agregar", fontSize = 13.sp)
            }
        }

        // Lista de sucursales
        if (sucursales.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surfaceColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No hay sucursales adicionales",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sucursales.forEach { sucursal ->
                    SucursalCard(
                        sucursal = sucursal,
                        colors = colors,
                        onEdit = {
                            sucursalToEdit = sucursal
                            showAddDialog = true
                        },
                        onDelete = { showDeleteDialog = sucursal }
                    )
                }
            }
        }

        // Success message
        AnimatedVisibility(
            visible = showSuccessMessage,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = successMessage,
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Error message
        showErrorMessage?.let { errorMsg ->
            AnimatedVisibility(
                visible = true,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog) {
        AddSucursalDialog(
            sucursal = sucursalToEdit,
            onDismiss = {
                showAddDialog = false
                sucursalToEdit = null
            },
            onConfirm = { nombre, direccion, codigoPostal, telefono ->
                viewModel.addSucursal(nombre, direccion, codigoPostal, telefono)
            },
            onUpdate = { sucursal ->
                viewModel.updateSucursal(sucursal)
            }
        )
    }

    // Delete Dialog
    showDeleteDialog?.let { sucursal ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar sucursal") },
            text = { Text("¿Estás seguro de eliminar '${sucursal.nombre}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSucursal(sucursal.id)
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SucursalCard(
    sucursal: SucursalEntity,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surfaceColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.primaryOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sucursal.nombre,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${sucursal.direccion} (CP: ${sucursal.codigoPostal})",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
                if (sucursal.telefono != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = sucursal.telefono,
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Acciones
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
