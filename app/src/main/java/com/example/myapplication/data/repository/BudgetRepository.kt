package com.example.myapplication.data.repository

import com.example.myapplication.data.local.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE PRESUPUESTOS Y LICITACIONES ---
 * Esta clase es el corazón de la lógica de negocio para el Cliente.
 * Centraliza el acceso a los presupuestos locales y la sincronización con la nube.
 */
@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
    // Aquí inyectaremos FirebaseFirestore más adelante
) {

    // ==========================================================
    // 1. OBSERVABLES (FLUJOS DE DATOS EN TIEMPO REAL)
    // ==========================================================

    /**
     * Lista de todas las licitaciones creadas por el cliente.
     * Se actualiza automáticamente gracias a Flow.
     */
    val allTenders: Flow<List<TenderEntity>> = budgetDao.getAllTenders()

    /**
     * Lista de presupuestos "Varios" (sin licitación asociada).
     */
    val directBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllDirectBudgets()

    /**
     * Obtiene los presupuestos específicos de una licitación.
     * Útil para la pantalla de "Comparar Ofertas".
     */
    fun getBudgetsForTender(tenderId: String): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsForTender(tenderId)
    }

    /**
     * 🔥 [NUEVO] Obtiene la lista de licitaciones abiertas (No observable).
     * Útil para procesos de simulación o tareas puntuales.
     */
    suspend fun getOpenTenders(): List<TenderEntity> {
        return budgetDao.getOpenTenders()
    }

    // ==========================================================
    // 2. ACCIONES DE ESCRITURA (LOCAL + PREPARACIÓN FIREBASE)
    // ==========================================================

    /**
     * Guarda una nueva licitación.
     * Primero en Room para persistencia offline y luego se enviaría a Firebase.
     */
    suspend fun createNewTender(tender: TenderEntity) {
        // 1. Persistencia local inmediata
        budgetDao.insertTender(tender)

        // 2. TODO: INTEGRACIÓN FIREBASE
        // Enviar el objeto tender a la colección "tenders" en Firestore
        // firestore.collection("tenders").document(tender.tenderId).set(tender)
    }

    /**
     * Procesa un presupuesto recibido por el Chat o Licitación.
     * Maverick envía un JSON, la app lo parsea y este método lo guarda en Room.
     */
    suspend fun receiveBudgetFromChat(budget: BudgetEntity) {
        // Guardamos en Room para que esté disponible offline
        budgetDao.insertBudget(budget)

        // Si el presupuesto tiene un tenderId, el cliente recibirá una
        // notificación en su sección de Licitaciones automáticamente.
    }

    /**
     * Cambia el estado de un presupuesto (Aceptar/Rechazar).
     */
    suspend fun updateBudgetStatus(budgetId: String, newStatus: BudgetStatus) {
        val currentBudget = budgetDao.getBudgetById(budgetId)
        currentBudget?.let {
            val updatedBudget = it.copy(status = newStatus)
            budgetDao.updateBudgetStatus(updatedBudget)

            // TODO: INTEGRACIÓN FIREBASE
            // Notificar al prestador el cambio de estado
            // firestore.collection("budgets").document(budgetId).update("status", newStatus.name)
        }
    }

    /**
     * Borra una licitación localmente.
     */
    suspend fun removeTender(tenderId: String) {
        budgetDao.deleteTender(tenderId)
        // TODO: Borrar también en Firebase si es necesario
    }
}