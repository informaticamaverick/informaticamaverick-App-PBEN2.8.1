package com.example.myapplication.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.Components.CustomTextField
import com.example.myapplication.Components.PrimaryButton

@Composable
fun CompleteProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    userName: String,
    onProfileComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Colores adaptativos para modo oscuro
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val surfaceColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textPrimaryColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onProfileComplete()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3B82F6),
                        Color(0xFF6366F1)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "¡Bienvenido, $userName!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "Completa tu perfil para continuar",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Datos de Contacto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Teléfono
                    CustomTextField(
                        value = uiState.phoneNumber,
                        onValueChange = { viewModel.onPhoneNumberChange(it) },
                        placeholder = "Número de teléfono",
                        icon = Icons.Default.Phone
                    )
                    
                    // Contraseña
                    CustomTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        placeholder = "Contraseña",
                        icon = Icons.Default.Lock,
                        isPassword = true
                    )
                    
                    // Dirección
                    CustomTextField(
                        value = uiState.address,
                        onValueChange = { viewModel.onAddressChange(it) },
                        placeholder = "Dirección completa",
                        icon = Icons.Default.Home
                    )
                    
                    // Ciudad
                    CustomTextField(
                        value = uiState.city,
                        onValueChange = { viewModel.onCityChange(it) },
                        placeholder = "Ciudad",
                        icon = Icons.Default.LocationOn
                    )
                    
                    // Estado
                    CustomTextField(
                        value = uiState.state,
                        onValueChange = { viewModel.onStateChange(it) },
                        placeholder = "Estado/Provincia",
                        icon = Icons.Default.Place
                    )
                    
                    // Código Postal
                    CustomTextField(
                        value = uiState.zipCode,
                        onValueChange = { viewModel.onZipCodeChange(it) },
                        placeholder = "Código Postal",
                        icon = Icons.Default.Build
                    )
                    
                    // Mensaje de error
                    if (uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón guardar
                    PrimaryButton(
                        text = if (uiState.isLoading) "Guardando..." else "Guardar y Continuar",
                        onClick = { viewModel.saveProfile() },
                        enabled = !uiState.isLoading,
                        backgroundColor = Color(0xFF3B82F6)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
