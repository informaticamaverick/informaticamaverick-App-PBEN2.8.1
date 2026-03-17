package com.example.myapplication.ui.screens.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.GreetingCard
import com.example.myapplication.ui.components.SimpleGreetingText
import com.example.myapplication.ui.components.EmojiGreetingText
import com.example.myapplication.utils.GreetingType

@Composable
fun GreetingDemoScreen() {
    var selectedGreetingType by remember { mutableStateOf(GreetingType.FRIENDLY) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Título
        Text(
            text = "Demostración de Saludos",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        // Botones de selección
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GreetingType.values().forEach { type ->
                Button(
                    onClick = { selectedGreetingType = type },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (selectedGreetingType == type) 
                            Color(0xFF3B82F6) else Color(0xFFE2E8F0)
                    )
                ) {
                    Text(
                        text = type.name,
                        color = if (selectedGreetingType == type) Color.White else Color(0xFF1E293B)
                    )
                }
            }
        }
        
        // Ejemplo 1: Card de saludo
        Text(
            text = "Ejemplo 1: Tarjeta de Saludo",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        GreetingCard(
            userName = "Juan",
            location = "Buenos Aires, AR",
            greetingType = selectedGreetingType,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // Ejemplo 2: Texto simple
        Text(
            text = "Ejemplo 2: Texto Simple",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        SimpleGreetingText(
            userName = "María",
            location = "Madrid, ES",
            greetingType = selectedGreetingType
        )
        
        // Ejemplo 3: Con emoji
        Text(
            text = "Ejemplo 3: Con Emoji",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        EmojiGreetingText(
            userName = "Carlos",
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        // Ejemplos de diferentes usuarios
        Text(
            text = "Ejemplo 4: Diferentes Ubicaciones",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        SimpleGreetingText(
            userName = "Ana",
            location = "Lima, PE",
            greetingType = GreetingType.FORMAL
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        SimpleGreetingText(
            userName = "Diego",
            location = "Santiago, CL",
            greetingType = GreetingType.PROFESSIONAL
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        SimpleGreetingText(
            userName = "Sofia",
            location = "Ciudad de México, MX",
            greetingType = GreetingType.STANDARD
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
