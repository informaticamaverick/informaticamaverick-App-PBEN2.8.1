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
import com.example.myapplication.prestador.data.local.dao.ProviderDao
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.google.gson.Gson

@HiltViewModel
class PrestadorRegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val providerDao: ProviderDao
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(
        email: String,
        password: String,
        nombre: String,
        dniCuit: String,
        telefono: String,
        matricula: String,
        profesion: String,
        direccion: String,
        codigoPostal: String,
        provincia: String,
        servicios: List<String>,
        // Configuración de Negocio
        tieneNegocio: Boolean,
        nombreNegocio: String,
        razonSocial: String,
        cuitNegocio: String,
        direccionNegocio: String,
        codigoPostalNegocio: String,
        sucursales: List<Map<String, String>>,
        // Configuración de Servicio
        isHomeService: Boolean,
        is24Hours: Boolean,
        hasPhysicalStore: Boolean,
        hasStoreAppointments: Boolean
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
                            "nombre" to nombre,
                            "dniCuit" to dniCuit,
                            "telefono" to telefono,
                            "matricula" to matricula,
                            "profesion" to profesion,
                            "direccion" to direccion,
                            "codigoPostal" to codigoPostal,
                            "provincia" to provincia,
                            "servicios" to servicios,
                            "tieneNegocio" to tieneNegocio,
                            "nombreNegocio" to nombreNegocio,
                            "razonSocial" to razonSocial,
                            "cuitNegocio" to cuitNegocio,
                            "direccionNegocio" to direccionNegocio,
                            "codigoPostalNegocio" to codigoPostalNegocio,
                            "sucursales" to sucursales,
                            "isHomeService" to isHomeService,
                            "is24Hours" to is24Hours,
                            "hasPhysicalStore" to hasPhysicalStore,
                            "hasStoreAppointments" to hasStoreAppointments,
                            "prestadorCreatedAt" to System.currentTimeMillis()
                        )
                        
                        userDocRef.update(updateData).await()
                    } else {
                        // Usuario nuevo, crear documento
                        val prestadorData = hashMapOf(
                            "nombre" to nombre,
                            "email" to (currentUser.email ?: email),
                            "dniCuit" to dniCuit,
                            "telefono" to telefono,
                            "matricula" to matricula,
                            "profesion" to profesion,
                            "direccion" to direccion,
                            "codigoPostal" to codigoPostal,
                            "provincia" to provincia,
                            "servicios" to servicios,
                            "tieneNegocio" to tieneNegocio,
                            "nombreNegocio" to nombreNegocio,
                            "razonSocial" to razonSocial,
                            "cuitNegocio" to cuitNegocio,
                            "direccionNegocio" to direccionNegocio,
                            "codigoPostalNegocio" to codigoPostalNegocio,
                            "sucursales" to sucursales,
                            "isHomeService" to isHomeService,
                            "is24Hours" to is24Hours,
                            "hasPhysicalStore" to hasPhysicalStore,
                            "hasStoreAppointments" to hasStoreAppointments,
                            "roles" to listOf("prestador"),
                            "createdAt" to System.currentTimeMillis()
                        )
                        
                        userDocRef.set(prestadorData).await()
                        userDocRef.set(prestadorData).await()
                        //Guardar en la base de datos local Room
                        saveProviderToRoom(currentUser.uid, nombre, currentUser.email ?: email, telefono, servicios)
                    }

                    _registerState.value = RegisterState.Success
                } else {
                    // Usuario nuevo con email y contraseña
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    val userId = result.user?.uid ?: throw Exception("Error al crear usuario")

                    val prestadorData = hashMapOf(
                        "nombre" to nombre,
                        "email" to email,
                        "dniCuit" to dniCuit,
                        "telefono" to telefono,
                        "matricula" to matricula,
                        "profesion" to profesion,
                        "direccion" to direccion,
                        "codigoPostal" to codigoPostal,
                        "provincia" to provincia,
                        "servicios" to servicios,
                        "tieneNegocio" to tieneNegocio,
                        "nombreNegocio" to nombreNegocio,
                        "razonSocial" to razonSocial,
                        "cuitNegocio" to cuitNegocio,
                        "direccionNegocio" to direccionNegocio,
                        "codigoPostalNegocio" to codigoPostalNegocio,
                        "sucursales" to sucursales,
                        "isHomeService" to isHomeService,
                        "is24Hours" to is24Hours,
                        "hasPhysicalStore" to hasPhysicalStore,
                        "hasStoreAppointments" to hasStoreAppointments,
                        "roles" to listOf("prestador"),
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("usuarios")
                        .document(userId)
                        .set(prestadorData)
                        .await()
                    //Guardar en la base de datos local Room
                    saveProviderToRoom(userId, nombre, email, telefono, servicios)

                    _registerState.value = RegisterState.Success

                }

            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Error al registrar")
            }
        }
    }
    
    private suspend fun saveProviderToRoom(
        id: String,
        nombre: String,
        email: String,
        telefono: String,
        servicios: List<String>
    ) {
        val gson = Gson()
        val categoriesJson = gson.toJson(servicios)
        
        val providerEntity = ProviderEntity(
            id = id,
            name = nombre,
            email = email,
            phone = telefono,
            imageUrl = null,
            description = null,
            address = null,
            rating = 0f,
            categories = categoriesJson,
            isActive = true,
            createdAt = System.currentTimeMillis()
        )
        
        providerDao.insertProvider(providerEntity)
    }
}


sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}