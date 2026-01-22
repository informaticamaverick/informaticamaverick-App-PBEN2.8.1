package com.example.myapplication.Client

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
// 1. Añadimos las importaciones que faltaban para los íconos de estrella.
// Star y StarHalf pertenecen a la colección "filled".
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
// StarBorder pertenece a la colección "outlined".
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.floor

/**
 * Componente reutilizable para mostrar una valoración con estrellas.
 * @param rating La valoración numérica (ej. 4.5f).
 * @param modifier Modificadores de diseño.
 * @param starColor El color de las estrellas.
 */
@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    starColor: Color = Color(0xFFFFD700) // Color dorado por defecto
) {
    Row(modifier = modifier) {
        val fullStars = floor(rating).toInt()
        val hasHalfStar = (rating - fullStars) >= 0.5f
        val emptyStars = 5 - fullStars - if (hasHalfStar) 1 else 0

        repeat(fullStars) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = starColor, modifier = Modifier.size(16.dp))
        }
        if (hasHalfStar) {
            // Ahora el compilador encontrará esta referencia gracias a la importación
            Icon(Icons.Filled.StarHalf, contentDescription = null, tint = starColor, modifier = Modifier.size(16.dp))
        }
        repeat(emptyStars) {
            // Y también encontrará esta referencia
            Icon(Icons.Outlined.StarBorder, contentDescription = null, tint = starColor, modifier = Modifier.size(16.dp))
        }
    }
}
