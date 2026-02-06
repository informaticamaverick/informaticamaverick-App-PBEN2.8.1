package com.example.myapplication.data.local

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.model.fake.CategorySampleDataFalso // Tus datos de categorías
import com.example.myapplication.data.model.fake.PrestadorSampleDataFalso // Descomenta si usas prestadores falsos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * --- BASE DE DATOS MAESTRA ---
 * Centraliza la persistencia de Usuarios, Prestadores y Categorías.
 */
@Database(
    entities = [
        UserEntity::class,      // Tabla de Usuarios
        ProviderEntity::class,  // Tabla de Prestadores
        CategoryEntity::class   // Tabla de Categorías
    ],
    version = 5, // 🔥 [ACTUALIZADO] Versión 5 para habilitar la carga masiva de prestadores
    exportSchema = false
)
// Aquí registramos el convertidor que arreglamos en el paso anterior
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // --- DAOs: Accesos a tablas ---
    abstract fun userDao(): UserDao
    abstract fun providerDao(): ProviderDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia de la base de datos (Singleton).
         * @param scope El alcance de corrutina para operaciones de inicialización.
         */
        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addCallback(AppDatabaseCallback(scope)) 
                    .fallbackToDestructiveMigration() // 🔥 Limpia la base de datos si el esquema cambia
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * --- CALLBACK DE INICIALIZACIÓN ---
     * Se ejecuta cuando la base de datos se crea por primera vez.
     */
    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    
                    // ===================================================
                    // SECCIÓN 1: CARGA DE CATEGORÍAS
                    // ===================================================
                    val categoryDao = database.categoryDao()
                    val categoryEntities = CategorySampleDataFalso.categories.map { item ->
                        CategoryEntity(
                            name = item.name,
                            icon = item.icon,
                            // Convertimos Color -> Long aquí mismo
                            color = item.color.toArgb().toLong(),
                            superCategory = item.superCategory,
                            providerIds = item.providerIds,
                            imageUrl = item.imageUrl,
                            isNew = item.isNew,
                            isNewPrestador = item.isNewPrestador,
                            isAd = item.isAd
                        )
                    }
                    categoryDao.insertAll(categoryEntities)

                    // ===================================================
                    // SECCIÓN 2: 🔥 CARGA DE PRESTADORES (INCLUYE MAVERICK)
                    // 🔥 A futuro: Integrar Firebase Auth y Firestore para
                    // descargar prestadores reales y sincronizarlos aquí.
                    // ===================================================
                    val providerDao = database.providerDao()
                    
                    // Genera Maverick (ID 1001) + entre 5 y 20 por categoría
                    val sampleProviders = PrestadorSampleDataFalso.generate()
                    
                    // Persistencia real en Room
                    providerDao.insertAll(sampleProviders)
                }
            }
        }
    }
}
