package com.example.myapplication.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAdminScreen(
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar Categorías") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3B82F6),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.initializeCategories() },
                containerColor = Color(0xFF3B82F6)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Inicializar Categorías",
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
                .padding(16.dp)
        ) {
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.initializeCategories() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Inicializar Categorías")
                }

                Button(
                    onClick = { viewModel.loadCategories() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Recargar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje de error/éxito
            error?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (it.contains("exitosamente")) 
                            Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(12.dp),
                        color = if (it.contains("exitosamente")) 
                            Color(0xFF166534) else Color(0xFF991B1B)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Indicador de carga
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Información
            Text(
                text = "Total de categorías: ${categories.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de categorías
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryAdminItem(category)
                }
            }
        }
    }
}

@Composable
fun CategoryAdminItem(category: Category) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de color
            // Se usa try-catch para evitar crash si el color hex es inválido
            val color = try {
                Color(android.graphics.Color.parseColor(category.colorHex))
            } catch (e: Exception) {
                Color.Gray
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(8.dp)
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Icon: ${category.iconName}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "Order: ${category.order}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Text(
                text = category.colorHex,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
