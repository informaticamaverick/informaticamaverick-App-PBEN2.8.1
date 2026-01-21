package com.example.myapplication.prestador.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class PrestadorRegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        direccion: String,
        codigoPostal: String,
        ciudad: String,
        provincia: String,
        servicios: List<String>,
        isHomeService: Boolean,
        is24Hours: Boolean
    ) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            try {
                // Obtener usuario actual (ya autenticado con Google)
                val currentUser = auth.currentUser
                
                if (currentUser != null) {
                    // Usuario ya autenticado (Google), verificar si ya tiene documento
                    val userDocRef = firestore.collection("usuarios").document(currentUser.uid)
                    val existingDoc = userDocRef.get().await()
                    
                    if (existingDoc.exists()) {
                        // El usuario ya existe, agregar rol de prestador
                        val currentRoles = existingDoc.get("roles") as? MutableList<String> ?: mutableListOf()
                        if (!currentRoles.contains("prestador")) {
                            currentRoles.add("prestador")
                        }
                        
                        // Actualizar con nuevos datos de prestador
                        val updateData = hashMapOf<String, Any>(
                            "roles" to currentRoles,
                            "direccion" to direccion,
                            "codigoPostal" to codigoPostal,
                            "ciudad" to ciudad,
                            "provincia" to provincia,
                            "servicios" to servicios,
                            "isHomeService" to isHomeService,
                            "is24Hours" to is24Hours,
                            "prestadorCreatedAt" to System.currentTimeMillis()
                        )
                        
                        userDocRef.update(updateData).await()
                    } else {
                        // Usuario nuevo, crear documento
                        val prestadorData = hashMapOf(
                            "nombre" to nombre,
                            "apellido" to apellido,
                            "email" to (currentUser.email ?: email),
                            "direccion" to direccion,
                            "codigoPostal" to codigoPostal,
                            "ciudad" to ciudad,
                            "provincia" to provincia,
                            "servicios" to servicios,
                            "isHomeService" to isHomeService,
                            "is24Hours" to is24Hours,
                            "roles" to listOf("prestador"),
                            "createdAt" to System.currentTimeMillis()
                        )
                        
                        userDocRef.set(prestadorData).await()
                    }

                    _registerState.value = RegisterState.Success
                } else {
                    // Usuario nuevo con email y contraseña
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    val userId = result.user?.uid ?: throw Exception("Error al crear usuario")

                    val prestadorData = hashMapOf(
                        "nombre" to nombre,
                        "apellido" to apellido,
                        "email" to email,
                        "direccion" to direccion,
                        "codigoPostal" to codigoPostal,
                        "ciudad" to ciudad,
                        "provincia" to provincia,
                        "servicios" to servicios,
                        "isHomeService" to isHomeService,
                        "is24Hours" to is24Hours,
                        "roles" to listOf("prestador"),
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("usuarios")
                        .document(userId)
                        .set(prestadorData)
                        .await()

                    _registerState.value = RegisterState.Success
                }

            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Error al registrar")
            }
        }
    }
}


sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}