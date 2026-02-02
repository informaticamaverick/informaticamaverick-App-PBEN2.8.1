# 🎨 REFERENCIA RÁPIDA - COLORES DEL TEMA DEL SISTEMA

## Cómo Usar Colores Adaptativos

### ✅ CORRECTO - Usar colores del tema
```kotlin
// Al inicio de cada pantalla
val colors = MaterialTheme.colorScheme
val isSystemInDarkMode = isSystemInDarkTheme()

// En componentes
Surface(
    color = colors.surface,  // Se adapta automáticamente
    shape = CircleShape,
    border = BorderStroke(2.5.dp, rainbowBrush)
) {
    Icon(
        Icons.Default.Category,
        tint = colors.onSurface  // Se adapta automáticamente
    )
}
```

### ❌ INCORRECTO - NO usar colores hardcodeados
```kotlin
// ❌ NO HACER ESTO
Surface(
    color = Color(0xFF121212),  // ❌ Hardcodeado
    shape = CircleShape
) {
    Icon(
        Icons.Default.Category,
        tint = Color.White  // ❌ Hardcodeado
    )
}
```

---

## 📊 TABLA DE COLORES RECOMENDADOS

### Para Superficies y Fondos
| Uso | Color del Tema | Modo Claro | Modo Oscuro |
|---|---|---|---|
| Fondo principal | `colors.background` | Blanco/Gris | Gris oscuro |
| Superficies (Cards, FABs) | `colors.surface` | Gris claro | Gris muy oscuro |
| Superficie variante | `colors.surfaceVariant` | Gris más claro | Gris menos oscuro |
| Contenedor primario | `colors.primaryContainer` | Azul claro | Azul oscuro |

### Para Texto e Iconos
| Uso | Color del Tema | Modo Claro | Modo Oscuro |
|---|---|---|---|
| Texto principal | `colors.onSurface` | Negro | Blanco |
| Texto sobre primario | `colors.onPrimaryContainer` | Azul oscuro | Azul claro |
| Texto secundario | `colors.onSurfaceVariant` | Gris | Gris claro |
| Placeholder | `colors.onSurfaceVariant` | Gris | Gris claro |

### Para Acciones
| Uso | Color del Tema |
|---|---|
| Botones primarios | `colors.primary` |
| Botones secundarios | `colors.secondary` |
| Botones de error | `colors.error` |
| Botón desactivado | `colors.surface` |

---

## 🎯 CASOS DE USO POR PANTALLA

### PresupuestosScreen
✅ **Implementado:**
- FABs de filtros (Categoría, Estado, Ordenar)
- Barra de búsqueda flotante
- Diálogos de filtros

**Colores usados:** surface, onSurface, primaryContainer, onPrimaryContainer

### ChatScreen
✅ **Implementado:**
- FABs de filtros (Categoría, No Leídos, Ordenar)
- Barra de búsqueda flotante
- Lista de chats

**Colores usados:** surface, onSurface, primaryContainer, onPrimaryContainer

### CalendarScreen
✅ **Implementado:**
- FABs de filtros (Estado, Limpiar, Ordenar)
- Barra de búsqueda flotante
- Widget del calendario

**Colores usados:** surface, onSurface, primaryContainer, onPrimaryContainer, accentBlue (custom)

### PromoScreen
✅ **Implementado:**
- FABs de filtros (Categorías, Descuento, Ordenar)
- Barra de búsqueda flotante
- Tarjetas de promociones

**Colores usados:** surface, onSurface, primaryContainer, onPrimaryContainer

---

## 🚀 CÓMO AGREGAR NUEVAS PANTALLAS CON SOPORTE A TEMA

### Paso 1: Importar dependencias
```kotlin
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
```

### Paso 2: Agregar variables al inicio de la pantalla
```kotlin
@Composable
fun MiNuevaPantalla() {
    val colors = MaterialTheme.colorScheme
    val isSystemInDarkMode = isSystemInDarkTheme()
    
    // Tu código aquí...
}
```

### Paso 3: Usar colores adaptativos
```kotlin
Surface(
    color = colors.surface,
    // ...
) {
    Icon(
        Icons.Default.MyIcon,
        tint = colors.onSurface
    )
    Text(
        "Mi texto",
        color = colors.onSurface
    )
}
```

### Paso 4: Para FABs con estado
```kotlin
Surface(
    color = if (isActivo) colors.primaryContainer else colors.surface,
    // ...
) {
    Icon(
        Icons.Default.MyIcon,
        tint = if (isActivo) colors.onPrimaryContainer else colors.onSurface
    )
}
```

---

## 🎨 COLORES ESPECIALES A PRESERVAR

### ✅ Mantener Intactos
- Gradiente Rainbow: `geminiGradientEffect()`
- Colores de estado (rojo, verde, naranja)
- Colores de iconografía de datos
- Colores de error/éxito/advertencia

### ❌ NO Modificar
```kotlin
// ✅ OK - Mantener estos
gradient = geminiGradientEffect()  // Rainbow
color = Color(0xFF10B981)  // Verde éxito
color = Color(0xFFEF4444)  // Rojo error
```

---

## 🔍 VERIFICACIÓN RÁPIDA

Para verificar que una pantalla está correctamente adaptada:

- [ ] ¿Tiene `val colors = MaterialTheme.colorScheme`?
- [ ] ¿Tiene `import androidx.compose.foundation.isSystemInDarkTheme`?
- [ ] ¿Usa `colors.surface` para fondos?
- [ ] ¿Usa `colors.onSurface` para texto?
- [ ] ¿Usa `colors.primaryContainer` para botones activos?
- [ ] ¿Usa `colors.onPrimaryContainer` para texto en activos?
- [ ] ¿Preserva `geminiGradientEffect()`?
- [ ] ¿Compila sin errores?

---

## 📝 EJEMPLO COMPLETO

```kotlin
@Composable
fun MiPantalla() {
    // 1. Obtener colores del tema
    val colors = MaterialTheme.colorScheme
    val isSystemInDarkMode = isSystemInDarkTheme()
    
    // 2. Estados
    var isFiltering by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold {
            // 3. FAB con estado
            Surface(
                modifier = Modifier.size(56.dp),
                onClick = { isFiltering = !isFiltering },
                shape = CircleShape,
                color = if (isFiltering) colors.primaryContainer else colors.surface,
                border = BorderStroke(2.5.dp, geminiGradientEffect()),
                shadowElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.FilterList,
                        tint = if (isFiltering) colors.onPrimaryContainer else colors.onSurface
                    )
                }
            }
            
            // 4. Barra de búsqueda
            Surface(
                color = colors.surface,
                shape = RoundedCornerShape(28.dp)
            ) {
                TextField(
                    textStyle = TextStyle(color = colors.onSurface)
                )
            }
        }
    }
}
```

---

## ❓ PREGUNTAS FRECUENTES

**P: ¿Se adapta automáticamente?**
R: Sí, `MaterialTheme.colorScheme` se adapta automáticamente al tema del sistema.

**P: ¿Necesito hacer algo especial para modo oscuro?**
R: No, el sistema lo maneja automáticamente. Solo usa `colors.*`.

**P: ¿Puedo forzar un modo específico?**
R: Sí, pero NO es recomendado. Respeta la preferencia del usuario.

**P: ¿Y si necesito un color personalizado?**
R: Crea variables locales pero úsalas junto con los colores del tema.

**P: ¿El gradiente rainbow se mantiene igual?**
R: Sí, `geminiGradientEffect()` se preservó intacto en todas las pantallas.

---

**Última actualización:** 2024
**Estado:** ✅ Completado y verificado
