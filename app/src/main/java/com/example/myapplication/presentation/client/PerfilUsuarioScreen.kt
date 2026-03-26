package com.example.myapplication.presentation.client

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.BranchClient
import com.example.myapplication.data.model.CompanyClient
import com.example.myapplication.data.model.RepresentativeClient
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

// =================================================================================
// --- SECCIÓN 0: MODELOS DE ESTADO Y ENUMS ---
// =================================================================================

sealed class EditMode {
    object None : EditMode()
    object Personal : EditMode()
    data class Company(val company: CompanyClient) : EditMode()
    object AddCompany : EditMode()
}

enum class UserProfileTabMode {
    PERSONAL, BUSINESS
}

// =================================================================================
// --- SECCIÓN 1: PANTALLA CONTENEDORA PRINCIPAL ---
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileSharedViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    var editMode by remember { mutableStateOf<EditMode>(EditMode.None) }
    val currentUser = userState

    // --- ESTADOS AUXILIARES PARA PICKERS DE IMÁGENES ---
    var companyIdForPicker by remember { mutableStateOf<String?>(null) }
    var repPickerData by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    // --- LAUNCHERS DE IMÁGENES ---
    val userAvatarPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val user = currentUser ?: return@let
            val updatedUser = user.copy(photoUrl = it.toString())
            viewModel.saveUserProfile(updatedUser)
        }
    }

    val userBannerPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val user = currentUser ?: return@let
            val updatedUser = user.copy(bannerImageUrl = it.toString())
            viewModel.saveUserProfile(updatedUser)
        }
    }

    val companyAvatarPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { newImageUri ->
            val user = currentUser ?: return@let
            companyIdForPicker?.let { id ->
                val updatedCompanies = user.companies.map { company ->
                    if (company.id == id) company.copy(photoUrl = newImageUri.toString()) else company
                }
                viewModel.saveUserProfile(user.copy(companies = updatedCompanies))
            }
            companyIdForPicker = null
        }
    }

    val companyBannerPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { newImageUri ->
            val user = currentUser ?: return@let
            companyIdForPicker?.let { id ->
                val updatedCompanies = user.companies.map { company ->
                    if (company.id == id) company.copy(bannerImageUrl = newImageUri.toString()) else company
                }
                viewModel.saveUserProfile(user.copy(companies = updatedCompanies))
            }
            companyIdForPicker = null
        }
    }

    val repPhotoPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { newImageUri ->
            val user = currentUser ?: return@let
            repPickerData?.let { (companyId, branchId, repId) ->
                val updatedCompanies = user.companies.map { company ->
                    if (company.id == companyId) {
                        val updatedBranches = company.branches.map { branch ->
                            if (branch.id == branchId) {
                                val updatedReps = branch.representatives.map { rep ->
                                    if (rep.id == repId) rep.copy(photoUrl = newImageUri.toString()) else rep
                                }
                                branch.copy(representatives = updatedReps)
                            } else branch
                        }
                        company.copy(branches = updatedBranches)
                    } else company
                }
                viewModel.saveUserProfile(user.copy(companies = updatedCompanies))
            }
            repPickerData = null
        }
    }

    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        PerfilUsuarioContent(
            user = currentUser,
            onNavigateBack = onNavigateBack,
            onLogout = {
                viewModel.logout()
                onLogout()
            },
            onEditRequest = { editMode = it },
            onEditUserBanner = { userBannerPicker.launch("image/*") },
            onEditUserAvatar = { userAvatarPicker.launch("image/*") },
            onEditCompanyBanner = { companyId ->
                companyIdForPicker = companyId
                companyBannerPicker.launch("image/*")
            },
            onEditCompanyAvatar = { companyId ->
                companyIdForPicker = companyId
                companyAvatarPicker.launch("image/*")
            }
        )
    }

    // --- MODALES DE EDICIÓN ---
    when (val currentEditMode = editMode) {
        is EditMode.Personal -> {
            ModalBottomSheet(onDismissRequest = { editMode = EditMode.None }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                EditProfileSheetContent(
                    user = currentUser!!,
                    onSave = { updatedUser ->
                        viewModel.saveUserProfile(updatedUser)
                        editMode = EditMode.None
                    },
                    onClose = { editMode = EditMode.None }
                )
            }
        }
        is EditMode.Company -> {
            ModalBottomSheet(onDismissRequest = { editMode = EditMode.None }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                EditCompanySheetContent(
                    company = currentEditMode.company,
                    onSave = { updatedCompany ->
                        val updatedCompanies = currentUser!!.companies.map { if (it.id == updatedCompany.id) updatedCompany else it }
                        viewModel.saveUserProfile(currentUser.copy(companies = updatedCompanies))
                        editMode = EditMode.None
                    },
                    onClose = { editMode = EditMode.None },
                    onEditRepresentativePhoto = { branchId, repId ->
                        repPickerData = Triple(currentEditMode.company.id, branchId, repId)
                        repPhotoPicker.launch("image/*")
                    }
                )
            }
        }
        is EditMode.AddCompany -> {
            ModalBottomSheet(onDismissRequest = { editMode = EditMode.None }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                EditCompanySheetContent(
                    company = CompanyClient(),
                    isCreating = true,
                    onSave = { newCompany ->
                        val updatedCompanies = currentUser!!.companies + newCompany
                        viewModel.saveUserProfile(currentUser.copy(companies = updatedCompanies))
                        editMode = EditMode.None
                    },
                    onClose = { editMode = EditMode.None },
                    onEditRepresentativePhoto = { _, _ -> }
                )
            }
        }
        EditMode.None -> {}
    }
}

// =================================================================================
// --- SECCIÓN 2: ESTRUCTURA DE LA PANTALLA DE VISUALIZACIÓN ---
// =================================================================================

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PerfilUsuarioContent(
    user: UserEntity,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onEditRequest: (EditMode) -> Unit,
    onEditUserBanner: () -> Unit,
    onEditUserAvatar: () -> Unit,
    onEditCompanyBanner: (String) -> Unit,
    onEditCompanyAvatar: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    var isFabExpanded by remember { mutableStateOf(false) }

    val hasCompanies = user.companies.isNotEmpty()
    var currentTab by remember { mutableStateOf(UserProfileTabMode.PERSONAL) }
    val companyPagerState = rememberPagerState(pageCount = { if (hasCompanies) user.companies.size else 1 })

    // Datos dinámicos para el Header
    val isCompanyMode = currentTab == UserProfileTabMode.BUSINESS && hasCompanies
    val currentCompany = if (isCompanyMode) user.companies[companyPagerState.currentPage] else null

    val currentBannerUrl = currentCompany?.bannerImageUrl ?: user.bannerImageUrl
    val currentPhotoUrl = currentCompany?.photoUrl ?: user.photoUrl
    val currentTitle = currentCompany?.name?.ifEmpty { "Mi Empresa" } ?: "${user.name} ${user.lastName}"
    val currentSubtitle = currentCompany?.razonSocial ?: user.email

    val onEditBannerAction = if (isCompanyMode) { { onEditCompanyBanner(currentCompany!!.id) } } else onEditUserBanner
    val onEditAvatarAction = if (isCompanyMode) { { onEditCompanyAvatar(currentCompany!!.id) } } else onEditUserAvatar

    Scaffold(
        containerColor = Color(0xFFF3F4F6), // Fondo gris moderno
        topBar = {
            Box(modifier = Modifier.padding(top = 40.dp, start = 16.dp).zIndex(20f)) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape).size(40.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            }
        }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 100.dp)
            ) {
                // 1. HEADER FULL WIDTH
                UserProfileHeaderFullWidth(
                    bannerUrl = currentBannerUrl,
                    photoUrl = currentPhotoUrl,
                    title = currentTitle,
                    subtitle = currentSubtitle,
                    onEditBanner = onEditBannerAction,
                    onEditAvatar = onEditAvatarAction
                )

                // 2. TABS ANIMADOS
                AnimatedUserHeaderTabs(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. CONTENIDO DINÁMICO
                if (currentTab == UserProfileTabMode.PERSONAL) {
                    PersonalFullWidthSection(user = user)
                } else if (hasCompanies) {
                    BusinessFullWidthSection(
                        user = user,
                        companyPagerState = companyPagerState
                    )
                } else {
                    // Estado vacío de empresas
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Business, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("Aún no tienes negocios registrados", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { onEditRequest(EditMode.AddCompany) }) {
                            Text("Añadir mi primer negocio")
                        }
                    }
                }
            }

            // --- FAB DE HERRAMIENTAS (Solo a la derecha) ---
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp).zIndex(10f),
                contentAlignment = Alignment.BottomEnd
            ) {
                UserToolsFAB(
                    isExpanded = isFabExpanded,
                    onToggleExpand = { isFabExpanded = !isFabExpanded },
                    onEditCurrent = {
                        isFabExpanded = false
                        if (currentTab == UserProfileTabMode.PERSONAL) onEditRequest(EditMode.Personal)
                        else if (hasCompanies) onEditRequest(EditMode.Company(user.companies[companyPagerState.currentPage]))
                    },
                    onCreateCompany = {
                        isFabExpanded = false
                        onEditRequest(EditMode.AddCompany)
                    },
                    onSettings = {
                        isFabExpanded = false
                        // TODO: Navegar a Configuración de Notificaciones / Premium
                    },
                    onShare = {
                        isFabExpanded = false
                        // TODO: Lógica de compartir
                    },
                    onLogout = {
                        isFabExpanded = false
                        onLogout()
                    }
                )
            }
        }
    }
}

// =================================================================================
// --- ANIMACIÓN DE TABS DEL USUARIO ---
// =================================================================================

@Composable
fun AnimatedUserHeaderTabs(
    currentTab: UserProfileTabMode,
    onTabSelected: (UserProfileTabMode) -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val personalOffset by animateDpAsState(
        targetValue = if (currentTab == UserProfileTabMode.PERSONAL) 0.dp else (-screenWidth / 2 + 50.dp),
        animationSpec = tween(400)
    )
    val businessOffset by animateDpAsState(
        targetValue = if (currentTab == UserProfileTabMode.BUSINESS) 0.dp else (screenWidth / 2 - 50.dp),
        animationSpec = tween(400)
    )

    Box(
        modifier = Modifier.fillMaxWidth().height(64.dp).background(Color.White).border(1.dp, Color(0xFFF3F4F6)).zIndex(10f),
        contentAlignment = Alignment.Center
    ) {
        // Tab Personal
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.offset(x = personalOffset).clickable { onTabSelected(UserProfileTabMode.PERSONAL) }.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(if (currentTab == UserProfileTabMode.PERSONAL) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = if (currentTab == UserProfileTabMode.PERSONAL) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(20.dp))
            }
            AnimatedVisibility(visible = currentTab == UserProfileTabMode.PERSONAL) {
                Text("Datos Personales", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
            }
        }

        // Tab Negocios
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.offset(x = businessOffset).clickable { onTabSelected(UserProfileTabMode.BUSINESS) }.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(if (currentTab == UserProfileTabMode.BUSINESS) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.BusinessCenter, null, tint = if (currentTab == UserProfileTabMode.BUSINESS) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(20.dp))
            }
            AnimatedVisibility(visible = currentTab == UserProfileTabMode.BUSINESS) {
                Text("Mis Negocios", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

// =================================================================================
// --- SECCIONES FULL WIDTH ---
// =================================================================================

@Composable
fun PersonalFullWidthSection(user: UserEntity) {
    Column {
        // --- TARJETA 1: INFO DE CONTACTO ---
        Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 24.dp)) {
            Text("INFORMACIÓN DE CONTACTO", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoRowUser(Icons.Default.PersonOutline, "Nombre Completo", "${user.name} ${user.lastName}")
                InfoRowUser(Icons.Default.Email, "Correo Electrónico", user.email)
                InfoRowUser(Icons.Default.Phone, "Teléfono", user.phoneNumber.ifEmpty { "No especificado" })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- TARJETA 2: DIRECCIONES PERSONALES ---
        Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 24.dp)) {
            Text("DIRECCIONES PERSONALES", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp))

            if (user.personalAddresses.isEmpty()) {
                Text("No tienes direcciones guardadas.", color = Color.Gray, modifier = Modifier.padding(horizontal = 20.dp))
            } else {
                user.personalAddresses.forEachIndexed { index, address ->
                    Row(modifier = Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Home, null, tint = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("${address.calle} ${address.numero}", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Text("${address.localidad}, ${address.provincia}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Text("${address.pais} - CP: ${address.codigoPostal}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    if (index < user.personalAddresses.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF3F4F6))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BusinessFullWidthSection(user: UserEntity, companyPagerState: androidx.compose.foundation.pager.PagerState) {
    Column {
        if (user.companies.size > 1) {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(top = 16.dp), horizontalArrangement = Arrangement.Center) {
                repeat(user.companies.size) { iteration ->
                    val color = if (companyPagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color(0xFFD1D5DB)
                    Box(modifier = Modifier.padding(3.dp).size(6.dp).clip(CircleShape).background(color))
                }
            }
        }

        HorizontalPager(state = companyPagerState, modifier = Modifier.fillMaxWidth()) { page ->
            val company = user.companies[page]

            Column {
                // --- TARJETA 1: DATOS DEL NEGOCIO ---
                Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(top = 16.dp, bottom = 24.dp)) {
                    Text("DATOS LEGALES Y GENERALES", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp))

                    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoRowUser(Icons.Default.Domain, "Razón Social", company.razonSocial.ifEmpty { "No especificado" })
                        InfoRowUser(Icons.Default.Badge, "CUIT", company.cuit.ifEmpty { "No especificado" })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- TARJETA 2: SUCURSALES Y REPRESENTANTES ---
                val branches = company.branches
                if (branches.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(vertical = 16.dp)) {
                        Text("SUCURSALES (${branches.size})", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp))

                        val branchPagerState = rememberPagerState(pageCount = { branches.size })

                        HorizontalPager(
                            state = branchPagerState,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 0.dp)
                        ) { branchPage ->
                            val branch = branches[branchPage]

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                                    .padding(20.dp)
                            ) {
                                Text(branch.name.ifEmpty { "Sucursal" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("${branch.address.calle} ${branch.address.numero}, ${branch.address.localidad}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF3F4F6))

                                Text("PERSONAS A CARGO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

                                if (branch.representatives.isEmpty()) {
                                    Text("Sin representantes asignados.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        branch.representatives.forEach { emp ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF9FAFB)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AsyncImage(
                                                    model = emp.photoUrl,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)),
                                                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text("${emp.nombre} ${emp.apellido}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                    Text(emp.cargo, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (branches.size > 1) {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Center) {
                                repeat(branches.size) { iteration ->
                                    val color = if (branchPagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color(0xFFD1D5DB)
                                    Box(modifier = Modifier.padding(3.dp).size(6.dp).clip(CircleShape).background(color))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =================================================================================
// --- COMPONENTES VISUALES BASE Y FAB ---
// =================================================================================

@Composable
fun UserProfileHeaderFullWidth(
    bannerUrl: String?,
    photoUrl: String?,
    title: String,
    subtitle: String,
    onEditBanner: () -> Unit,
    onEditAvatar: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        AsyncImage(
            model = bannerUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(id = R.drawable.ic_launcher_background) // Usa tu placeholder aquí
        )

        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))))

        // Botón de editar Banner (Arriba derecha)
        IconButton(
            onClick = onEditBanner,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(Icons.Default.Edit, "Editar banner", tint = Color.White)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 24.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(CircleShape).border(3.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )
                // Botón editar foto de perfil superpuesto
                IconButton(
                    onClick = onEditAvatar,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(28.dp).background(MaterialTheme.colorScheme.surface, CircleShape).border(2.dp, Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Edit, "Editar foto", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }

            Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun InfoRowUser(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, modifier = Modifier.size(20.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color.DarkGray)
        }
    }
}

/**
 * FAB Especial para el usuario (Solo herramientas, sin lado izquierdo)
 */
@Composable
fun UserToolsFAB(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditCurrent: () -> Unit,
    onCreateCompany: () -> Unit,
    onSettings: () -> Unit,
    onShare: () -> Unit,
    onLogout: () -> Unit
) {
    val fabIconRotation by animateFloatAsState(targetValue = if (isExpanded) 45f else 0f, label = "fabRotation")

    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 50 })
        ) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFabTool("Compartir Perfil", Icons.Default.Share, onClick = onShare)
                SmallFabTool("Ajustes / Notificaciones", Icons.Default.Settings, onClick = onSettings)
                SmallFabTool("Añadir Empresa", Icons.Default.AddBusiness, onClick = onCreateCompany)
                SmallFabTool("Editar Información", Icons.Default.Edit, onClick = onEditCurrent)
                SmallFabTool("Cerrar Sesión", Icons.Default.Logout, onClick = onLogout, color = MaterialTheme.colorScheme.error)
            }
        }

        Surface(
            onClick = onToggleExpand,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(if (isExpanded) Icons.Default.Close else Icons.Default.Build, "Herramientas", tint = Color.White, modifier = Modifier.rotate(fabIconRotation))
            }
        }
    }
}

@Composable
fun SmallFabTool(text: String, icon: ImageVector, onClick: () -> Unit, color: Color = MaterialTheme.colorScheme.primary) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
            Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Surface(onClick = onClick, modifier = Modifier.size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(20.dp), tint = color) }
        }
    }
}

// =================================================================================
// --- SHEETS DE EDICIÓN (FORMULARIOS MANTENIDOS INTACTOS Y PULIDOS) ---
// =================================================================================

@Composable
fun EditProfileSheetContent(user: UserEntity, onSave: (UserEntity) -> Unit, onClose: () -> Unit) {
    var name by remember { mutableStateOf(user.name) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var email by remember { mutableStateOf(user.email) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }
    val personalAddresses = remember { user.personalAddresses.toMutableStateList() }

    Column(modifier = Modifier.fillMaxHeight(0.9f).background(Color(0xFFF9FAFB))) {
        Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Cerrar") }
            Text("Editar Perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(onClick = {
                onSave(user.copy(name = name, lastName = lastName, email = email, phoneNumber = phoneNumber, personalAddresses = personalAddresses.toList()))
            }) { Text("Guardar") }
        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                SectionTitle("Datos Personales", Icons.Default.Person)
                ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    SectionTitle("Mis Direcciones", Icons.Default.Home)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { personalAddresses.add(AddressClient(calle = "Nueva Dirección")) }) { Icon(Icons.Default.AddCircle, "Añadir", tint = MaterialTheme.colorScheme.primary) }
                }
            }
            itemsIndexed(personalAddresses) { index, address ->
                EditableAddressItem(address = address, onUpdate = { updated -> personalAddresses[index] = updated }, onDelete = { personalAddresses.removeAt(index) })
            }
        }
    }
}

@Composable
fun EditCompanySheetContent(company: CompanyClient, onSave: (CompanyClient) -> Unit, onClose: () -> Unit, onEditRepresentativePhoto: (String, String) -> Unit, isCreating: Boolean = false) {
    var tempCompany by remember { mutableStateOf(company) }
    val branches = remember { tempCompany.branches.toMutableStateList() }

    Column(modifier = Modifier.fillMaxHeight(0.9f).background(Color(0xFFF9FAFB))) {
        Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Cerrar") }
            Text(if (isCreating) "Crear Negocio" else "Editar Negocio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(onClick = { onSave(tempCompany.copy(branches = branches.toList())) }) { Text("Guardar") }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                SectionTitle("Datos del Negocio", Icons.Default.Business)
                ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = tempCompany.name, onValueChange = { tempCompany = tempCompany.copy(name = it) }, label = { Text("Nombre Comercial") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = tempCompany.razonSocial, onValueChange = { tempCompany = tempCompany.copy(razonSocial = it) }, label = { Text("Razón Social") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = tempCompany.cuit, onValueChange = { tempCompany = tempCompany.copy(cuit = it) }, label = { Text("CUIT") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    SectionTitle("Sucursales", Icons.Default.Store)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { branches.add(BranchClient(name = "Nueva Sucursal")) }) { Icon(Icons.Default.AddCircle, "Añadir", tint = MaterialTheme.colorScheme.primary) }
                }
            }
            itemsIndexed(branches) { index, branch ->
                EditableBranchItem(branch = branch, onUpdate = { updated -> branches[index] = updated }, onDelete = { branches.removeAt(index) }, onEditPhoto = { repId -> onEditRepresentativePhoto(branch.id, repId) })
            }
        }
    }
}

@Composable
fun EditableAddressItem(address: AddressClient, onUpdate: (AddressClient) -> Unit, onDelete: () -> Unit) {
    var tempAddress by remember { mutableStateOf(address) }
    LaunchedEffect(tempAddress) { onUpdate(tempAddress) }
    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = tempAddress.calle, onValueChange = { tempAddress = tempAddress.copy(calle = it) }, label = { Text("Calle") }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(value = tempAddress.numero, onValueChange = { tempAddress = tempAddress.copy(numero = it) }, label = { Text("Nº") }, modifier = Modifier.width(80.dp))
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = tempAddress.localidad, onValueChange = { tempAddress = tempAddress.copy(localidad = it) }, label = { Text("Localidad") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = tempAddress.provincia, onValueChange = { tempAddress = tempAddress.copy(provincia = it) }, label = { Text("Provincia") }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = tempAddress.pais, onValueChange = { tempAddress = tempAddress.copy(pais = it) }, label = { Text("País") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = tempAddress.codigoPostal, onValueChange = { tempAddress = tempAddress.copy(codigoPostal = it) }, label = { Text("CP") }, modifier = Modifier.width(100.dp))
            }
        }
    }
}

@Composable
fun EditableBranchItem(branch: BranchClient, onUpdate: (BranchClient) -> Unit, onDelete: () -> Unit, onEditPhoto: (String) -> Unit) {
    var tempBranch by remember { mutableStateOf(branch) }
    val representatives = remember { tempBranch.representatives.toMutableStateList() }
    LaunchedEffect(tempBranch, representatives) { onUpdate(tempBranch.copy(representatives = representatives.toList())) }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = tempBranch.name, onValueChange = { tempBranch = tempBranch.copy(name = it) }, label = { Text("Nombre Sucursal (Ej: Centro)") }, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
            }

            Text("Dirección de la Sucursal", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp), color = Color.Gray)
            EditableAddressItem(address = tempBranch.address, onUpdate = { tempBranch = tempBranch.copy(address = it) }, onDelete = {})

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Personas a Cargo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { representatives.add(RepresentativeClient()) }) { Icon(Icons.Default.AddCircleOutline, "Añadir Persona", tint = MaterialTheme.colorScheme.primary) }
            }
            representatives.forEachIndexed { index, rep ->
                EditableRepresentativeItem(representative = rep, onUpdate = { updated -> representatives[index] = updated }, onDelete = { representatives.removeAt(index) }, onEditPhoto = { onEditPhoto(rep.id) })
            }
        }
    }
}

@Composable
fun EditableRepresentativeItem(representative: RepresentativeClient, onUpdate: (RepresentativeClient) -> Unit, onDelete: () -> Unit, onEditPhoto: () -> Unit) {
    var tempRep by remember { mutableStateOf(representative) }
    LaunchedEffect(tempRep) { onUpdate(tempRep) }

    Card(modifier = Modifier.padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)), border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.clickable { onEditPhoto() }) {
                AsyncImage(model = tempRep.photoUrl, contentDescription = "Foto Representante", modifier = Modifier.size(50.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape), contentScale = ContentScale.Crop, error = painterResource(id = R.drawable.ic_launcher_foreground))
                Icon(Icons.Default.Edit, null, modifier = Modifier.align(Alignment.BottomEnd).size(18.dp).background(Color.White, CircleShape).border(1.dp, Color.LightGray, CircleShape), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = tempRep.nombre, onValueChange = { tempRep = tempRep.copy(nombre = it)}, label = {Text("Nombre")}, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tempRep.apellido, onValueChange = { tempRep = tempRep.copy(apellido = it)}, label = {Text("Apellido")}, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tempRep.cargo, onValueChange = { tempRep = tempRep.copy(cargo = it)}, label = {Text("Cargo")}, modifier = Modifier.fillMaxWidth())
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Close, "Eliminar", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray)
    }
}

@Preview(showBackground = true, name = "Perfil de Usuario")
@Composable
fun GoogleUserPreview() {
    val googleUser = UserEntity("google_123", "ana.lopez@gmail.com", "Ana López", "Ana", "López", "+11234567890", bannerImageUrl = "https://picsum.photos/seed/anabanner/800/600", photoUrl = "https://picsum.photos/seed/anita/200/200", companies = mutableListOf(), personalAddresses = mutableListOf(AddressClient(calle = "Av. Siempreviva", numero = "742", localidad = "Springfield", provincia = "Estado Desconocido")), isVerified = true, isSubscribed = false, hasCompanyProfile = false, isFavorite = false, isOnline = true, rating = 0f, matricula = null, titulo = null, createdAt = System.currentTimeMillis())
    MyApplicationTheme {
        PerfilUsuarioContent(
            user = googleUser,
            onNavigateBack = {},
            onLogout = {},
            onEditRequest = {},
            onEditUserBanner = {},
            onEditUserAvatar = {},
            onEditCompanyBanner = {},
            onEditCompanyAvatar = {}
        )
    }
}



/**package com.example.myapplication.presentation.client

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.BranchClient
import com.example.myapplication.data.model.CompanyClient
import com.example.myapplication.data.model.RepresentativeClient
//import com.example.myapplication.presentation.components.SmallFabTool
import com.example.myapplication.presentation.components.geminiGradientEffect
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

// =================================================================================
// --- SECCIÓN 0: MODELOS DE ESTADO ---
// =================================================================================

sealed class EditMode {
    object None : EditMode()
    object Personal : EditMode()
    data class Company(val company: CompanyClient) : EditMode()
    object AddCompany : EditMode()
}

// =================================================================================
// --- SECCIÓN 1: PANTALLA CONTENEDORA PRINCIPAL ---
// =================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileSharedViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    var editMode by remember { mutableStateOf<EditMode>(EditMode.None) }
    val currentUser = userState
    
    // --- ESTADOS AUXILIARES PARA PICKERS DE IMÁGENES ---
    // IDs o referencias temporales para saber a qué entidad (Empresa o Representante) asignar la imagen cargada
    var companyIdForPicker by remember { mutableStateOf<String?>(null) }
    
    // Para representantes: Guardamos [ID Empresa, ID Sucursal, ID Representante]
    var repPickerData by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    // --- LAUNCHERS DE IMÁGENES ---

    // 1. Avatar del Usuario
    val userAvatarPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val user = currentUser ?: return@let
            val updatedUser = user.copy(photoUrl = it.toString())
            viewModel.saveUserProfile(updatedUser)
        }
    }
    // 2. Banner del Usuario
    val userBannerPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val user = currentUser ?: return@let
            val updatedUser = user.copy(bannerImageUrl = it.toString())
            viewModel.saveUserProfile(updatedUser)
        }
    }
    
    // 3. Avatar de Empresa
    val companyAvatarPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { newImageUri ->
            val user = currentUser ?: return@let
            companyIdForPicker?.let { id ->
                val updatedCompanies = user.companies.map { company ->
                    if (company.id == id) company.copy(photoUrl = newImageUri.toString()) else company
                }
                viewModel.saveUserProfile(user.copy(companies = updatedCompanies))
            }
            companyIdForPicker = null
        }
    }

    // 4. Banner de Empresa
    val companyBannerPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { newImageUri ->
            val user = currentUser ?: return@let
            companyIdForPicker?.let { id ->
                val updatedCompanies = user.companies.map { company ->
                    if (company.id == id) company.copy(bannerImageUrl = newImageUri.toString()) else company
                }
                viewModel.saveUserProfile(user.copy(companies = updatedCompanies))
            }
            companyIdForPicker = null
        }
    }

    // 5. Foto de Representante (Persona a cargo)
    // Este launcher es más complejo porque necesitamos navegar profundo en la estructura:
    // User -> Companies -> Branch -> Representatives
    val repPhotoPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { newImageUri ->
            val user = currentUser ?: return@let
            repPickerData?.let { (companyId, branchId, repId) ->
                val updatedCompanies = user.companies.map { company ->
                    if (company.id == companyId) {
                        // Encontramos la empresa
                        val updatedBranches = company.branches.map { branch ->
                            if (branch.id == branchId) {
                                // Encontramos la sucursal
                                val updatedReps = branch.representatives.map { rep ->
                                    if (rep.id == repId) {
                                        // Encontramos al representante -> Actualizamos foto
                                        rep.copy(photoUrl = newImageUri.toString())
                                    } else rep
                                }
                                branch.copy(representatives = updatedReps)
                            } else branch
                        }
                        company.copy(branches = updatedBranches)
                    } else company
                }
                
                // Guardamos todo el árbol actualizado
                viewModel.saveUserProfile(user.copy(companies = updatedCompanies))
            }
            repPickerData = null // Reset
        }
    }

    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        PerfilUsuarioContent(
            user = currentUser,
            onNavigateBack = onNavigateBack,
            onLogout = {
                viewModel.logout()
                onLogout()
            },
            onEditRequest = { editMode = it },
            onEditUserBanner = { userBannerPicker.launch("image/*") },
            onEditUserAvatar = { userAvatarPicker.launch("image/*") },
            onEditCompanyBanner = { companyId ->
                companyIdForPicker = companyId
                companyBannerPicker.launch("image/*")
            },
            onEditCompanyAvatar = { companyId ->
                companyIdForPicker = companyId
                companyAvatarPicker.launch("image/*")
            }
        )
    }

    // --- MODALES DE EDICIÓN ---
    when (val currentEditMode = editMode) {
        is EditMode.Personal -> {
            ModalBottomSheet(onDismissRequest = { editMode = EditMode.None }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                EditProfileSheetContent(
                    user = currentUser!!,
                    onSave = { updatedUser ->
                        viewModel.saveUserProfile(updatedUser)
                        editMode = EditMode.None
                    },
                    onClose = { editMode = EditMode.None }
                )
            }
        }
        is EditMode.Company -> {
             ModalBottomSheet(onDismissRequest = { editMode = EditMode.None }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                EditCompanySheetContent(
                    company = currentEditMode.company,
                    onSave = { updatedCompany ->
                        val updatedCompanies = currentUser!!.companies.map { if (it.id == updatedCompany.id) updatedCompany else it }
                        viewModel.saveUserProfile(currentUser.copy(companies = updatedCompanies))
                        editMode = EditMode.None
                    },
                    onClose = { editMode = EditMode.None },
                    // Pasamos la función para iniciar el picker de foto de representante
                    onEditRepresentativePhoto = { branchId, repId ->
                        repPickerData = Triple(currentEditMode.company.id, branchId, repId)
                        repPhotoPicker.launch("image/*")
                    }
                )
            }
        }
        is EditMode.AddCompany -> {
            ModalBottomSheet(onDismissRequest = { editMode = EditMode.None }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                // Al crear, la ID de la empresa es nueva y autogenerada dentro del objeto
                // Pero necesitamos capturarla si quisiéramos editar fotos inmediatamente.
                // Por simplicidad, primero se crea la empresa sin fotos de representantes, y luego se edita.
                EditCompanySheetContent(
                    company = CompanyClient(),
                    isCreating = true,
                    onSave = { newCompany ->
                        val updatedCompanies = currentUser!!.companies + newCompany
                        viewModel.saveUserProfile(currentUser.copy(companies = updatedCompanies))
                        editMode = EditMode.None
                    },
                    onClose = { editMode = EditMode.None },
                     onEditRepresentativePhoto = { _, _ -> 
                         // No soportado en creación directa por complejidad de IDs temporales.
                         // El usuario debe guardar primero y luego editar para añadir fotos de representantes.
                     }
                )
            }
        }
        EditMode.None -> {}
    }
}

// =================================================================================
// --- SECCIÓN 2: ESTRUCTURA DE LA PANTALLA DE VISUALIZACIÓN ---
// =================================================================================

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioContent(
    user: UserEntity,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onEditRequest: (EditMode) -> Unit,
    onEditUserBanner: () -> Unit,
    onEditUserAvatar: () -> Unit,
    onEditCompanyBanner: (String) -> Unit,
    onEditCompanyAvatar: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    var isFabExpanded by remember { mutableStateOf(false) }
    val companyPagerState = rememberPagerState(pageCount = { user.companies.size.coerceAtLeast(1) })

    // --- LÓGICA DE HEADER DINÁMICO ---
    val isCompanyTab = pagerState.currentPage == 1
    val hasCompanies = user.companies.isNotEmpty()
    val showCompanyHeader = isCompanyTab && hasCompanies

    // Datos dinámicos según lo seleccionado
    val currentBannerUrl = if (showCompanyHeader) user.companies[companyPagerState.currentPage.coerceIn(0, user.companies.lastIndex)].bannerImageUrl else user.bannerImageUrl
    val currentPhotoUrl = if (showCompanyHeader) user.companies[companyPagerState.currentPage.coerceIn(0, user.companies.lastIndex)].photoUrl else user.photoUrl
    val currentTitle = if (showCompanyHeader) user.companies[companyPagerState.currentPage.coerceIn(0, user.companies.lastIndex)].name else "${user.name} ${user.lastName}"
    val currentSubtitle = if (showCompanyHeader) user.companies[companyPagerState.currentPage.coerceIn(0, user.companies.lastIndex)].razonSocial else user.email

    val currentOnEditBanner = if (showCompanyHeader) {
        { onEditCompanyBanner(user.companies[companyPagerState.currentPage.coerceIn(0, user.companies.lastIndex)].id) }
    } else onEditUserBanner

    val currentOnEditAvatar = if (showCompanyHeader) {
        { onEditCompanyAvatar(user.companies[companyPagerState.currentPage.coerceIn(0, user.companies.lastIndex)].id) }
    } else onEditUserAvatar


    Scaffold(
        floatingActionButton = {
            SimplifiedGeminiFAB(
                isExpanded = isFabExpanded,
                onToggleExpand = { isFabExpanded = !isFabExpanded },
                onEdit = {
                    isFabExpanded = false
                    if (pagerState.currentPage == 0) onEditRequest(EditMode.Personal)
                    else if (hasCompanies) onEditRequest(EditMode.Company(user.companies[companyPagerState.currentPage.coerceIn(0, user.companies.lastIndex)]))
                },
                onCreateCompany = {
                    isFabExpanded = false
                    onEditRequest(EditMode.AddCompany)
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // HEADER DINÁMICO
            GenericProfileHeader(
                bannerUrl = currentBannerUrl,
                photoUrl = currentPhotoUrl,
                title = currentTitle,
                subtitle = currentSubtitle,
                onEditBanner = currentOnEditBanner,
                onEditAvatar = currentOnEditAvatar
            )

            val titles = listOf("Datos Personales", "Mis Negocios")
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(text = title) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> DatosPersonalesPage(user = user)
                    1 -> MisNegociosPage(
                        user = user,
                        companyPagerState = companyPagerState
                    )
                }
            }
        }
    }
}

// =================================================================================
// --- PÁGINA: DATOS PERSONALES ---
// =================================================================================

@Composable
fun DatosPersonalesPage(user: UserEntity) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            ArchiveroSectionUser("Información de Contacto", MaterialTheme.colorScheme.primaryContainer) {
                InfoRowUser(Icons.Default.Person, "Nombre", "${user.name} ${user.lastName}")
                InfoRowUser(Icons.Default.Email, "Email", user.email)
                InfoRowUser(Icons.Default.Phone, "Teléfono", user.phoneNumber)
            }
        }
        item {
            ArchiveroSectionUser("Direcciones Personales", MaterialTheme.colorScheme.secondaryContainer) {
                if (user.personalAddresses.isEmpty()) Text("No tienes direcciones guardadas.")
                else user.personalAddresses.forEach { address -> 
                    // Mostramos la dirección completa según estructura solicitada
                    Column(Modifier.padding(vertical = 4.dp)) {
                        InfoRowUser(Icons.Default.LocationOn, "Calle y Número", "${address.calle} ${address.numero}")
                        Text("${address.localidad}, ${address.provincia}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 32.dp))
                        Text("${address.pais} - CP: ${address.codigoPostal}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 32.dp))
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

// =================================================================================
// --- PÁGINA: MIS NEGOCIOS ---
// =================================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MisNegociosPage(
    user: UserEntity,
    companyPagerState: PagerState
) {
    if (user.companies.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Aún no tienes negocios registrados.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = companyPagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp),
                pageSpacing = 16.dp
            ) { page ->
                val company = user.companies[page]
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        ArchiveroSectionUser("Datos del Negocio", MaterialTheme.colorScheme.tertiaryContainer) {
                            InfoRowUser(Icons.Default.Business, "Nombre", company.name)
                            InfoRowUser(Icons.Default.Domain, "Razón Social", company.razonSocial)
                            InfoRowUser(Icons.Default.Badge, "CUIT", company.cuit)
                        }
                    }
                    item {
                        ArchiveroSectionUser("Sucursales", MaterialTheme.colorScheme.surfaceVariant) {
                            company.branches.forEach { branch ->
                                ExpandableBranchItem(branch = branch)
                                if (company.branches.last() != branch) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
            // Indicador de Paginación
            if (user.companies.size > 1) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                    repeat(user.companies.size) { iteration ->
                        val color = if (companyPagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                        Box(modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(10.dp))
                    }
                }
            }
        }
    }
}

// =================================================================================
// --- COMPONENTES VISUALES ---
// =================================================================================

@Composable
fun GenericProfileHeader(
    bannerUrl: String?,
    photoUrl: String?,
    title: String,
    subtitle: String,
    onEditBanner: () -> Unit,
    onEditAvatar: () -> Unit
) {
     Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        Image(painter = rememberAsyncImagePainter(model = bannerUrl ?: R.drawable.myeasteregg), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))))
        IconButton(onClick = onEditBanner, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)) { Icon(Icons.Default.Edit, "Editar banner", tint = Color.White) }
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.Bottom) {
            Box {
                Image(painter = rememberAsyncImagePainter(model = photoUrl ?: R.drawable.iconapp), contentDescription = null, modifier = Modifier.size(80.dp).clip(CircleShape).border(3.dp, Color.White, CircleShape), contentScale = ContentScale.Crop)
                IconButton(onClick = onEditAvatar, modifier = Modifier.align(Alignment.BottomEnd).size(30.dp).background(MaterialTheme.colorScheme.surface, CircleShape).border(2.dp, Color.White, CircleShape)) { Icon(Icons.Default.Edit, "Editar foto de perfil", modifier = Modifier.size(16.dp)) }
            }
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                if (subtitle.isNotEmpty()) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
fun ArchiveroSectionUser(title: String, color: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Box(modifier = Modifier.size(width = 4.dp, height = 18.dp).background(color, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
        }
    }
}

@Composable
fun InfoRowUser(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, modifier = Modifier.size(20.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ExpandableBranchItem(branch: BranchClient) {
    var isExpanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().animateContentSize().clickable { isExpanded = !isExpanded }.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Store, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(branch.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
        }
        if (isExpanded) {
            Spacer(Modifier.height(12.dp))
            // Dirección
            InfoRowUser(Icons.Default.Place, "Dirección", "${branch.address.calle} ${branch.address.numero}")
            Text(
                "${branch.address.localidad}, ${branch.address.provincia}, ${branch.address.pais}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 32.dp, bottom = 4.dp)
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Representantes (Persona a Cargo)
            Text("Personas a Cargo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (branch.representatives.isEmpty()) {
                Text("No hay representantes asignados.", style = MaterialTheme.typography.bodySmall)
            } else {
                branch.representatives.forEach { rep ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                         // Foto del representante
                         Image(
                            painter = rememberAsyncImagePainter(model = rep.photoUrl ?: R.drawable.iconapp),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("${rep.nombre} ${rep.apellido}", fontWeight = FontWeight.Bold)
                            Text(rep.cargo, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// =================================================================================
// --- SHEETS DE EDICIÓN (FORMULARIOS) ---
// =================================================================================

@Composable
fun EditProfileSheetContent(user: UserEntity, onSave: (UserEntity) -> Unit, onClose: () -> Unit) {
    var name by remember { mutableStateOf(user.name) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var email by remember { mutableStateOf(user.email) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }
    // Convertimos a mutable para editar, pero al guardar debe ser inmutable
    val personalAddresses = remember { user.personalAddresses.toMutableStateList() }

    Column(modifier = Modifier.fillMaxHeight(0.9f)) {
        // Toolbar del Sheet
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Cerrar") }
            Text("Editar Perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(onClick = { 
                // CRUCIAL: Convertir la lista mutable a una lista inmutable (List) antes de guardar
                onSave(user.copy(
                    name = name, 
                    lastName = lastName, 
                    email = email, 
                    phoneNumber = phoneNumber, 
                    personalAddresses = personalAddresses.toList() 
                )) 
            }) { Text("Guardar") }
        }
        
        // Formulario
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                SectionTitle("Datos Personales", Icons.Default.Person)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellido") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("Mis Direcciones", Icons.Default.Home)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { personalAddresses.add(AddressClient(calle = "Nueva Dirección")) }) { Icon(Icons.Default.AddCircle, "Añadir Dirección", tint = MaterialTheme.colorScheme.primary) }
                }
            }
            itemsIndexed(personalAddresses) { index, address ->
                EditableAddressItem(address = address, onUpdate = { updated -> personalAddresses[index] = updated }, onDelete = { personalAddresses.removeAt(index) })
            }
        }
    }
}

@Composable
fun EditCompanySheetContent(
    company: CompanyClient,
    onSave: (CompanyClient) -> Unit,
    onClose: () -> Unit,
    onEditRepresentativePhoto: (String, String) -> Unit, // (BranchId, RepId)
    isCreating: Boolean = false
) {
    var tempCompany by remember { mutableStateOf(company) }
    val branches = remember { tempCompany.branches.toMutableStateList() }

    Column(modifier = Modifier.fillMaxHeight(0.9f)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Cerrar") }
            Text(if (isCreating) "Crear Negocio" else "Editar Negocio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(onClick = { 
                onSave(tempCompany.copy(branches = branches.toList())) 
            }) { Text("Guardar") }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                SectionTitle("Datos del Negocio", Icons.Default.Business)
                OutlinedTextField(value = tempCompany.name, onValueChange = { tempCompany = tempCompany.copy(name = it) }, label = { Text("Nombre de la Empresa") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tempCompany.razonSocial, onValueChange = { tempCompany = tempCompany.copy(razonSocial = it) }, label = { Text("Razón Social") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tempCompany.cuit, onValueChange = { tempCompany = tempCompany.copy(cuit = it) }, label = { Text("CUIT") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("Sucursales", Icons.Default.Store)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { branches.add(BranchClient(name = "Nueva Sucursal")) }) { Icon(Icons.Default.AddCircle, "Añadir Sucursal", tint = MaterialTheme.colorScheme.primary) }
                }
            }
            itemsIndexed(branches) { index, branch ->
                EditableBranchItem(
                    branch = branch, 
                    onUpdate = { updated -> branches[index] = updated }, 
                    onDelete = { branches.removeAt(index) },
                    onEditPhoto = { repId -> onEditRepresentativePhoto(branch.id, repId) }
                )
            }
        }
    }
}

@Composable
fun EditableAddressItem(address: AddressClient, onUpdate: (AddressClient) -> Unit, onDelete: () -> Unit) {
    var tempAddress by remember { mutableStateOf(address) }
    LaunchedEffect(tempAddress) { onUpdate(tempAddress) }
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Calle y Número
                OutlinedTextField(value = tempAddress.calle, onValueChange = { tempAddress = tempAddress.copy(calle = it) }, label = { Text("Calle") }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(value = tempAddress.numero, onValueChange = { tempAddress = tempAddress.copy(numero = it) }, label = { Text("Número") }, modifier = Modifier.width(80.dp))
                
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
            }
            
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = tempAddress.localidad, onValueChange = { tempAddress = tempAddress.copy(localidad = it) }, label = { Text("Localidad") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = tempAddress.provincia, onValueChange = { tempAddress = tempAddress.copy(provincia = it) }, label = { Text("Provincia") }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = tempAddress.pais, onValueChange = { tempAddress = tempAddress.copy(pais = it) }, label = { Text("País") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = tempAddress.codigoPostal, onValueChange = { tempAddress = tempAddress.copy(codigoPostal = it) }, label = { Text("CP") }, modifier = Modifier.width(100.dp))
            }
        }
    }
}

@Composable
fun EditableBranchItem(
    branch: BranchClient, 
    onUpdate: (BranchClient) -> Unit, 
    onDelete: () -> Unit,
    onEditPhoto: (String) -> Unit // (RepId)
) {
    var tempBranch by remember { mutableStateOf(branch) }
    val representatives = remember { tempBranch.representatives.toMutableStateList() }
    
    LaunchedEffect(tempBranch, representatives) { onUpdate(tempBranch.copy(representatives = representatives.toList())) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Descripción de la sucursal (Nombre)
                OutlinedTextField(value = tempBranch.name, onValueChange = { tempBranch = tempBranch.copy(name = it) }, label = { Text("Nombre Sucursal (Ej: Centro)") }, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error) }
            }
            
            Text("Dirección de la Sucursal", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
            EditableAddressItem(address = tempBranch.address, onUpdate = { tempBranch = tempBranch.copy(address = it) }, onDelete = {})
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            
            // Personas a Cargo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Personas a Cargo", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { representatives.add(RepresentativeClient()) }) { Icon(Icons.Default.Add, "Añadir Persona") }
            }
            representatives.forEachIndexed { index, rep ->
                EditableRepresentativeItem(
                    representative = rep, 
                    onUpdate = { updated -> representatives[index] = updated }, 
                    onDelete = { representatives.removeAt(index) },
                    onEditPhoto = { onEditPhoto(rep.id) }
                )
            }
        }
    }
}

@Composable
fun EditableRepresentativeItem(
    representative: RepresentativeClient, 
    onUpdate: (RepresentativeClient) -> Unit, 
    onDelete: () -> Unit,
    onEditPhoto: () -> Unit
) {
    var tempRep by remember { mutableStateOf(representative) }
    LaunchedEffect(tempRep) { onUpdate(tempRep) }
    
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.Top) {
            // Foto
            Box(modifier = Modifier.clickable { onEditPhoto() }) {
                Image(
                    painter = rememberAsyncImagePainter(model = tempRep.photoUrl ?: R.drawable.iconapp),
                    contentDescription = "Foto Representante",
                    modifier = Modifier.size(50.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Icon(Icons.Default.Edit, null, modifier = Modifier.align(Alignment.BottomEnd).size(16.dp).background(Color.White, CircleShape))
            }
            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = tempRep.nombre, onValueChange = { tempRep = tempRep.copy(nombre = it)}, label = {Text("Nombre")}, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tempRep.apellido, onValueChange = { tempRep = tempRep.copy(apellido = it)}, label = {Text("Apellido")}, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tempRep.cargo, onValueChange = { tempRep = tempRep.copy(cargo = it)}, label = {Text("Cargo")}, modifier = Modifier.fillMaxWidth())
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Close, "Eliminar") }
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SimplifiedGeminiFAB(isExpanded: Boolean, onToggleExpand: () -> Unit, onEdit: () -> Unit, onCreateCompany: () -> Unit) {
    val rainbowBrush = geminiGradientEffect()
    val fabIconRotation by animateFloatAsState(targetValue = if (isExpanded) 45f else 0f, label = "fabRotation")
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        AnimatedVisibility(visible = isExpanded, enter = fadeIn() + slideInHorizontally(), exit = fadeOut() + slideOutHorizontally()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                //SmallFabTool("Negocio", Icons.Default.AddBusiness, onClick = onCreateCompany)
                //SmallFabTool("Editar", Icons.Default.Edit, onClick = onEdit)
            }
        }
        Surface(onClick = onToggleExpand, modifier = Modifier.size(56.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface, border = BorderStroke(2.5.dp, rainbowBrush), shadowElevation = 12.dp) {
            Box(contentAlignment = Alignment.Center) { Icon(if (isExpanded) Icons.Default.Close else Icons.Default.Add, "Acciones", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.rotate(fabIconRotation)) }
        }
    }
}
**/