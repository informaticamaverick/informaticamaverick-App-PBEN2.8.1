package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import com.example.myapplication.prestador.ui.register.FloatingLabelTextField

@Composable
fun AddSucursalDialog(
    sucursal: SucursalEntity? = null, // null = agregar, con data = editar
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, direccion: String, codigoPostal: String, telefono: String?) -> Unit,
    onUpdate: (SucursalEntity) -> Unit = {}
) {
    var nombre by remember { mutableStateOf(sucursal?.nombre ?: "") }
    var direccion by remember { mutableStateOf(sucursal?.direccion ?: "") }
    var codigoPostal by remember { mutableStateOf(sucursal?.codigoPostal ?: "") }
    var telefono by remember { mutableStateOf(sucursal?.telefono ?: "") }
    
    var nombreError by remember { mutableStateOf(false) }
    var direccionError by remember { mutableStateOf(false) }
    var codigoPostalError by remember { mutableStateOf(false) }

    val isEditMode = sucursal != null

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEditMode) "Editar Sucursal" else "Agregar Sucursal",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFFF6F00)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nombre
                FloatingLabelTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        nombreError = false
                    },
                    label = "Nombre de la sucursal",
                    leadingIcon = Icons.Default.Business,
                    keyboardType = KeyboardType.Text
                )
                if (nombreError) {
                    Text(
                        text = "Campo obligatorio",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dirección
                FloatingLabelTextField(
                    value = direccion,
                    onValueChange = {
                        direccion = it
                        direccionError = false
                    },
                    label = "Dirección",
                    leadingIcon = Icons.Default.LocationOn,
                    keyboardType = KeyboardType.Text
                )
                if (direccionError) {
                    Text(
                        text = "Campo obligatorio",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Código Postal
                FloatingLabelTextField(
                    value = codigoPostal,
                    onValueChange = {
                        codigoPostal = it
                        codigoPostalError = false
                    },
                    label = "Código Postal",
                    leadingIcon = Icons.Default.PinDrop,
                    keyboardType = KeyboardType.Number
                )
                if (codigoPostalError) {
                    Text(
                        text = "Campo obligatorio",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Teléfono (opcional)
                FloatingLabelTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = "Teléfono (opcional)",
                    leadingIcon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            // Validación
                            nombreError = nombre.isBlank()
                            direccionError = direccion.isBlank()
                            codigoPostalError = codigoPostal.isBlank()

                            if (!nombreError && !direccionError && !codigoPostalError) {
                                if (isEditMode) {
                                    onUpdate(
                                        sucursal!!.copy(
                                            nombre = nombre,
                                            direccion = direccion,
                                            codigoPostal = codigoPostal,
                                            telefono = telefono.takeIf { it.isNotBlank() }
                                        )
                                    )
                                } else {
                                    onConfirm(nombre, direccion, codigoPostal, telefono.takeIf { it.isNotBlank() })
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6F00)
                        )
                    ) {
                        Text(if (isEditMode) "Guardar" else "Agregar")
                    }
                }
            }
        }
    }
}
