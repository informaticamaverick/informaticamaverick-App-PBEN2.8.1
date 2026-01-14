# Sistema de Subcategorías con Firebase

## ✅ Archivos Creados:

1. **SubCategory.kt** - Modelo de datos para las subcategorías
2. **SubCategoryRepository.kt** - Repositorio para operaciones con Firestore
3. **SubCategoryViewModel.kt** - ViewModel para manejar la lógica

## 🚀 Cómo Agregar Subcategorías a Firebase:

### Paso 1: Subir Categorías Primero
1. Abre la app
2. Presiona el botón verde "🔥 Subir Categorías a Firebase"
3. Espera a que se completen

### Paso 2: Subir Subcategorías de Mecánico
1. Presiona el botón azul "🔧 Agregar Subcategorías Mecánico"
2. Esto agregará automáticamente 6 subcategorías para la categoría Mecánico:
   - Mecánico de Motor
   - Mecánico de Tren Delantero
   - Mecánico de Transmisión
   - Mecánico Eléctrico Automotriz
   - Mecánico de Aire Acondicionado
   - Mecánico Diesel

## 📊 Estructura en Firebase:

```
Firestore Database:
├── categories/
│   └── [categoryId]/
│       ├── id: "abc123"
│       ├── name: "Mecánico"
│       ├── iconName: "ic_mecanico"
│       ├── colorHex: "#475569"
│       └── order: 7
│
└── subcategories/
    └── [subCategoryId]/
        ├── id: "xyz789"
        ├── categoryId: "abc123" (referencia a la categoría padre)
        ├── name: "Mecánico de Motor"
        ├── description: "Especialista en reparación..."
        ├── iconName: "ic_motor"
        ├── order: 1
        └── isActive: true
```

## 💡 Cómo Agregar Subcategorías a Otras Categorías:

### Opción 1: Usar código directamente

```kotlin
// En tu código, obtén el ID de la categoría
val categoryId = categoryViewModel.getCategoryIdByName("Electricista")

// Luego agrega las subcategorías
subCategoryViewModel.addSubCategoriesForCategory(
    categoryId = categoryId!!,
    categoryName = "Electricista",
    subCategoryNames = listOf(
        "Electricista Residencial",
        "Electricista Industrial",
        "Electricista Automotriz",
        "Instalador de Paneles Solares"
    )
)
```

### Opción 2: Crear más botones temporales

Agrega más botones en ClientDashboardScreen para otras categorías:

```kotlin
Button(onClick = { 
    coroutineScope.launch {
        val plomeroId = categoryViewModel.getCategoryIdByName("Plomero")
        if (plomeroId != null) {
            subCategoryViewModel.addSubCategoriesForCategory(
                categoryId = plomeroId,
                categoryName = "Plomero",
                subCategoryNames = listOf(
                    "Plomero Residencial",
                    "Plomero de Gas",
                    "Plomero de Emergencia",
                    "Destapador de Cañerías"
                )
            )
        }
    }
}) {
    Text("🔧 Agregar Subcategorías Plomero")
}
```

## 🔍 Funciones Disponibles:

### CategoryViewModel
- `initializeCategories()` - Crear las 18 categorías iniciales
- `getCategoryIdByName(name)` - Obtener ID de una categoría por nombre

### SubCategoryViewModel
- `initializeSubCategories(mechanicId)` - Crear subcategorías de mecánico
- `addSubCategoriesForCategory(id, name, list)` - Agregar subcategorías a cualquier categoría
- `loadSubCategoriesByCategory(categoryId)` - Cargar subcategorías de una categoría
- `loadAllSubCategories()` - Cargar todas las subcategorías

## 📱 Ejemplo de Uso en UI:

```kotlin
// Obtener subcategorías de una categoría
val subCategories by subCategoryViewModel.subCategories.collectAsState()

// Cargar subcategorías cuando el usuario selecciona una categoría
LaunchedEffect(selectedCategoryId) {
    subCategoryViewModel.loadSubCategoriesByCategory(selectedCategoryId)
}

// Mostrar subcategorías
LazyColumn {
    items(subCategories) { subCategory ->
        Text(text = subCategory.name)
    }
}
```

## 🎯 Próximos Pasos Sugeridos:

1. Crear una pantalla de administración de subcategorías
2. Permitir que los usuarios seleccionen subcategorías al buscar profesionales
3. Filtrar profesionales por subcategoría
4. Agregar subcategorías para las demás categorías principales
