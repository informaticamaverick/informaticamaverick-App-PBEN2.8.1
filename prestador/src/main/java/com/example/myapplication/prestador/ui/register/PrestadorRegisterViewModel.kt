package com.example.myapplication.prestador.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.dao.ProviderDao
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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
        apellido: String,
        categoria: String,
        mensaje: String,
        serviceType: String,
        isGoogleUser: Boolean = false,
        telefono: String = "",
        dniCuit: String = "",
        matricula: String = "",
        profesion: String = "",
        direccion: String = "",
        codigoPostal: String = "",
        provincia: String = "",
        tieneNegocio: Boolean = false,
        nombreNegocio: String = "",
        razonSocial: String = "",
        cuitNegocio: String = "",
        direccionNegocio: String = "",
        codigoPostalNegocio: String = "",
        sucursales: List<Map<String, String>> = emptyList(),
        isHomeService: Boolean = false,
        is24Hours: Boolean = false,
        hasPhysicalStore: Boolean = false,
        hasStoreAppointments: Boolean = false
    ) {

        //Validar campos antes de llamar Firebase
        if (!isGoogleUser) {
            if (email.isBlank() || !email.contains("@")) {
               _registerState.value = RegisterState.Error("Ingresá un correo electrónico valido")
                return
            }
            if (password.length < 6) {
                _registerState.value = RegisterState.Error("La contraseña debe tener al menso 6 caracteres")
                return
            }
        }

        if (nombre.isBlank()) {
            _registerState.value = RegisterState.Error("Ingresa tunombre")
            return
        }

        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            try {
                val servicios = listOf(categoria).filter { it.isNotBlank() }
                //Para registro manual. siempre crear cuenta nueva en Fireba Auth
                //ignorar cualquier sesion activa de otro usuario
                val currentUser = if (isGoogleUser) auth.currentUser else null

                if (currentUser != null) {
                    val userDocRef = firestore.collection("usuarios").document(currentUser.uid)
                    val existingDoc = userDocRef.get().await()

                    if (existingDoc.exists()) {
                        val currentRoles =
                            (existingDoc.get("roles") as? List<*>)?.filterIsInstance<String>()?.toMutableList()
                                ?: mutableListOf()

                        if (!currentRoles.contains("prestador")) {
                            currentRoles.add("prestador")
                        }

                        val updateData = hashMapOf<String, Any>(
                            "roles" to currentRoles,
                            "nombre" to nombre,
                            "apellido" to apellido,
                            "email" to (currentUser.email ?: email),
                            "telefono" to telefono,
                            "dniCuit" to dniCuit,
                            "matricula" to matricula,
                            "profesion" to profesion,
                            "direccion" to direccion,
                            "codigoPostal" to codigoPostal,
                            "provincia" to provincia,
                            "servicios" to servicios,
                            "description" to mensaje,
                            "serviceType" to serviceType,
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
                        saveProviderToRoom(
                            id = currentUser.uid,
                            nombre = nombre,
                            apellido = apellido,
                            email = currentUser.email ?: email,
                            telefono = telefono,
                            mensaje = mensaje,
                            servicios = servicios,
                            serviceType = serviceType
                        )
                    } else {
                        val prestadorData = hashMapOf(
                            "nombre" to nombre,
                            "apellido" to apellido,
                            "email" to (currentUser.email ?: email),
                            "telefono" to telefono,
                            "dniCuit" to dniCuit,
                            "matricula" to matricula,
                            "profesion" to profesion,
                            "direccion" to direccion,
                            "codigoPostal" to codigoPostal,
                            "provincia" to provincia,
                            "servicios" to servicios,
                            "description" to mensaje,
                            "serviceType" to serviceType,
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
                        saveProviderToRoom(
                            id = currentUser.uid,
                            nombre = nombre,
                            apellido = apellido,
                            email = currentUser.email ?: email,
                            telefono = telefono,
                            mensaje = mensaje,
                            servicios = servicios,
                            serviceType = serviceType
                        )
                    }

                    _registerState.value = RegisterState.Success
                } else {
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    val userId = result.user?.uid ?: throw Exception("Error al crear usuario")

                    val prestadorData = hashMapOf(
                        "nombre" to nombre,
                        "apellido" to apellido,
                        "email" to email,
                        "telefono" to telefono,
                        "dniCuit" to dniCuit,
                        "matricula" to matricula,
                        "profesion" to profesion,
                        "direccion" to direccion,
                        "codigoPostal" to codigoPostal,
                        "provincia" to provincia,
                        "servicios" to servicios,
                        "description" to mensaje,
                        "serviceType" to serviceType,
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

                    saveProviderToRoom(
                        id = userId,
                        nombre = nombre,
                        apellido = apellido,
                        email = email,
                        telefono = telefono,
                        mensaje = mensaje,
                        servicios = servicios,
                        serviceType = serviceType
                    )

                    _registerState.value = RegisterState.Success
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Error al registrar")
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }

    private suspend fun saveProviderToRoom(
        id: String,
        nombre: String,
        apellido: String,
        email: String,
        telefono: String,
        mensaje: String,
        servicios: List<String>,
        serviceType: String
    ) {
        val categoriesJson = Gson().toJson(servicios)

        val providerEntity = ProviderEntity(
            id = id,
            name = nombre,
            apellido = apellido.ifBlank { null },
            email = email,
            phone = telefono,
            imageUrl = null,
            description = mensaje.ifBlank { null },
            address = null,
            rating = 0f,
            categories = categoriesJson,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            serviceType = serviceType
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
