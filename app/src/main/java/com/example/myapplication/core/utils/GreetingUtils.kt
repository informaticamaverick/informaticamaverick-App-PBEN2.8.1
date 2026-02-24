package com.example.myapplication.core.utils

import java.util.Calendar

object GreetingUtils {
    
    fun getGreetingMessage(
        userName: String,
        location: String = "Desconocida",
        greetingType: GreetingType = GreetingType.STANDARD
    ): String {
        val timeGreeting = getTimeBasedGreeting()
        val emoji = getTimeBasedEmoji()
        
        val cleanName = userName.trim().ifEmpty { "Usuario" }
        
        return when (greetingType) {
            GreetingType.STANDARD -> "$timeGreeting, $cleanName $emoji"
            GreetingType.FORMAL -> "$timeGreeting Sr/Sra. $cleanName"
            GreetingType.FRIENDLY -> "¡Hola $cleanName! $timeGreeting $emoji"
            GreetingType.PROFESSIONAL -> "$timeGreeting, $cleanName"
        }
    }
    
    // Sobrecarga para solo tipo de saludo
    fun getGreetingMessage(greetingType: GreetingType): String {
        val timeGreeting = getTimeBasedGreeting()
        val emoji = getTimeBasedEmoji()
        
        return when (greetingType) {
            GreetingType.STANDARD -> timeGreeting
            GreetingType.FORMAL -> timeGreeting
            GreetingType.FRIENDLY -> "¡Hola! $timeGreeting $emoji"
            GreetingType.PROFESSIONAL -> timeGreeting
        }
    }
    
    fun getGreetingWithEmoji(
        userName: String,
        location: String = "Desconocida"
    ): String {
        val timeGreeting = getTimeBasedGreeting()
        val emoji = getTimeBasedEmoji()
        val cleanName = userName.trim().ifEmpty { "Usuario" }
        return "¡Hola $cleanName! $timeGreeting $emoji"
    }
    
    private fun getTimeBasedGreeting(): String {
        val hour = getCurrentHour()
        return when (hour) {
            in 5..11 -> "Buenos días"
            in 12..17 -> "Buenas tardes"
            in 18..23 -> "Buenas noches"
            else -> "Buenas noches"
        }
    }
    
    private fun getTimeBasedEmoji(): String {
        val hour = getCurrentHour()
        return when (hour) {
            in 5..6 -> "🌅"
            in 7..11 -> "☀️"
            in 12..14 -> "🌤️"
            in 15..17 -> "🌆"
            in 18..19 -> "🌇"
            in 20..23 -> "🌙"
            else -> "⭐"
        }
    }
    
    private fun getCurrentHour(): Int {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }
    
    fun getSubGreeting(hour: Int = getCurrentHour()): String {
        return when (hour) {
            in 5..11 -> "Espero que tengas un excelente día"
            in 12..17 -> "Que disfrutes tu tarde"
            in 18..23 -> "Que descanses bien esta noche"
            else -> "Que descanses bien"
        }
    }
    
    fun getTimeOfDay(): String {
        return when (getCurrentHour()) {
            in 5..11 -> "mañana"
            in 12..17 -> "tarde"
            in 18..23 -> "noche"
            else -> "madrugada"
        }
    }
}

enum class GreetingType {
    STANDARD,
    FORMAL,
    FRIENDLY,
    PROFESSIONAL
}
