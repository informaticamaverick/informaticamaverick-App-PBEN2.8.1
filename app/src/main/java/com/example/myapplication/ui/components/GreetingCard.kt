package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.utils.GreetingType
import com.example.myapplication.utils.GreetingUtils.getGreetingMessage

@Composable
fun GreetingCard(
    userName: String,
    location: String,
    greetingType: GreetingType = GreetingType.FRIENDLY,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = getGreetingMessage(greetingType),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3B82F6)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = location,
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun SimpleGreetingText(
    userName: String,
    location: String,
    greetingType: GreetingType = GreetingType.STANDARD,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "${getGreetingMessage(greetingType)}, $userName",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E293B)
        )
        
        Text(
            text = location,
            fontSize = 14.sp,
            color = Color(0xFF64748B)
        )
    }
}

@Composable
fun EmojiGreetingText(
    userName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "👋 ¡Hola, $userName!",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF3B82F6),
        modifier = modifier
    )
}

/**
 * GreetingCard: Tarjetas y textos de saludo personalizados (GreetingCard, SimpleGreetingText, EmojiGreetingText)
 * con nombre de usuario, ubicación y tipo de saludo dinámico según hora del día.
 */
