package com.example.myapplication.prestador.ui.promotion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.CreatePromotionViewModel

/**
 * PANTALLA DE PRUEBA PARA VER LAS PROMOCIONES GUARDADAS EN LA BD
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionListScreen(
    onBack: () -> Unit,
    viewModel: CreatePromotionViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    
    // Obtener promociones de la BD
    val promotions by viewModel.getPromotions("test_provider_123").collectAsState(initial = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Promociones (${promotions.size})", color = colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surfaceColor
                )
            )
        },
        containerColor = colors.backgroundColor
    ) { paddingValues ->
        if (promotions.isEmpty()) {
            // Mensaje cuando no hay promociones
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "No hay promociones guardadas\n\n¡Crea tu primera promoción!",
                    fontSize = 16.sp,
                    color = colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            // Lista de promociones
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(promotions) { promotion ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surfaceColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Título
                            Text(
                                text = promotion.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Descripción
                            Text(
                                text = promotion.description,
                                fontSize = 14.sp,
                                color = colors.textSecondary
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Info adicional
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tipo: ${promotion.type.name}",
                                    fontSize = 12.sp,
                                    color = colors.primaryOrange
                                )
                                
                                promotion.discount?.let {
                                    Text(
                                        text = "Descuento: $it%",
                                        fontSize = 12.sp,
                                        color = colors.primaryOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Categorías
                            Text(
                                text = "Categorías: ${promotion.categories.joinToString(", ")}",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Métricas
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "❤️ ${promotion.likes} likes",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "👁 ${promotion.views} vistas",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
