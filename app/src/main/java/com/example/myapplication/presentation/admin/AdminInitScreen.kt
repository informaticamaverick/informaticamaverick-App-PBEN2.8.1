package com.example.myapplication.presentation.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.admin.CategoryViewModel
import com.example.myapplication.presentation.admin.SubCategoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInitScreen(
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    subCategoryViewModel: SubCategoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val categories by categoryViewModel.categories.collectAsState()
    val error by categoryViewModel.error.collectAsState()
    val isLoading by categoryViewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var showSuccess by remember { mutableStateOf(false) }
    
    // Manejar el botón "Atrás" del sistema (hardware o gestos)
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inicializar Firebase") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3B82F6),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "🔥 Inicializar Base de Datos",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Usa estos botones SOLO UNA VEZ para poblar Firebase",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Card de instrucciones
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFDCFCE7)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📋 Instrucciones:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Presiona 'Crear Categorías'\n2. Espera a que termine\n3. Presiona 'Crear Subcategorías de Mecánico'\n4. ¡Listo! Ve al dashboard",
                        fontSize = 14.sp,
                        color = Color(0xFF166534)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Estado actual
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 Estado Actual",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Categorías en Firebase: ${categories.size}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón 1: Crear Categorías
            Button(
                onClick = {
                    showSuccess = false
                    categoryViewModel.initializeCategories()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Creando...", fontSize = 16.sp)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("1️⃣ Crear 18 Categorías", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón 2: Crear Subcategorías
            Button(
                onClick = {
                    showSuccess = false
                    coroutineScope.launch {
                        val mechanicId = categoryViewModel.getCategoryIdByName("Mecánico")
                        if (mechanicId != null) {
                            subCategoryViewModel.initializeSubCategories(mechanicId)
                            showSuccess = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                ),
                enabled = !isLoading && categories.isNotEmpty()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("2️⃣ Crear Subcategorías Mecánico", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de volver
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✅ Volver al Dashboard")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mensajes de error/éxito
            error?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (it.contains("exitosamente")) 
                            Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (it.contains("exitosamente")) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF166534)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = it,
                            color = if (it.contains("exitosamente")) 
                                Color(0xFF166534) else Color(0xFF991B1B)
                        )
                    }
                }
            }

            if (showSuccess) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFDCFCE7)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🎉 ¡Todo Listo!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF166534)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Firebase está poblado con:\n• 18 Categorías principales\n• 6 Subcategorías de Mecánico\n\n¡Ahora puedes agregar más desde Firebase Console!",
                            fontSize = 14.sp,
                            color = Color(0xFF166534)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Info adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFEF3C7)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Tip:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Después de usar esto, puedes agregar más categorías directamente desde Firebase Console sin necesidad de código.",
                        fontSize = 12.sp,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }
    }
}
