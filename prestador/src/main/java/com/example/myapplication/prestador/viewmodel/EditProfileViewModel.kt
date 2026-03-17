package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.example.myapplication.prestador.data.model.PrestadorProfileMode
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.data.repository.BusinessRepository
import com.example.myapplication.prestador.data.repository.ProviderRepository
import com.example.myapplication.prestador.utils.ServiceTypeConfig
import com.example.myapplication.prestador.utils.getServiceTypeConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val businessRepository: BusinessRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    
    // Estado del modo de visualización del perfil
    private val _profileMode = MutableStateFlow(PrestadorProfileMode.PERSONAL)
    val profileMode: StateFlow<PrestadorProfileMode> = _profileMode.asStateFlow()
    
    // ID del business del prestador (si tiene empresa)
    private val _businessId = MutableStateFlow<String?>(null)
    val businessId: StateFlow<String?> = _businessId.asStateFlow()

    private val _bussinesEntity = MutableStateFlow<com.example.myapplication.prestador.data.local.entity.BusinessEntity?>(null)
    val businessEntity: StateFlow<com.example.myapplication.prestador.data.local.entity.BusinessEntity?> = _bussinesEntity.asStateFlow()
    
    // Configuración de tipo de servicio
    private val _serviceTypeConfig = MutableStateFlow(getServiceTypeConfig(ServiceType.TECHNICAL))
    val serviceTypeConfig: StateFlow<ServiceTypeConfig> = _serviceTypeConfig.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                
                // Siempre cargar desde Firebase para garantizar datos frescos
                // (evita mostrar datos de otra sesión que quedaron en cache de Room)
                loadFromFirebase(userId)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Error al cargar perfil")
            }
        }
    }

    private suspend fun loadFromFirebase(userId: String) {
        try {
            val doc = firestore.collection("usuarios").document(userId).get().await()
            if (doc.exists()) {
                val provider = ProviderEntity(
                    id = userId,
                    name = doc.getString("nombre") ?: "",
                    apellido = doc.getString("apellido"),
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("telefono") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    description = doc.getString("description"),
                    address = doc.getString("direccion"),
                    rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                    categories = doc.get("servicios")?.toString() ?: "[]",
                    isActive = doc.getBoolean("isActive") ?: true,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    // Campos nuevos
                    dniCuit = doc.getString("dniCuit"),
                    profesion = doc.getString("profesion"),
                    tieneMatricula = doc.getBoolean("tieneMatricula") ?: false,
                    matricula = doc.getString("matricula"),
                    provincia = doc.getString("provincia"),
                    codigoPostal = doc.getString("codigoPostal"),
                    pais = doc.getString("pais") ?: "Argentina",
                    atencionUrgencias = doc.getBoolean("atencionUrgencias") ?: false,
                    vaDomicilio = doc.getBoolean("vaDomicilio") ?: false,
                    turnosEnLocal = doc.getBoolean("turnosEnLocal") ?: false,
                    direccionLocal = doc.getString("direccionLocal"),
                    provinciaLocal = doc.getString("provinciaLocal"),
                    codigoPostalLocal = doc.getString("codigoPostalLocal"),
                    horarioLocal = doc.getString("horarioLocal"),
                    tieneEmpresa = doc.getBoolean("tieneEmpresa") ?: false,
                    trabajaConOtros = doc.getBoolean("trabajaConOtros") ?: false,
                    nombreEmpresa = doc.getString("nombreEmpresa"),
                    cuitEmpresa = doc.getString("cuitEmpresa"),
                    direccionEmpresa = doc.getString("direccionEmpresa"),
                    serviceType = doc.getString("serviceType") ?: "TECHNICAL",
                    envios = doc.getBoolean("envios") ?: false,
                    atiendeVirtual = doc.getBoolean("atiendeVirtual") ?: false
                )
                providerRepository.saveProvider(provider)
                _profileState.value = ProfileState.Success(provider)
                // Sincronizar el modo con tieneEmpresa
                _profileMode.value = if (provider.tieneEmpresa) {
                    PrestadorProfileMode.EMPRESA
                } else {
                    PrestadorProfileMode.PERSONAL
                }
                
                // Actualizar configuración de tipo de servicio
                _serviceTypeConfig.value = getServiceTypeConfig(
                    ServiceType.fromString(provider.serviceType)
                )
                
                // Cargar businessId si tiene empresa
                if (provider.tieneEmpresa) {
                    val businesses = businessRepository.getBusinessesByProvider(userId).first()
                    val existingBusiness = businesses.firstOrNull()
                    
                    if (existingBusiness != null) {
                        _businessId.value = existingBusiness.id
                        _bussinesEntity.value = existingBusiness
                    } else {
                        // Si tiene empresa pero no existe BusinessEntity, crearlo
                        if (!provider.nombreEmpresa.isNullOrBlank() &&
                            !provider.cuitEmpresa.isNullOrBlank() &&
                            !provider.direccionEmpresa.isNullOrBlank()) {
                            
                            val newBusinessId = UUID.randomUUID().toString()
                            val newBusiness = BusinessEntity(
                                id = newBusinessId,
                                providerId = userId,
                                nombreNegocio = provider.nombreEmpresa!!,
                                razonSocial = provider.nombreEmpresa!!,
                                cuitNegocio = provider.cuitEmpresa!!,
                                direccion = provider.direccionEmpresa!!,
                                codigoPostal = "",
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            businessRepository.saveBusiness(newBusiness)
                            _businessId.value = newBusinessId
                        } else {
                            _businessId.value = null
                        }
                    }
                } else {
                    _businessId.value = null
                }
            } else {
                _profileState.value = ProfileState.Error("Perfil no encontrado")
            }
        } catch (e: Exception) {
            _profileState.value = ProfileState.Error(e.message ?: "Error al cargar desde Firebase")
        }
    }

    fun updateProfile(
        name: String? = null,
        apellido: String? = null,
        email: String? = null,
        phone: String? = null,
        description: String? = null,
        address: String? = null,
        dniCuit: String? = null,
        profesion: String? = null,
        tieneMatricula: Boolean? = null,
        matricula: String? = null,
        provincia: String? = null,
        codigoPostal: String? = null,
        pais: String? = null,
        atencionUrgencias: Boolean? = null,
        vaDomicilio: Boolean? = null,
        turnosEnLocal: Boolean? = null,
        direccionLocal: String? = null,
        provinciaLocal: String? = null,
        codigoPostalLocal: String? = null,
        tieneEmpresa: Boolean? = null,
        trabajaConOtros: Boolean? = null,
        envios: Boolean? = null,
        nombreEmpresa: String? = null,
        cuitEmpresa: String? = null,
        direccionEmpresa: String? = null,
        serviceType: String? = null,
        horarioLocal: String? = null,
        atiendeVirtual: Boolean? = null
    ) {
        println("🟢 ViewModel.updateProfile llamado")
        println("🟢 name=$name, email=$email, phone=$phone")
        println("🟢 profesion=$profesion, matricula=$matricula")
        
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            println("🟡 Estado: Loading...")
            
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                println("🟡 UserId: $userId")
                
                val currentProvider = providerRepository.getProviderByIdOnce(userId)
                    ?: throw Exception("Perfil no encontrado")
                println("🟡 Provider actual encontrado: ${currentProvider.name}")
                
                val updatedProvider = currentProvider.copy(
                    name = name ?: currentProvider.name,
                    apellido = apellido ?: currentProvider.apellido,
                    email = email ?: currentProvider.email,
                    phone = phone ?: currentProvider.phone,
                    description = description ?: currentProvider.description,
                    address = address ?: currentProvider.address,
                    dniCuit = dniCuit ?: currentProvider.dniCuit,
                    profesion = profesion ?: currentProvider.profesion,
                    tieneMatricula = tieneMatricula ?: currentProvider.tieneMatricula,
                    matricula = matricula,
                    provincia = provincia ?: currentProvider.provincia,
                    codigoPostal = codigoPostal ?: currentProvider.codigoPostal,
                    pais = pais ?: currentProvider.pais,
                    atencionUrgencias = atencionUrgencias ?: currentProvider.atencionUrgencias,
                    vaDomicilio = vaDomicilio ?: currentProvider.vaDomicilio,
                    turnosEnLocal = turnosEnLocal ?: currentProvider.turnosEnLocal,
                    direccionLocal = direccionLocal,
                    provinciaLocal = provinciaLocal,
                    codigoPostalLocal = codigoPostalLocal,
                    tieneEmpresa = tieneEmpresa ?: currentProvider.tieneEmpresa,
                    trabajaConOtros = trabajaConOtros ?: currentProvider.trabajaConOtros,
                    envios = envios ?: currentProvider.envios,
                    nombreEmpresa = nombreEmpresa,
                    cuitEmpresa = cuitEmpresa,
                    direccionEmpresa = direccionEmpresa,
                    serviceType = serviceType ?: currentProvider.serviceType,
                    horarioLocal = horarioLocal,
                    atiendeVirtual = atiendeVirtual ?: currentProvider.atiendeVirtual
                )
                
                // Actualizar en Room
                println("🟡 Actualizando en Room...")
                providerRepository.updateProvider(updatedProvider)
                
                // Actualizar en Firebase
                println("🟡 Actualizando en Firebase...")
                val updateData = hashMapOf<String, Any>(
                    "updatedAt" to System.currentTimeMillis()
                )
                
                // Solo actualizar campos que fueron pasados
                if (name != null) updateData["nombre"] = name
                if (apellido != null) updateData["apellido"] = apellido
                if (email != null) updateData["email"] = email
                if (phone != null) updateData["telefono"] = phone
                if (description != null) updateData["description"] = description
                if (address != null) updateData["direccion"] = address
                if (dniCuit != null) updateData["dniCuit"] = dniCuit
                if (profesion != null) updateData["profesion"] = profesion
                if (tieneMatricula != null) updateData["tieneMatricula"] = tieneMatricula
                if (matricula != null) updateData["matricula"] = matricula
                if (provincia != null) updateData["provincia"] = provincia
                if (codigoPostal != null) updateData["codigoPostal"] = codigoPostal
                if (pais != null) updateData["pais"] = pais
                if (atencionUrgencias != null) updateData["atencionUrgencias"] = atencionUrgencias
                if (vaDomicilio != null) updateData["vaDomicilio"] = vaDomicilio
                if (turnosEnLocal != null) updateData["turnosEnLocal"] = turnosEnLocal
                if (direccionLocal != null) updateData["direccionLocal"] = direccionLocal
                if (provinciaLocal != null) updateData["provinciaLocal"] = provinciaLocal
                if (codigoPostalLocal != null) updateData["codigoPostalLocal"] = codigoPostalLocal
                if (tieneEmpresa != null) updateData["tieneEmpresa"] = tieneEmpresa
                if (trabajaConOtros != null) updateData["trabajaConOtros"] = trabajaConOtros
                if (envios != null) updateData["envios"] = envios
                if (nombreEmpresa != null) updateData["nombreEmpresa"] = nombreEmpresa
                if (cuitEmpresa != null) updateData["cuitEmpresa"] = cuitEmpresa
                if (direccionEmpresa != null) updateData["direccionEmpresa"] = direccionEmpresa
                if (serviceType != null) updateData["serviceType"] = serviceType
                if (horarioLocal != null) updateData["horarioLocal"] = horarioLocal
                if (atiendeVirtual != null) updateData["atiendeVirtual"] = atiendeVirtual
                
                firestore.collection("usuarios")
                    .document(userId)
                    .update(updateData)
                    .await()
                
                // Si tiene empresa, crear o actualizar BusinessEntity
                if (updatedProvider.tieneEmpresa && 
                    !updatedProvider.nombreEmpresa.isNullOrBlank() &&
                    !updatedProvider.cuitEmpresa.isNullOrBlank()) {
                    
                    val businesses = businessRepository.getBusinessesByProvider(userId).first()
                    val existingBusiness = businesses.firstOrNull()
                    if (existingBusiness != null) {
                        // Actualizar business existente
                        val updatedBusiness = existingBusiness.copy(
                            nombreNegocio = updatedProvider.nombreEmpresa!!,
                            razonSocial = updatedProvider.nombreEmpresa!!,
                            cuitNegocio = updatedProvider.cuitEmpresa!!,
                            direccion = updatedProvider.direccionEmpresa!!,
                            codigoPostal = "", // TODO: agregar código postal de empresa
                            updatedAt = System.currentTimeMillis()
                        )
                        businessRepository.updateBusiness(updatedBusiness)
                        _businessId.value = existingBusiness.id
                        _bussinesEntity.value = existingBusiness
                    } else {
                        // Crear nuevo business
                        val newBusinessId = UUID.randomUUID().toString()
                        val newBusiness = BusinessEntity(
                            id = newBusinessId,
                            providerId = userId,
                            nombreNegocio = updatedProvider.nombreEmpresa!!,
                            razonSocial = updatedProvider.nombreEmpresa!!,
                            cuitNegocio = updatedProvider.cuitEmpresa!!,
                            direccion = updatedProvider.direccionEmpresa!!,
                            codigoPostal = "", // TODO: agregar código postal de empresa
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        businessRepository.saveBusiness(newBusiness)
                        _businessId.value = newBusinessId
                    }
                } else if (!updatedProvider.tieneEmpresa) {
                    // Si desactiva empresa, eliminar businessId
                    _businessId.value = null
                }
                
                println("✅ Perfil actualizado exitosamente con TODOS los campos")
                _updateState.value = UpdateState.Success
                _profileState.value = ProfileState.Success(updatedProvider)
            } catch (e: Exception) {
                println("❌ Error al actualizar: ${e.message}")
                e.printStackTrace()
                _updateState.value = UpdateState.Error(e.message ?: "Error al actualizar perfil")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }
    
    // Función para alternar entre modo Personal y Empresa
    fun toggleProfileMode() {
        _profileMode.update { currentMode ->
            if (currentMode == PrestadorProfileMode.PERSONAL) {
                PrestadorProfileMode.EMPRESA
            } else {
                PrestadorProfileMode.PERSONAL
            }
        }
    }

    fun updateImagenesProductos(json: String) {
        val business = _bussinesEntity.value ?: return
        val updated = business.copy(imagenesProductos = json)
        _bussinesEntity.value = updated
        viewModelScope.launch {
            businessRepository.updateBusiness(updated)
        }
    }

    fun updateCategorias(json: String) {
        val business = _bussinesEntity.value ?: return
        val updated = business.copy(categorias = json)
        _bussinesEntity.value = updated
        viewModelScope.launch {
            businessRepository.updateBusiness(updated)
        }
    }

    fun updateHorarioCasaCentral(horario: String) {
        val businees = _bussinesEntity.value?: return
        val updated = businees.copy(horario = horario.ifBlank { null })
        _bussinesEntity.value = updated
        viewModelScope.launch { businessRepository.updateBusiness(updated) }
    }

}


sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val provider: ProviderEntity) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}