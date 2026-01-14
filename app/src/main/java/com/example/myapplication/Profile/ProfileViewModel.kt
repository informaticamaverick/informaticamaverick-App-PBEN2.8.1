package com.example.myapplication.Profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.Data.Model.UserProfile
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
import javax.inject.Inject
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.storage.FirebaseStorage





@HiltViewModel

class ProfileViewModel @Inject constructor() : ViewModel() {


    private val _uiState = MutableStateFlow(ProfileUiState())

    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()


    private val firestore = Firebase.firestore

    private val auth = Firebase.auth


    fun onPhoneNumberChange(phone: String) {

        _uiState.update { it.copy(phoneNumber = phone, error = null) }

    }


    fun onPasswordChange(password: String) {

        _uiState.update { it.copy(password = password, error = null) }

    }


    fun onAddressChange(address: String) {

        _uiState.update { it.copy(address = address, error = null) }

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


    fun saveProfile() {
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

                val userProfile = UserProfile(
                    uid = currentUser.uid,
                    displayName = currentUser.displayName ?: "",
                    email = currentUser.email ?: "",
                    phoneNumber = _uiState.value.phoneNumber,
                    address = _uiState.value.address,
                    city = _uiState.value.city,
                    state = _uiState.value.state,
                    zipCode = _uiState.value.zipCode,
                    isProfileComplete = true
                )

                firestore.collection("users")
                    .document(currentUser.uid)
                    .set(userProfile)
                    .await()

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

                    val doc = firestore.collection("users")

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

                            city = profile?.city ?: "",

                            state = profile?.state ?: "",

                            zipCode = profile?.zipCode ?: "",

                            photoUrl = profile?.photoUrl ?: currentUser.photoUrl?.toString() ?: ""

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

            _uiState.update { it.copy(isLoading = true) }


            try {

                val currentUser = auth.currentUser

                if (currentUser != null) {

                    val updates = mapOf(

                        "displayName" to displayName,

                        "phoneNumber" to phoneNumber,

                        "address" to address

                    )


                    firestore.collection("users")

                        .document(currentUser.uid)

                        .update(updates)

                        .await()


                    _uiState.update {

                        it.copy(

                            isLoading = false,

                            displayName = displayName,

                            phoneNumber = phoneNumber,

                            address = address

                        )

                    }

                }

            } catch (e: Exception) {

                _uiState.update { it.copy(isLoading = false, error = e.message) }

            }

        }

    }


    fun updateProfilePhoto(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Guardar la URI directamente (almacenamiento local)
                    val photoUrlString = uri.toString()
                    
                    // Actualizar el estado local con la URI
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            photoUrl = photoUrlString
                        )
                    }
                    
                    // Actualizar en Firestore con la URI local
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .update("photoUrl", photoUrlString)
                        .await()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteProfilePhoto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true)}
            try {
                val  currentUser = auth.currentUser
                if (currentUser != null) {
                    // Limpiar la URL  de la foto
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            photoUrl = ""
                        )
                    }

                    //Actualizar en Firestore (Eliminar el campo)
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .update("photoUrl", "")
                        .await()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun updateEmail(newEmail: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.email != null) {
                    // Re-autenticar al usuario antes de cambiar el email
                    val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
                    currentUser.reauthenticate(credential).await()
                    
                    // Actualizar el email en Firebase Auth
                    currentUser.updateEmail(newEmail).await()
                    
                    // Actualizar el email en Firestore
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .update("email", newEmail)
                        .await()
                    
                    // Actualizar el estado local
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            email = newEmail,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al actualizar email: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
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
                            error = null
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
}
