package com.example.myapplication.Client

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * REPOSITORIO DE USUARIO (UserRepository)
 * 
 * Este componente es el intermediario entre la UI (ViewModels) y las fuentes de datos.
 * Utiliza Room como base de datos local y está preparado para integrarse con Firebase 🔥.
 * 
 * FUNCIONES:
 * 1. Leer los datos del usuario desde Room (Flujo reactivo).
 * 2. Guardar cambios localmente de forma permanente.
 * 3. Sincronizar con Firebase Firestore 🔥.
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    /**
     * Expone el perfil del usuario actual.
     * Al ser un Flow, la UI se actualiza automáticamente ante cualquier cambio en la BD.
     */
    val userProfile: Flow<UserEntity?> = userDao.getUser()

    /**
     * Guarda o actualiza los datos del usuario en la base de datos local (Room).
     * 🔥 En el futuro, esta función también enviará los datos a Firebase Firestore.
     */
    suspend fun updateUser(userEntity: UserEntity) {
        userDao.insertOrUpdateUser(userEntity)
        // 🔥 Lógica futura: firestore.collection("users").document(userEntity.id).set(userEntity)
    }

    /**
     * Inicializa los datos del usuario.
     * Si no hay un usuario en Room, carga los datos iniciales de 'UserSampleDataFalso'.
     * 🔥 En producción, esta función descargará el perfil desde Firebase si el usuario está autenticado.
     */
    suspend fun refreshUserFromRemote() {
        try {
            // Obtenemos el primer valor emitido por el Flow de Room
            val existingUser = userDao.getUser().first()
            
            if (existingUser == null) {
                // Si la BD está vacía, cargamos los datos de prueba adaptados
                val fakeUser = UserSampleDataFalso.currentUser
                
                val userEntity = UserEntity(
                    id = fakeUser.id,
                    username = fakeUser.username,
                    name = fakeUser.name,
                    lastName = fakeUser.lastName,
                    matricula = fakeUser.matricula,
                    titulo = fakeUser.titulo,
                    emails = fakeUser.emails.toList(),
                    phones = fakeUser.phones.toList(),
                    profileImageUrl = fakeUser.profileImageUrl?.toString(),
                    bannerImageUrl = fakeUser.bannerImageUrl?.toString(),
                    personalAddresses = fakeUser.personalAddresses.toList(),
                    hasCompanyProfile = fakeUser.hasCompanyProfile,
                    isSubscribed = fakeUser.isSubscribed,
                    isVerified = fakeUser.isVerified,
                    isOnline = fakeUser.isOnline,
                    isFavorite = fakeUser.isFavorite,
                    rating = fakeUser.rating,
                    companies = fakeUser.companies.toList()
                )
                
                userDao.insertOrUpdateUser(userEntity)
                // 🔥 Lógica futura: Obtener datos reales de Firebase Firestore
            }
        } catch (e: Exception) {
            // Manejar posibles errores de lectura
        }
    }

    /**
     * Borra los datos locales del usuario. Útil para cerrar sesión.
     * 🔥 También debería cerrar la sesión en Firebase Auth.
     */
    suspend fun clearLocalUser() {
        userDao.deleteUser()
    }
}
