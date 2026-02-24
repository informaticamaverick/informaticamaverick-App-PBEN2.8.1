package com.example.myapplication.prestador.ui.presupuesto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.PresupuestoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestosListScreen(
    onBack: () -> Unit = {},
    viewModel: PresupuestoViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val presupuestos by viewModel.presupuestos.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Presupuestos Guardados") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surfaceColor
                )
            )
        }
    ) { padding ->
        if (presupuestos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = colors.textSecondary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "No hay presupuestos guardados",
                        color = colors.textSecondary,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(presupuestos) { presupuesto ->
                    PresupuestoCard(presupuesto = presupuesto, colors = colors)
                }
            }
        }
    }
}

@Composable
fun PresupuestoCard(
    presupuesto: PresupuestoEntity,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Abrir detalle */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Número y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = presupuesto.numeroPresupuesto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.textPrimary
                )
                
                // Badge de estado
                Box(
                    modifier = Modifier
                        .background(
                            when (presupuesto.estado) {
                                "Pendiente" -> colors.primaryOrange.copy(alpha = 0.2f)
                                "Aprobado" -> Color(0xFF66BB6A).copy(alpha = 0.2f)
                                else -> colors.textSecondary.copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = presupuesto.estado,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (presupuesto.estado) {
                            "Pendiente" -> colors.primaryOrange
                            "Aprobado" -> Color(0xFF66BB6A)
                            else -> colors.textSecondary
                        }
                    )
                }
            }
            
            // Fecha
            Text(
                text = "Fecha: ${presupuesto.fecha}",
                fontSize = 14.sp,
                color = colors.textSecondary
            )
            
            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Total:",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = "$ ${String.format("%,.2f", presupuesto.total)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryOrange
                )
            }
            
            // Notas (si existen)
            if (presupuesto.notas.isNotBlank()) {
                Text(
                    text = presupuesto.notas,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 2
                )
            }
        }
    }
}
