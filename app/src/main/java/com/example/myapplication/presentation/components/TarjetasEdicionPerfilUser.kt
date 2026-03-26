package com.example.myapplication.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.BranchClient
import com.example.myapplication.data.model.CompanyClient
import com.example.myapplication.data.model.RepresentativeClient

/**
 * 4. HOJA DE EDICIÓN DE EMPRESA (Company)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCompanySheetContent(
    company: CompanyClient,
    onSave: (CompanyClient) -> Unit,
    onClose: () -> Unit
) {
    var name by remember { mutableStateOf(company.name) }
    var cuit by remember { mutableStateOf(company.cuit) }
    var email by remember { mutableStateOf(company.email) }
    var razonSocial by remember { mutableStateOf(company.razonSocial) }

    BentoBottomSheetContent(
        title = "Datos de Empresa",
        emoji = "🏢",
        onClose = onClose,
        showPrimaryButton = true,
        onPrimaryButtonClick = { 
            onSave(company.copy(name = name, cuit = cuit, email = email, razonSocial = razonSocial)) 
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            BentoTextFieldM3(value = name, onValueChange = { name = it }, label = "Nombre Comercial", emoji = "🏢")
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = razonSocial, onValueChange = { razonSocial = it }, label = "Razón Social", emoji = "🏭")
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = cuit, onValueChange = { cuit = it }, label = "CUIT", emoji = "🆔")
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = email, onValueChange = { email = it }, label = "Email Corporativo", emoji = "📧")
        }
    }
}

/**
 * 3. HOJA DE EDICIÓN DE SUCURSAL (Branch)
 */
@Composable
fun EditBranchSheetContent(
    branch: BranchClient,
    onSave: (BranchClient) -> Unit,
    onClose: () -> Unit
) {
    var name by remember { mutableStateOf(branch.name) }
    var isMain by remember { mutableStateOf(branch.isMainBranch) }

    BentoBottomSheetContent(
        title = "Editar Sucursal",
        emoji = "🏪",
        onClose = onClose,
        showPrimaryButton = true,
        onPrimaryButtonClick = { onSave(branch.copy(name = name, isMainBranch = isMain)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            BentoTextFieldM3(value = name, onValueChange = { name = it }, label = "Nombre de Sucursal", emoji = "🏪")

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 16.dp)) {
                Checkbox(
                    checked = isMain, 
                    onCheckedChange = { isMain = it },
                    colors = CheckboxDefaults.colors(checkedColor = GeminiAccent, uncheckedColor = Color.Gray)
                )
                Spacer(Modifier.width(8.dp))
                Text("Es Casa Central / Sede Principal", color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

/**
 * 1. HOJA DE EDICIÓN DE UBICACIÓN (Address)
 */
@Composable
fun EditAddressSheetContent(
    address: AddressClient,
    onSave: (AddressClient) -> Unit,
    onClose: () -> Unit = {}
) {
    var calle by remember { mutableStateOf(address.calle) }
    var numero by remember { mutableStateOf(address.numero) }
    var localidad by remember { mutableStateOf(address.localidad) }
    var provincia by remember { mutableStateOf(address.provincia) }
    var pais by remember { mutableStateOf(address.pais) }
    var codigoPostal by remember { mutableStateOf(address.codigoPostal) }
    var label by remember { mutableStateOf(address.label) }

    val emojis = listOf("🏠 Casa", "🏢 Oficina", "👵 Abuela", "🏖️ Vacaciones", "📍 Otro")

    BentoBottomSheetContent(
        title = "Ubicación",
        emoji = "📍",
        onClose = onClose,
        showPrimaryButton = true,
        onPrimaryButtonClick = { 
            onSave(address.copy(
                calle = calle, 
                numero = numero, 
                localidad = localidad, 
                provincia = provincia, 
                pais = pais,
                codigoPostal = codigoPostal,
                label = label
            )) 
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text("Tipo de dirección", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                items(emojis) { item ->
                    val isSelected = label == item
                    Surface(
                        onClick = { label = item },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) GeminiAccent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                        border = if (isSelected) BorderStroke(1.dp, GeminiAccent) else null
                    ) {
                        Text(item, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = if (isSelected) GeminiAccent else Color.White)
                    }
                }
            }

            BentoTextFieldM3(value = label, onValueChange = { label = it }, label = "Etiqueta personalizada", emoji = "🏷️")
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = calle, onValueChange = { calle = it }, label = "Calle / Avenida", emoji = "📍")
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(0.4f)) { BentoTextFieldM3(value = numero, onValueChange = { numero = it }, label = "Nº", emoji = "🔢") }
                Box(Modifier.weight(0.6f)) { BentoTextFieldM3(value = localidad, onValueChange = { localidad = it }, label = "Localidad", emoji = "🏙️") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(0.6f)) { BentoTextFieldM3(value = provincia, onValueChange = { provincia = it }, label = "Provincia", emoji = "🗺️") }
                Box(Modifier.weight(0.4f)) { BentoTextFieldM3(value = codigoPostal, onValueChange = { codigoPostal = it }, label = "C.P.", emoji = "📮") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = pais, onValueChange = { pais = it }, label = "País", emoji = "🌐")
        }
    }
}

/**
 * 2. HOJA DE EDICIÓN DE EQUIPO DE TRABAJO (Representative)
 */
@Composable
fun EditRepresentativeSheetContent(
    representative: RepresentativeClient?,
    onSave: (RepresentativeClient) -> Unit,
    onClose: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var nombre by remember { mutableStateOf(representative?.nombre ?: "") }
    var apellido by remember { mutableStateOf(representative?.apellido ?: "") }
    var cargo by remember { mutableStateOf(representative?.cargo ?: "") }
    var photoUrl by remember { mutableStateOf(representative?.photoUrl) }
    
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUrl = uri?.toString()
    }

    BentoBottomSheetContent(
        title = if (representative == null) "Nuevo Miembro" else "Editar Miembro",
        emoji = "👤",
        onClose = onClose,
        showPrimaryButton = true,
        onPrimaryButtonClick = { 
            onSave((representative ?: RepresentativeClient()).copy(
                nombre = nombre, 
                apellido = apellido, 
                cargo = cargo, 
                photoUrl = photoUrl
            )) 
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FOTO DE PERFIL CON EFECTO PREMIUM
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(elevation = 16.dp, shape = CircleShape, ambientColor = GeminiAccent, spotColor = GeminiAccent)
                    .clip(CircleShape)
                    .background(BentoDarkGlassBackground)
                    .background(BentoGlassBrush)
                    .border(1.5.dp, BentoBorderBrush, CircleShape)
                    .clickable { photoLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl, 
                        contentDescription = null, 
                        modifier = Modifier.fillMaxSize(), 
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person, 
                        null, 
                        modifier = Modifier.size(50.dp), 
                        tint = Color.White.copy(alpha = 0.4f)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(32.dp)
                        .background(GeminiAccent, CircleShape)
                        .border(2.dp, BentoDarkGlassBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AddAPhoto, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            BentoTextFieldM3(value = nombre, onValueChange = { nombre = it }, label = "Nombre", emoji = "👤")
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = apellido, onValueChange = { apellido = it }, label = "Apellido", emoji = "👤")
            Spacer(modifier = Modifier.height(12.dp))
            BentoTextFieldM3(value = cargo, onValueChange = { cargo = it }, label = "Cargo / Puesto", emoji = "💼")

            if (onDelete != null) {
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.7f))) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminar de la sucursal", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
