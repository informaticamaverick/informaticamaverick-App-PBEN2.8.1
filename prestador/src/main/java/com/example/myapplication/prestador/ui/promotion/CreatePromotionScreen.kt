
package com.example.myapplication.prestador.ui.promotion

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.model.PromotionType
import com.example.myapplication.prestador.data.model.ProviderPromotion
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.CreatePromotionViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePromotionScreen(
    onBack: () -> Unit,
    onPublish: (ProviderPromotion) -> Unit,
    viewModel: CreatePromotionViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val context = LocalContext.current

    // Estados del formulario
    var selectedType by remember { mutableStateOf(PromotionType.PROMOTION) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var discount by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Estados del ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val vmErrorMessage by viewModel.errorMessage.collectAsState()
    
    // Mostrar mensajes de éxito/error
    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }
    
    LaunchedEffect(vmErrorMessage) {
        vmErrorMessage?.let {
            errorMessage = it
            viewModel.clearMessages()
        }
    }

    // Launcher para seleccionar imágenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.size <= 3) {
            selectedImages = uris
            errorMessage = null
        } else {
            errorMessage = "Máximo 3 imágenes permitidas"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Promoción", color = colors.textPrimary)},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surfaceColor
                )
            )
        },
        containerColor = colors.backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //Selector de tipo
            PromotionTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it },
                colors = colors
            )

            //Campo título
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titulo de la promoción")},
                placeholder = { Text("Ej: Descuento especial en plomería")},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    focusedLabelColor = colors.primaryOrange
                )
            )

            //Campo descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it},
                label = { Text("Descripción")},
                placeholder = { Text("Describe tu oferta o servicio...")},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    focusedLabelColor = colors.primaryOrange
                )

            )

            //Selector de imágenes
            ImageSelector(
                selectedImages = selectedImages,
                onAddImages = { imagePickerLauncher.launch("image/*")},
                onRemoveImage = { uri ->
                    selectedImages = selectedImages.filter { it != uri }
                },
                colors = colors
            )

            //Selector de categorias
            CategorySelector(
                selectedCategories = selectedCategories,
                onShowDialog = { showCategoryDialog = true },
                colors = colors
            )

            //Campo descuento
            OutlinedTextField(
                value = discount,
                onValueChange = {
                    if (it.isEmpty() || (it.toIntOrNull() in 1..100)) {
                        discount = it
                    }
                },
                label = { Text("Descuento (opcional)") },
                placeholder = { Text("Ej: 30") },
                suffix = { Text("%") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryOrange,
                    focusedLabelColor = colors.primaryOrange
                )
            )

            // Mensaje de error

            errorMessage?.let {
                Text(
                    text = it,
                    color = colors.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            //botón de publicar
            Button(
                onClick = {
                    // Llamar al ViewModel para guardar en la BD
                    viewModel.createPromotion(
                        providerId = "test_provider_123", // TODO: Obtener del usuario actual
                        providerName = "Prestador de Prueba",
                        providerImageUrl = null,
                        type = selectedType,
                        title = title,
                        description = description,
                        imageUrls = selectedImages,
                        discount = discount,
                        categories = selectedCategories,
                        onSuccess = {
                            // Navegar de vuelta al hacer clic en OK del Toast
                            onBack()
                        }
                    )
                },
                enabled = !isLoading, // Deshabilitar mientras carga
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryOrange
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publicar Promoción", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    
    // Diálogo de categorías
    if (showCategoryDialog) {
        CategoryDialog(
            selectedCategories = selectedCategories,
            onCategoryToogle = { category ->
                selectedCategories = if (selectedCategories.contains(category)) {
                    selectedCategories - category
                } else {
                    selectedCategories + category
                }
            },
            onDismiss = { showCategoryDialog = false },
            colors = colors
        )
    }
}

@Composable
fun PromotionTypeSelector(
    selectedType: PromotionType,
    onTypeSelected: (PromotionType) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    Column {
        Text(
            text = "Tipo de publicación",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PromotionType.values().forEach {
                type ->
                TypeCard(
                    type = type,
                    isSelected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@Composable
fun TypeCard(
    type: PromotionType,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
            colors.primaryOrange else colors.surfaceColor
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected)
            colors.primaryOrange else colors.border
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (type == PromotionType.STORY) Icons.Default.FlashOn else
                    Icons.Default.Campaign,
                contentDescription = null,
                tint = if (isSelected) Color.White else colors.textPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (type == PromotionType.STORY) "Historia" else
                "Promoción",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White.copy(alpha = 0.9f) else
                colors.textSecondary
            )
        }
    }
}

@Composable
fun ImageSelector(
    selectedImages: List<Uri>,
    onAddImages: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    Column {
        Text(
            text = "Imagenes (máximo 3)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //Botón agregar imagen
            if (selectedImages.size < 3) {
                item {
                    Card(
                        modifier = Modifier
                            .size(120.dp)
                            .clickable { onAddImages() },
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surfaceColor
                        ),
                        border = BorderStroke(2.dp, colors.border),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Agregar imagen",
                                    tint = colors.primaryOrange,
                                    modifier = Modifier.size(40.dp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Agregar",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            //Imágenes seleccionadas
            items(selectedImages) { uri ->
                Box(modifier = Modifier.size(120.dp)) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { onRemoveImage(uri)},
                        modifier = Modifier.align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySelector(
    selectedCategories: Set<String>,
    onShowDialog: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    Column {
        Text(
            text = "Categorías",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowDialog() },
            colors = CardDefaults.cardColors(
                containerColor = colors.surfaceColor
            ),
            border = BorderStroke(1.dp, colors.border),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategories.isEmpty())
                    "Seleccionar categorias" else
                    "${selectedCategories.size} selecionada(s)",
                    color = if (selectedCategories.isEmpty())
                    colors.textSecondary else colors.textPrimary
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = colors.textSecondary
                )
            }
        }

        if (selectedCategories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedCategories.toList()) { category ->
                    AssistChip(
                        onClick = { },
                        label = { Text(category, fontSize = 12.sp)},
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = colors.primaryOrange.copy(alpha = 0.1f),
                            labelColor = colors.primaryOrange
                        )

                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDialog(
    selectedCategories: Set<String>,
    onCategoryToogle: (String) -> Unit,
    onDismiss: () ->Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    val categories = listOf(
        "Plomería", "Electricidad",
        "Carpintería", "Pintura",
        "Albañilería", "Jardinería",
        "Limpieza", "Cerrajería",
        "Aire Acondicionado", "Reparaciones Generales"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Selecciona categorías", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                categories.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategoryToogle(category) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedCategories.contains(category),
                            onCheckedChange = { onCategoryToogle(category) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colors.primaryOrange
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(category)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Aceptar", color = colors.primaryOrange)
            }
        },
        containerColor = colors.surfaceColor
    )
}