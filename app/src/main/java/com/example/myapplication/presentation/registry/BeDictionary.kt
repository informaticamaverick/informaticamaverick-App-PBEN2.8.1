package com.example.myapplication.presentation.registry


import androidx.compose.ui.graphics.Color
import com.example.myapplication.presentation.components.BeEmotion
import com.example.myapplication.presentation.components.BeMessage

/** * --- DICCIONARIO CENTRALIZADO DE BE --- */
object BeDictionary {
    val HomeMessages = listOf(
        BeMessage("💡", "Usa el Menú Táctico inferior para filtrar prestadores verificados.", null, Color(0xFF22D3EE), emotion = BeEmotion.NORMAL),
        BeMessage("🚀", "¡Nuevas categorías disponibles! Explora los servicios destacados hoy.", null, Color(0xFF10B981), emotion = BeEmotion.HAPPY)
    )
    val BudgetMessages = listOf(
        BeMessage("⚖️", "Selecciona múltiples ofertas para que yo pueda ayudarte a analizarlas y compararlas.", "ANALIZAR", Color(0xFF9B51E0), Color.White, BeEmotion.HAPPY),
        BeMessage("📋", "Recuerda revisar los detalles de cada presupuesto antes de aceptar.", null, Color(0xFFFACC15), emotion = BeEmotion.NORMAL)
    )
    val ChatMessages = listOf(
        BeMessage("💬", "Nunca compartas datos de tarjetas de crédito o contraseñas a través del chat.", null, Color(0xFFF43F5E), Color.White, BeEmotion.ANGRY),
        BeMessage("👀", "Si el prestador no responde, puedo ayudarte a buscar alternativas rápidas.", "BUSCAR", Color(0xFF22D3EE), emotion = BeEmotion.NORMAL)
    )
    val CalendarMessages = listOf(
        BeMessage("📅", "Recuerda que si cancelas un turno, el sistema le avisará automáticamente.", null, Color(0xFF10B981), emotion = BeEmotion.NORMAL),
        BeMessage("⏰", "Tienes turnos pendientes de confirmación. ¡No los pierdas!", "VER TURNOS", Color(0xFFF59E0B), emotion = BeEmotion.SURPRISED)
    )
    val DefaultMessages = listOf(
        BeMessage("🤖", "Hola, soy Be. Estoy aquí para asistirte en todo lo que necesites.", null, Color(0xFF22D3EE), emotion = BeEmotion.NORMAL)
    )
}
