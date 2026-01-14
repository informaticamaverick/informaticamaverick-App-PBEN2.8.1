# 📚 Guía: Cómo Agregar Categorías desde Firebase Console

## ✅ Botones Provisorios ELIMINADOS

La app ahora carga las categorías **automáticamente desde Firebase** al iniciar.

---

## 🔥 Cómo Agregar Categorías desde Firebase Console

### Paso 1: Abrir Firebase Console
1. Ve a https://console.firebase.google.com
2. Selecciona tu proyecto
3. En el menú lateral, haz clic en **"Firestore Database"**

### Paso 2: Crear la Colección "categories" (si no existe)
1. Haz clic en **"Start collection"** o **"Iniciar colección"**
2. Escribe el nombre: **`categories`**
3. Haz clic en **"Next"** / **"Siguiente"**

### Paso 3: Agregar una Nueva Categoría
1. Firestore te pedirá crear el primer documento
2. En **"Document ID"**, selecciona **"Auto-ID"** (ID automático)
3. Agrega los siguientes campos:

```
Campo         | Tipo    | Valor (ejemplo)
------------- | ------- | ---------------
name          | string  | Electricista
iconName      | string  | ic_electricista
colorHex      | string  | #FBBF24
order         | number  | 1
```

4. Haz clic en **"Save"** / **"Guardar"**

### Paso 4: Agregar Más Categorías
1. Haz clic en **"Add document"** / **"Agregar documento"**
2. Repite el proceso con diferentes valores:

#### Ejemplo: Categoría "Plomero"
```
name: Plomero
iconName: ic_plomero
colorHex: #06B6D4
order: 2
```

#### Ejemplo: Categoría "Pintura"
```
name: Pintura
iconName: ic_pintura
colorHex: #EC4899
order: 3
```

---

## 🎨 Colores Disponibles (Formato HEX)

| Color              | Código HEX |
|--------------------|------------|
| Amarillo/Dorado    | #FBBF24    |
| Azul Cyan          | #06B6D4    |
| Rosa               | #EC4899    |
| Verde              | #10B981    |
| Morado             | #8B5CF6    |
| Verde Lima         | #84CC16    |
| Gris Oscuro        | #475569    |
| Naranja            | #F97316    |
| Marrón Oscuro      | #92400E    |
| Marrón             | #78350F    |
| Rosa Oscuro        | #DB2777    |
| Azul Claro         | #0891B2    |
| Verde Oscuro       | #16A34A    |
| Azul Oscuro        | #0369A1    |
| Verde Esmeralda    | #059669    |
| Naranja Oscuro     | #EA580C    |
| Azul Cielo         | #0284C7    |
| Gris               | #9CA3AF    |

---

## 🖼️ Iconos Disponibles

Estos son los iconos que ya existen en la app:

```
ic_electricista
ic_plomero
ic_pintura
ic_mudanza
ic_limpieza
ic_jardin
ic_mecanico
ic_albanil
ic_otros
```

**Nota:** Si usas un `iconName` que no existe, la app mostrará `ic_otros` por defecto.

---

## 📊 Estructura Completa de una Categoría

```json
{
  "id": "auto-generado-por-firebase",
  "name": "Mecánico",
  "iconName": "ic_mecanico",
  "colorHex": "#475569",
  "order": 7
}
```

---

## 🔧 Cómo Agregar Subcategorías

### Paso 1: Seleccionar la Categoría Padre
1. En Firestore, abre la colección **`categories`**
2. Haz clic en el documento de la categoría (ej: "Mecánico")

### Paso 2: Crear Subcolección
1. En la vista del documento, busca **"Start collection"** / **"Iniciar colección"**
2. Nombre de la subcolección: **`subcategories`**
3. Haz clic en **"Next"**

### Paso 3: Agregar Subcategoría
1. ID del documento: **Auto-ID**
2. Agrega los campos:

```
Campo         | Tipo    | Valor (ejemplo)
------------- | ------- | ---------------------------
id            | string  | (auto-generado)
categoryId    | string  | (ID de la categoría padre)
name          | string  | Mecánico de Motor
description   | string  | Especialista en reparación de motores
iconName      | string  | ic_motor
order         | number  | 1
isActive      | boolean | true
```

3. Haz clic en **"Save"**

### Ejemplo Visual:
```
categories/
  └── abc123 (Mecánico)
      └── subcategories/
          ├── xyz001
          │   ├── name: "Mecánico de Motor"
          │   ├── order: 1
          │   └── isActive: true
          └── xyz002
              ├── name: "Mecánico de Tren Delantero"
              ├── order: 2
              └── isActive: true
```

---

## 🚀 La App Cargará Automáticamente

- ✅ Al abrir la app, carga automáticamente todas las categorías de Firebase
- ✅ Si no hay categorías en Firebase, muestra categorías locales por defecto
- ✅ Muestra un indicador de carga mientras obtiene los datos
- ✅ Actualiza en tiempo real cuando agregas nuevas categorías

---

## 🎯 Ejemplo Rápido: Agregar Categoría "Jardinero"

1. Firebase Console → Firestore Database
2. Colección `categories` → **Add document**
3. Auto-ID activado
4. Agregar campos:
   ```
   name: Jardinero
   iconName: ic_jardin
   colorHex: #84CC16
   order: 20
   ```
5. **Save**
6. **Reinicia la app** y verás la nueva categoría

---

## ⚠️ Notas Importantes

1. **El campo `order`** determina el orden de aparición (menor número = aparece primero)
2. **El campo `name`** es lo que se muestra en la app
3. **El campo `colorHex`** DEBE empezar con `#` (ej: #FBBF24)
4. **El campo `iconName`** debe coincidir con un icono existente o usará el default

---

## 🔄 Cómo Editar una Categoría Existente

1. Firebase Console → Firestore Database
2. Abre la colección `categories`
3. Haz clic en el documento que quieres editar
4. Haz clic en el **icono de lápiz** junto al campo
5. Modifica el valor
6. Haz clic en **Update** / **Actualizar**
7. Reinicia la app para ver los cambios

---

## 🗑️ Cómo Eliminar una Categoría

1. Firebase Console → Firestore Database
2. Abre la colección `categories`
3. Haz clic en el documento
4. Haz clic en el **icono de tres puntos** (⋮)
5. Selecciona **"Delete document"** / **"Eliminar documento"**
6. Confirma la eliminación

**Nota:** Si la categoría tiene subcategorías, estas NO se eliminarán automáticamente. Debes eliminarlas manualmente.

---

## ✨ Ventajas de este Sistema

✅ No necesitas recompilar la app para agregar categorías  
✅ Puedes actualizar categorías en tiempo real  
✅ Fácil de administrar desde Firebase Console  
✅ Estructura profesional y escalable  
✅ Soporte para subcategorías organizadas  

---

## 🎓 Siguiente Paso

Ahora que sabes cómo agregar categorías, puedes:
1. Agregar todas las categorías que necesites desde Firebase Console
2. Crear subcategorías para cada una
3. La app las mostrará automáticamente

¡Sin necesidad de tocar código! 🚀
