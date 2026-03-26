package com.example.myapplication.data.model

// Este archivo define QUÉ tipo de cosa estamos enviando en el chat.
enum class MessageType {
    TEXT,       // Texto simple
    IMAGE,      // Foto (Uri o Path)
    AUDIO,      // Nota de voz
    LOCATION,   // Ubicación (Lat/Lng)
    VISIT,      // Una cita técnica agendada
    BUDGET,      // Un presupuesto formal recibido
    TENDER,    // 🔥 NUEVO: Invitación a Licitación enviada
    SYSTEM    // 🔥 AGREGA ESTA LÍNEA PARA QUITAR EL ERROR
}