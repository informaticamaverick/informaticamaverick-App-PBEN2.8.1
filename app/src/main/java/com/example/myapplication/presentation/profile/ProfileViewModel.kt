package com.example.myapplication.presentation.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.UserProfile
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

@HiltViewModel

open class ProfileViewModel @Inject constructor() : ViewModel() {


    private val _uiState = MutableStateFlow(ProfileUiState())

    open val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    //Linea de código modificada
    private  var isEmailChangePending = false


    private val firestore by lazy { Firebase.firestore }

    private val auth by lazy { Firebase.auth }

    open fun onDisplayNameChange(displayName: String) {
        _uiState.update { it.copy(displayName = displayName, error = null) }
    }

    open fun onPhoneNumberChange(phone: String) {

        _uiState.update { it.copy(phoneNumber = phone, error = null) }

    }


    fun onPasswordChange(password: String) {

        _uiState.update { it.copy(password = password, error = null) }

    }


    fun onAddressChange(address: String) {

        _uiState.update { it.copy(address = address, error = null) }

    }

    fun onAddressHomeChange(addressHome: String) {
        _uiState.update { it.copy(addressHome = addressHome, error = null) }
    }

    fun onAddressWorkChange(addressWork: String) {
        _uiState.update { it.copy(addressWork = addressWork, error = null) }
    }


    fun onCityChange(city: String) {

        _uiState.update { it.copy(city = city, error = null) }

    }


    fun onStateChange(state: String) {

        _uiState.update { it.copy(state = state, error = null) }

    }


    fun onZipCodeChange(zipCode: String) {

        _uiState.update { it.copy(zipCode = zipCode, error = null) }

    }

    // Funciones para dirección de casa
    fun onCityHomeChange(cityHome: String) {
        _uiState.update { it.copy(cityHome = cityHome, error = null) }
    }

    fun onStateHomeChange(stateHome: String) {
        _uiState.update { it.copy(stateHome = stateHome, error = null) }
    }

    fun onZipCodeHomeChange(zipCodeHome: String) {
        _uiState.update { it.copy(zipCodeHome = zipCodeHome, error = null) }
    }

    // Funciones para dirección de trabajo
    fun onCityWorkChange(cityWork: String) {
        _uiState.update { it.copy(cityWork = cityWork, error = null) }
    }

    fun onStateWorkChange(stateWork: String) {
        _uiState.update { it.copy(stateWork = stateWork, error = null) }
    }

    fun onZipCodeWorkChange(zipCodeWork: String) {
        _uiState.update { it.copy(zipCodeWork = zipCodeWork, error = null) }
    }

    //********************************************************************************************************************
//             DATOS DE PRUEBA PERFIL USUARIO DATOS EMPRESA APP CLIENTE
//********************************************************************************************************************

    fun onIsEmpresaChange(isEmpresa: Boolean) {
        _uiState.update { it.copy(isEmpresa = isEmpresa, error = null) }
    }

    open fun onNameComercialEmpresaChange(nameComercialEmpresa: String) {
        _uiState.update { it.copy(nameComercialEmpresa = nameComercialEmpresa, error = null) }
    }

    open fun onNameRazonSocialEmpresaChange(nameRazonSocialEmpresa: String) {
        _uiState.update { it.copy(nameRazonSocialEmpresa = nameRazonSocialEmpresa, error = null) }
    }

    fun onNumberCuitEmpresaChange(numberCuitEmpresa: String) {
        _uiState.update { it.copy(numberCuitEmpresa = numberCuitEmpresa, error = null) }
    }

    fun onEmailEmpresaChange(emailEmpresa: String) {
        _uiState.update { it.copy(emailEmpresa = emailEmpresa, error = null) }
    }

    fun onPhoneNumberEmpresaChange(phoneNumberEmpresa: String) {
        _uiState.update { it.copy(phoneNumberEmpresa = phoneNumberEmpresa, error = null) }
    }

    fun onAddressEmpresaChange(addressEmpresa: String) {
        _uiState.update { it.copy(addressEmpresa = addressEmpresa, error = null) }
    }

    fun onCityEmpresaChange(cityEmpresa: String) {
        _uiState.update { it.copy(cityEmpresa = cityEmpresa, error = null) }
    }

    fun onStateEmpresaChange(stateEmpresa: String) {
        _uiState.update { it.copy(stateEmpresa = stateEmpresa, error = null) }
    }

    fun onZipCodeEmpresaChange(zipCodeEmpresa: String) {
        _uiState.update { it.copy(zipCodeEmpresa = zipCodeEmpresa, error = null) }
    }

    fun onAddressEmpresaSucursal1Change(addressEmpresaSucursal1: String) {
        _uiState.update { it.copy(addressEmpresaSucursal1 = addressEmpresaSucursal1, error = null) }
    }

    fun onCityEmpresaSucursal1Change(cityEmpresaSucursal1: String) {
        _uiState.update { it.copy(cityEmpresaSucursal1 = cityEmpresaSucursal1, error = null) }
    }

    fun onStateEmpresaSucursal1Change(stateEmpresaSucursal1: String) {
        _uiState.update { it.copy(stateEmpresaSucursal1 = stateEmpresaSucursal1, error = null) }
    }

    fun onZipCodeEmpresaSucursal1Change(zipCodeEmpresaSucursal1: String) {
        _uiState.update { it.copy(zipCodeEmpresaSucursal1 = zipCodeEmpresaSucursal1, error = null) }
    }

    fun onAddressEmpresaSucursal2Change(addressEmpresaSucursal2: String) {
        _uiState.update { it.copy(addressEmpresaSucursal2 = addressEmpresaSucursal2, error = null) }
    }

    fun onCityEmpresaSucursal2Change(cityEmpresaSucursal2: String) {
        _uiState.update { it.copy(cityEmpresaSucursal2 = cityEmpresaSucursal2, error = null) }
    }

    fun onStateEmpresaSucursal2Change(stateEmpresaSucursal2: String) {
        _uiState.update { it.copy(stateEmpresaSucursal2 = stateEmpresaSucursal2, error = null) }
    }

    fun onZipCodeEmpresaSucursal2Change(zipCodeEmpresaSucursal2: String) {
        _uiState.update { it.copy(zipCodeEmpresaSucursal2 = zipCodeEmpresaSucursal2, error = null) }
    }

    open fun onNotificationsEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled, error = null) }
    }

    open fun onIsPublicProfileChange(isPublic: Boolean) {
        _uiState.update { it.copy(isPublicProfile = isPublic, error = null) }
    }


    open fun saveProfile() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Usuario no autenticado"
                        )
                    }
                    return@launch
                }

                // Vincular email y contraseña a la cuenta de Google
                try {
                    val email = currentUser.email ?: ""
                    val password = _uiState.value.password

                    // --- CORRECCIÓN AQUÍ ---
                    // Usamos EmailAuthProvider directamente
                    val credential = EmailAuthProvider.getCredential(email, password)

                    currentUser.linkWithCredential(credential).await()
                } catch (e: Exception) {
                    // Si ya está vinculado o hay error, continúa (según tu lógica actual)
                    // Nota: Si la contraseña es débil, esto lanzará error y lo atraparás aquí.
                }

                // Verificar si ya existe documento (puede ser prestador también)
                val userDocRef = firestore.collection("usuarios").document(currentUser.uid)
                val existingDoc = userDocRef.get().await()

                if (existingDoc.exists()) {
                    // El usuario ya existe, agregar rol de cliente
                    val currentRoles = existingDoc.get("roles") as? MutableList<String> ?: mutableListOf()
                    if (!currentRoles.contains("cliente")) {
                        currentRoles.add("cliente")
                    }

                    // Actualizar con nuevos datos de cliente
                    val updateData = hashMapOf<String, Any>(
                        "roles" to currentRoles,
                        "displayName" to (currentUser.displayName ?: ""),
                        "email" to (currentUser.email ?: ""),
                        "phoneNumber" to _uiState.value.phoneNumber,
                        "address" to _uiState.value.address,
                        "city" to _uiState.value.city,
                        "state" to _uiState.value.state,
                        "zipCode" to _uiState.value.zipCode,
                        "isProfileComplete" to true,
                        "clienteCreatedAt" to System.currentTimeMillis()
                    )

                    // Si tiene valores de addressHome, agregarlos
                    if (_uiState.value.addressHome.isNotEmpty()) {
                        updateData["addressHome"] = _uiState.value.addressHome
                        updateData["cityHome"] = _uiState.value.cityHome
                        updateData["stateHome"] = _uiState.value.stateHome
                        updateData["zipCodeHome"] = _uiState.value.zipCodeHome
                    }

                    // Si tiene valores de addressWork, agregarlos
                    if (_uiState.value.addressWork.isNotEmpty()) {
                        updateData["addressWork"] = _uiState.value.addressWork
                        updateData["cityWork"] = _uiState.value.cityWork
                        updateData["stateWork"] = _uiState.value.stateWork
                        updateData["zipCodeWork"] = _uiState.value.zipCodeWork
                    }

                    userDocRef.update(updateData).await()
                } else {
                    // Usuario nuevo, crear documento
                    val userProfile = UserProfile(
                        uid = currentUser.uid,
                        displayName = currentUser.displayName ?: "",
                        email = currentUser.email ?: "",
                        phoneNumber = _uiState.value.phoneNumber,
                        address = _uiState.value.address,
                        addressHome = _uiState.value.addressHome,
                        addressWork = _uiState.value.addressWork,
                        cityHome = _uiState.value.cityHome,
                        stateHome = _uiState.value.stateHome,
                        zipCodeHome = _uiState.value.zipCodeHome,
                        cityWork = _uiState.value.cityWork,
                        stateWork = _uiState.value.stateWork,
                        zipCodeWork = _uiState.value.zipCodeWork,
                        city = _uiState.value.city,
                        state = _uiState.value.state,
                        zipCode = _uiState.value.zipCode,
                        isProfileComplete = true
                    )

                    // Convertir a Map y agregar roles
                    val profileData = hashMapOf<String, Any>(
                        "uid" to userProfile.uid,
                        "displayName" to userProfile.displayName,
                        "email" to userProfile.email,
                        "phoneNumber" to userProfile.phoneNumber,
                        "address" to userProfile.address,
                        "city" to userProfile.city,
                        "state" to userProfile.state,
                        "zipCode" to userProfile.zipCode,
                        "isProfileComplete" to true,
                        "roles" to listOf("cliente"),
                        "createdAt" to System.currentTimeMillis()
                    )

                    // Agregar campos opcionales si tienen valor
                    if (userProfile.addressHome.isNotEmpty()) {
                        profileData["addressHome"] = userProfile.addressHome
                        profileData["cityHome"] = userProfile.cityHome
                        profileData["stateHome"] = userProfile.stateHome
                        profileData["zipCodeHome"] = userProfile.zipCodeHome
                    }

                    if (userProfile.addressWork.isNotEmpty()) {
                        profileData["addressWork"] = userProfile.addressWork
                        profileData["cityWork"] = userProfile.cityWork
                        profileData["stateWork"] = userProfile.stateWork
                        profileData["zipCodeWork"] = userProfile.zipCodeWork
                    }

                    userDocRef.set(profileData).await()
                }

                _uiState.update { it.copy(isLoading = false, isComplete = true) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al guardar datos"
                    )
                }
            }
        }
    }


    private fun validateInputs(): Boolean {

        val state = _uiState.value


        if (state.phoneNumber.isEmpty() || state.phoneNumber.length < 10) {

            _uiState.update { it.copy(error = "Ingresa un número de teléfono válido") }

            return false

        }


        if (state.password.isEmpty() || state.password.length < 6) {

            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }

            return false

        }


        if (state.address.isEmpty()) {

            _uiState.update { it.copy(error = "La dirección es requerida") }

            return false

        }


        if (state.city.isEmpty()) {

            _uiState.update { it.copy(error = "La ciudad es requerida") }

            return false

        }


        if (state.state.isEmpty()) {

            _uiState.update { it.copy(error = "El estado es requerido") }

            return false

        }


        if (state.zipCode.isEmpty()) {

            _uiState.update { it.copy(error = "El código postal es requerido") }

            return false

        }


        return true

    }


    fun loadUserProfile() {

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }


            try {

                val currentUser = auth.currentUser

                if (currentUser != null) {

                    val doc = firestore.collection("usuarios")

                        .document(currentUser.uid)

                        .get()

                        .await()


                    val profile = doc.toObject(UserProfile::class.java)

                    _uiState.update {

                        it.copy(

                            isLoading = false,

                            displayName = profile?.displayName ?: currentUser.displayName ?: "",

                            email = profile?.email ?: currentUser.email ?: "",

                            phoneNumber = profile?.phoneNumber ?: "",

                            address = profile?.address ?: "",

                            addressHome = profile?.addressHome ?: "",

                            addressWork = profile?.addressWork ?: "",

                            city = profile?.city ?: "",

                            state = profile?.state ?: "",

                            zipCode = profile?.zipCode ?: "",

                            photoUrl = profile?.photoUrl ?: currentUser.photoUrl?.toString() ?: "",

                            coverPhotoUrl = profile?.coverPhotoUrl ?: "",

                            cityHome = profile?.cityHome ?: "",

                            stateHome = profile?.stateHome ?: "",

                            zipCodeHome = profile?.zipCodeHome ?: "",

                            cityWork = profile?.cityWork ?: "",

                            stateWork = profile?.stateWork ?: "",

                            zipCodeWork = profile?.zipCodeWork ?: "",

                            isEmpresa = profile?.isEmpresa ?: false,
                            nameComercialEmpresa = profile?.nameComercialEmpresa ?: "",
                            nameRazonSocialEmpresa = profile?.nameRazonSocialEmpresa ?: "",
                            numberCuitEmpresa = profile?.numberCuitEmpresa ?: "",
                            emailEmpresa = profile?.emailEmpresa ?: "",
                            phoneNumberEmpresa = profile?.phoneNumberEmpresa ?: "",
                            addressEmpresa = profile?.addressEmpresa ?: "",
                            cityEmpresa = profile?.cityEmpresa ?: "",
                            stateEmpresa = profile?.stateEmpresa ?: "",
                            zipCodeEmpresa = profile?.zipCodeEmpresa ?: "",
                            addressEmpresaSucursal1 = profile?.addressEmpresaSucursal1 ?: "",
                            cityEmpresaSucursal1 = profile?.cityEmpresaSucursal1 ?: "",
                            stateEmpresaSucursal1 = profile?.stateEmpresaSucursal1 ?: "",
                            zipCodeEmpresaSucursal1 = profile?.zipCodeEmpresaSucursal1 ?: "",
                            addressEmpresaSucursal2 = profile?.addressEmpresaSucursal2 ?: "",
                            cityEmpresaSucursal2 = profile?.cityEmpresaSucursal2 ?: "",
                            stateEmpresaSucursal2 = profile?.stateEmpresaSucursal2 ?: "",
                            zipCodeEmpresaSucursal2 = profile?.zipCodeEmpresaSucursal2 ?: ""


                        )

                    }

                }

            } catch (e: Exception) {

                _uiState.update { it.copy(isLoading = false, error = e.message) }

            }

        }

    }


    fun updateProfile(displayName: String, phoneNumber: String, address: String) {

        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }


            try {

                val currentUser = auth.currentUser

                if (currentUser != null) {

                    // Detectar qué cambió para mostrar mensaje específico
                    val nameChanged = displayName != _uiState.value.displayName
                    val phoneChanged = phoneNumber != _uiState.value.phoneNumber

                    val successMsg = when {
                        nameChanged && phoneChanged -> "✓ Nombre y teléfono actualizados"
                        nameChanged -> "✓ Nombre actualizado correctamente"
                        phoneChanged -> "✓ Teléfono actualizado correctamente"
                        else -> "✓ Perfil actualizado correctamente"
                    }

                    val updates = mutableMapOf(

                        "displayName" to displayName,

                        "phoneNumber" to phoneNumber,

                        "address" to address,

                        "isProfileComplete" to true

                    )

                    if (_uiState.value.isEmpresa) {
                        updates["isEmpresa"] = _uiState.value.isEmpresa
                        updates["nameComercialEmpresa"] = _uiState.value.nameComercialEmpresa
                        updates["nameRazonSocialEmpresa"] = _uiState.value.nameRazonSocialEmpresa
                        updates["numberCuitEmpresa"] = _uiState.value.numberCuitEmpresa
                        updates["emailEmpresa"] = _uiState.value.emailEmpresa
                        updates["phoneNumberEmpresa"] = _uiState.value.phoneNumberEmpresa
                        updates["addressEmpresa"] = _uiState.value.addressEmpresa
                        updates["cityEmpresa"] = _uiState.value.cityEmpresa
                        updates["stateEmpresa"] = _uiState.value.stateEmpresa
                        updates["zipCodeEmpresa"] = _uiState.value.zipCodeEmpresa
                        updates["addressEmpresaSucursal1"] = _uiState.value.addressEmpresaSucursal1
                        updates["cityEmpresaSucursal1"] = _uiState.value.cityEmpresaSucursal1
                        updates["stateEmpresaSucursal1"] = _uiState.value.stateEmpresaSucursal1
                        updates["zipCodeEmpresaSucursal1"] = _uiState.value.zipCodeEmpresaSucursal1
                        updates["addressEmpresaSucursal2"] = _uiState.value.addressEmpresaSucursal2
                        updates["cityEmpresaSucursal2"] = _uiState.value.cityEmpresaSucursal2
                        updates["stateEmpresaSucursal2"] = _uiState.value.stateEmpresaSucursal2
                        updates["zipCodeEmpresaSucursal2"] = _uiState.value.zipCodeEmpresaSucursal2
                    }


                    firestore.collection("usuarios")

                        .document(currentUser.uid)

                        .update(updates as Map<String, Any>)

                        .await()


                    _uiState.update {

                        it.copy(

                            isLoading = false,

                            displayName = displayName,

                            phoneNumber = phoneNumber,

                            address = address,

                            successMessage = successMsg

                        )

                    }

                }

            } catch (e: Exception) {

                _uiState.update { it.copy(isLoading = false, error = "Error al actualizar perfil: ${e.message}") }

            }

        }

    }

    // Función para actualizar solo las direcciones sin requerir contraseña
    fun updateAddresses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val updates = mapOf(
                        "address" to _uiState.value.address,
                        "city" to _uiState.value.city,
                        "state" to _uiState.value.state,
                        "zipCode" to _uiState.value.zipCode,
                        "addressHome" to _uiState.value.addressHome,
                        "cityHome" to _uiState.value.cityHome,
                        "stateHome" to _uiState.value.stateHome,
                        "zipCodeHome" to _uiState.value.zipCodeHome,
                        "addressWork" to _uiState.value.addressWork,
                        "cityWork" to _uiState.value.cityWork,
                        "stateWork" to _uiState.value.stateWork,
                        "zipCodeWork" to _uiState.value.zipCodeWork
                    )

                    firestore.collection("usuarios")
                        .document(currentUser.uid)
                        .update(updates)
                        .await()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "✓ Direcciones actualizadas correctamente"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al actualizar direcciones: ${e.message}") }
            }
        }
    }


    fun updateProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Guardar la URI directamente (almacenamiento local)
                    val photoUrlString = uri.toString()

                    // Actualizar el estado local con la URI
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            photoUrl = photoUrlString,
                            successMessage = "✓ Foto de perfil actualizada"
                        )
                    }

                    // Actualizar en Firestore con la URI local
                    firestore.collection("usuarios")
                        .document(currentUser.uid)
                        .update("photoUrl", photoUrlString)
                        .await()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al actualizar foto: ${e.message}") }
            }
        }
    }

    fun deleteProfilePhoto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null)}
            try {
                val  currentUser = auth.currentUser
                if (currentUser != null) {
                    // Limpiar la URL  de la foto
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            photoUrl = "",
                            successMessage = "✓ Foto de perfil eliminada"
                        )
                    }

                    //Actualizar en Firestore (Eliminar el campo)
                    firestore.collection("usuarios")
                        .document(currentUser.uid)
                        .update("photoUrl", "")
                        .await()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al eliminar foto: ${e.message}") }
            }
        }
    }


    fun updateCoverPhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val coverPhotoUrlString = uri.toString()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            coverPhotoUrl = coverPhotoUrlString,
                            successMessage = "✓ Foto de portada actualizada"
                        )
                    }

                    firestore.collection("usuarios")
                        .document(currentUser.uid)
                        .update("coverPhotoUrl", coverPhotoUrlString)
                        .await()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al actualizar portada: ${e.message}") }
            }
        }
    }


    fun deleteCoverPhoto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            coverPhotoUrl = "",
                            successMessage = "✓ Foto de portada eliminada"
                        )
                    }

                    firestore.collection("usuarios")
                        .document(currentUser.uid)
                        .update("coverPhotoUrl", "")
                        .await()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al eliminar portada: ${e.message}") }
            }
        }
    }


    fun getCurrentLocation(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Permisos de ubicación no concedidos"
                        )
                    }
                    return@launch
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModelScope.launch {
                            getAddressFromLocation(context, location.latitude, location.longitude)
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "No se pudo obtener la ubicación"
                            )
                        }
                    }
                }.addOnFailureListener { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al obtener ubicación: ${exception.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                address = address.getAddressLine(0) ?: "",
                                city = address.locality ?: "",
                                state = address.adminArea ?: "",
                                zipCode = address.postalCode ?: ""
                            )
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            address = address.getAddressLine(0) ?: "",
                            city = address.locality ?: "",
                            state = address.adminArea ?: "",
                            zipCode = address.postalCode ?: ""
                        )
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Error al obtener dirección: ${e.message}"
                )
            }
        }
    }


    fun updateEmail(newEmail: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

            try {
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.email != null) {
                    // Configurar idioma español para el email
                    auth.setLanguageCode("es")

                    // Re-autenticar al usuario antes de cambiar el email
                    val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
                    currentUser.reauthenticate(credential).await()

                    // Usar verifyBeforeUpdateEmail para mayor seguridad
                    // Esto envía un email de verificación ANTES de cambiar el email
                    currentUser.verifyBeforeUpdateEmail(newEmail).await()

                    //Correccion de mensaje de verificacion
                    isEmailChangePending = true

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            successMessage = "✓ Se envió un correo de verificación a $newEmail. Revisa tu bandeja de entrada y confirma el cambio."
                        )
                    }



                    // Nota: El email en Firestore se actualizará después de que el usuario verifique
                    // el link en su correo. Puedes agregar un listener para detectar cuando se verifica.
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("invalid-email") == true -> "Email inválido"
                    e.message?.contains("email-already-in-use") == true -> "Este email ya está en uso"
                    e.message?.contains("wrong-password") == true -> "Contraseña incorrecta"
                    e.message?.contains("requires-recent-login") == true -> "Por seguridad, vuelve a iniciar sesión"
                    else -> "Error al actualizar email: ${e.message}"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    // Función para verificar y completar el cambio de email después de la verificación
    fun checkAndCompleteEmailChange() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Recargar el usuario para obtener el email actualizado
                    currentUser.reload().await()

                    val newEmail = currentUser.email
                    if (newEmail != null && newEmail != _uiState.value.email) {
                        // El email fue verificado y actualizado, ahora actualizar Firestore
                        firestore.collection("usuarios")
                            .document(currentUser.uid)
                            .update("email", newEmail)
                            .await()

                        _uiState.update {
                            it.copy(
                                email = newEmail,
                                successMessage = if (isEmailChangePending) {
                                    isEmailChangePending = false;
                                    "✓ Email verificado y actualizado exitosamente"
                                } else {

                                    null
                                }
                            )
                        }
                    } else if (newEmail != null) {
                        // Actualizar el estado aunque sea el mismo email
                        // para asegurar sincronización
                        _uiState.update {
                            it.copy(email = newEmail)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error verificando email: ${e.message}")
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

            try {
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.email != null) {
                    // Re-autenticar al usuario antes de cambiar la contraseña
                    val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                    currentUser.reauthenticate(credential).await()

                    // Actualizar la contraseña en Firebase Auth
                    currentUser.updatePassword(newPassword).await()

                    // Actualizar el estado local
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            successMessage = "✓ Contraseña actualizada exitosamente"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al actualizar contraseña: ${e.message}"
                    )
                }
            }
        }
    }

    // Función para limpiar mensajes después de mostrarlos
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}