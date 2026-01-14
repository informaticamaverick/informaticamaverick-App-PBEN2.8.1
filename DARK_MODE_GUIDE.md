# Guía de Modo Oscuro - Sistema de Temas Centralizado

## 🎨 Resumen

La app ahora usa el sistema de temas de Material3 centralizado. Esto significa que **NO necesitas detectar el modo oscuro manualmente** en cada pantalla.

## ✅ Cómo Usar en Nuevas Pantallas

### Opción 1: Usar MaterialTheme directamente (Recomendado)

```kotlin
@Composable
fun MiNuevaPantalla() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // ✅ Color de fondo
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface, // ✅ Color de tarjetas
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Mi texto",
                color = MaterialTheme.colorScheme.onSurface // ✅ Color de texto
            )
        }
        
        Text(
            text = "Texto secundario",
            color = MaterialTheme.colorScheme.onSurfaceVariant // ✅ Texto secundario
        )
        
        Divider(
            color = MaterialTheme.colorScheme.outline // ✅ Divisores
        )
    }
}
```

### Opción 2: Usar getAppColors() helper

```kotlin
@Composable
fun MiNuevaPantalla() {
    val colors = getAppColors() // ✅ Obtiene colores adaptativos automáticamente
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        Text(
            text = "Mi texto",
            color = colors.textPrimaryColor
        )
        
        Text(
            text = "Texto secundario",
            color = colors.textSecondaryColor
        )
    }
}
```

## 🎯 Colores Disponibles

### MaterialTheme.colorScheme (Opción 1)

| Propiedad | Uso | Modo Claro | Modo Oscuro |
|-----------|-----|------------|-------------|
| `background` | Fondo de pantalla | `#F8FAFC` | `#0F172A` |
| `surface` | Tarjetas, cards | `#FFFFFF` | `#1E293B` |
| `onBackground` | Texto en fondo | `#1E293B` | `#FFFFFF` |
| `onSurface` | Texto en superficie | `#1E293B` | `#FFFFFF` |
| `onSurfaceVariant` | Texto secundario | `#64748B` | `#94A3B8` |
| `outline` | Bordes, divisores | `#E2E8F0` | `#334155` |
| `primary` | Color primario (azul) | `#3B82F6` | `#3B82F6` |
| `error` | Errores (rojo) | `#EF4444` | `#EF4444` |

### AppColors (Opción 2)

```kotlin
val colors = getAppColors()
colors.backgroundColor       // Fondo principal
colors.surfaceColor         // Tarjetas
colors.textPrimaryColor     // Texto principal
colors.textSecondaryColor   // Texto secundario
colors.dividerColor         // Separadores
colors.accentBlue          // Azul (#3B82F6)
colors.accentYellow        // Amarillo (#FBBF24)
colors.accentRed           // Rojo (#EF4444)
colors.accentGreen         // Verde (#10B981)
```

## 🚫 NO Hacer

```kotlin
// ❌ MAL - No detectar el tema manualmente
val isDarkTheme = isSystemInDarkTheme()
val backgroundColor = if (isDarkTheme) Color.Black else Color.White

// ❌ MAL - No hardcodear colores
Text(text = "Hola", color = Color(0xFF1E293B))

// ✅ BIEN - Usar el tema
Text(text = "Hola", color = MaterialTheme.colorScheme.onSurface)
```

## 📝 Ejemplos Completos

### Formulario con TextField

```kotlin
@Composable
fun FormularioEjemplo() {
    val colors = getAppColors()
    
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Email", color = colors.textSecondaryColor) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.textPrimaryColor,
            unfocusedTextColor = colors.textPrimaryColor,
            focusedBorderColor = colors.accentBlue,
            unfocusedBorderColor = colors.textSecondaryColor,
            cursorColor = colors.accentBlue
        )
    )
}
```

### Card Personalizada

```kotlin
@Composable
fun MiCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Título",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Subtítulo",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

### AlertDialog

```kotlin
@Composable
fun MiDialogo() {
    val colors = getAppColors()
    
    AlertDialog(
        onDismissRequest = { },
        containerColor = colors.surfaceColor, // ✅ Fondo del diálogo
        title = {
            Text("Título", color = colors.textPrimaryColor)
        },
        text = {
            Text("Mensaje", color = colors.textSecondaryColor)
        },
        confirmButton = {
            TextButton(onClick = { }) {
                Text("OK", color = colors.accentBlue)
            }
        }
    )
}
```

## 🔄 Migración de Código Existente

Si tienes código que ya usa colores manuales:

**Antes:**
```kotlin
val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
```

**Después:**
```kotlin
// Opción 1
MaterialTheme.colorScheme.background

// Opción 2
getAppColors().backgroundColor
```

## 💡 Beneficios

1. ✅ **Consistencia**: Todos los colores en un solo lugar
2. ✅ **Mantenibilidad**: Cambias un color y se actualiza en toda la app
3. ✅ **Menos código**: No necesitas detectar el tema en cada pantalla
4. ✅ **Material Design**: Sigue las guías de Material3
5. ✅ **Barra de estado**: Se actualiza automáticamente

## 📚 Más Información

- [Material3 Color System](https://m3.material.io/styles/color/the-color-system/color-roles)
- [Compose Theme Guide](https://developer.android.com/jetpack/compose/designsystems/material3)
