# Sistema de Categorías con Firebase

## Archivos Creados:

1. **Category.kt** - Modelo de datos para las categorías
2. **CategoryRepository.kt** - Repositorio para operaciones con Firestore
3. **CategoryViewModel.kt** - ViewModel para manejar la lógica
4. **CategoryAdminScreen.kt** - Pantalla de administración

## Cómo usar:

### Opción 1: Usar la pantalla de administración

1. Agrega la ruta a tu navegación en MainActivity o donde manejes las rutas
2. Navega a `CategoryAdminScreen`
3. Presiona el botón "Inicializar Categorías" 
4. Las 18 categorías se subirán automáticamente a Firebase Firestore

### Opción 2: Llamar directamente desde código

```kotlin
val categoryViewModel: CategoryViewModel = hiltViewModel()

// Para inicializar todas las categorías
categoryViewModel.initializeCategories()

// Para cargar categorías desde Firebase
categoryViewModel.loadCategories()

// Para buscar categorías
categoryViewModel.searchCategories("Electricista")
```

### Opción 3: Usar en ClientDashboardScreen

Modifica ClientDashboardScreen para usar las categorías de Firebase:

```kotlin
@Composable
fun ClientDashboardScreen(
    // ... otros parámetros
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val firebaseCategories by categoryViewModel.categories.collectAsState()
    
    // Usar firebaseCategories en lugar de la lista hardcodeada
    LazyVerticalGrid(...) {
        items(firebaseCategories) { category ->
            // Renderizar categoría
        }
    }
}
```

## Estructura en Firebase:

Las categorías se guardan en la colección "categories" con esta estructura:
```
categories/
  └─ [documentId]/
      ├─ id: String
      ├─ name: String (ej: "Electricista")
      ├─ iconName: String (ej: "ic_electricista")
      ├─ colorHex: String (ej: "#FBBF24")
      └─ order: Int (1-18)
```

## Funciones disponibles:

- `addCategory()` - Agregar una categoría individual
- `addCategories()` - Agregar múltiples categorías
- `getAllCategories()` - Obtener todas las categorías
- `searchCategories()` - Buscar por nombre
- `deleteCategory()` - Eliminar una categoría
- `initializeCategories()` - Poblar Firebase con las 18 categorías iniciales
