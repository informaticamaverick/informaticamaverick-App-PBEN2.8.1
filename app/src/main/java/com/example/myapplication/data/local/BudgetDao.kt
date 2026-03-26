package com.example.myapplication.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO DE PRESUPUESTOS Y LICITACIONES ---
 * Define las consultas SQL para manejar la lógica comercial en la App Cliente.
 */
@Dao
interface BudgetDao {

    // ==========================================
    // 1. GESTIÓN DE LICITACIONES (Tenders)
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTender(tender: TenderEntity)

    /**
     * Obtiene todas las licitaciones que el cliente ha creado.
     * Usamos Flow para que la UI se actualice sola si llega una nueva.
     */
    @Query("SELECT * FROM tenders ORDER BY dateTimestamp DESC")
    fun getAllTenders(): Flow<List<TenderEntity>>

    /**
     * 🔥 [NUEVO] Obtiene solo las licitaciones con estado ABIERTA.
     * Utilizado por el motor de simulación para generar respuestas.
     */
    @Query("SELECT * FROM tenders WHERE status = 'ABIERTA'")
    suspend fun getOpenTenders(): List<TenderEntity>

    @Query("DELETE FROM tenders WHERE tenderId = :tId")
    suspend fun deleteTender(tId: String)

    /**
     * 🔥 NUEVO: Filtra licitaciones ABIERTAS que coincidan con la categoría del prestador.
     * Ejemplo: Si providerCategory es "Electricista", solo trae licitaciones de electricidad.
     */
    @Query("SELECT * FROM tenders WHERE category = :providerCategory AND status = 'ABIERTA' ORDER BY dateTimestamp DESC")
    fun getOpenTendersByCategory(providerCategory: String): Flow<List<TenderEntity>>

    // ==========================================
    // 2. GESTIÓN DE PRESUPUESTOS (Budgets)
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    /**
     * Obtiene presupuestos "Varios" (los que llegan por chat sin licitación).
     */
    @Query("SELECT * FROM budgets WHERE tenderId IS NULL ORDER BY dateTimestamp DESC")
    fun getAllDirectBudgets(): Flow<List<BudgetEntity>>

    /**
     * Obtiene todos los presupuestos recibidos para una licitación específica.
     * Esto permite al cliente comparar los montos totales (grandTotal).
     */
    @Query("SELECT * FROM budgets WHERE tenderId = :tId ORDER BY grandTotal ASC")
    fun getBudgetsForTender(tId: String): Flow<List<BudgetEntity>>

    /**
     * Busca un presupuesto específico por su ID.
     */
    @Query("SELECT * FROM budgets WHERE budgetId = :bId")
    suspend fun getBudgetById(bId: String): BudgetEntity?

    /**
     * Actualiza el estado del presupuesto (Ej: ACEPTADO, RECHAZADO).
     */
    @Update
    suspend fun updateBudgetStatus(budget: BudgetEntity)
}
