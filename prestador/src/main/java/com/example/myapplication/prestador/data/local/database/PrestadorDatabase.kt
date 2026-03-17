package com.example.myapplication.prestador.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.prestador.data.local.dao.ClienteDao
import com.example.myapplication.prestador.data.local.dao.PresupuestoDao
import com.example.myapplication.prestador.data.local.dao.ProviderDao
import com.example.myapplication.prestador.data.local.dao.PromotionDao
import com.example.myapplication.prestador.data.local.dao.EmpleadoDao
import com.example.myapplication.prestador.data.local.dao.AvailabilityScheduleDao
import com.example.myapplication.prestador.data.local.dao.RentalSpaceDao
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.example.myapplication.prestador.data.local.entity.PromotionEntity
import com.example.myapplication.prestador.data.local.entity.EmpleadoEntity
import com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity
import com.example.myapplication.prestador.data.local.entity.RentalSpaceEntity
import com.example.myapplication.prestador.data.local.dao.AppointmentDao
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import com.example.myapplication.prestador.data.local.dao.BusinessDao
import com.example.myapplication.prestador.data.local.dao.SucursalDao
import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import com.example.myapplication.prestador.data.local.dao.MessageDao
import com.example.myapplication.prestador.data.local.dao.ConversationDao
import com.example.myapplication.prestador.data.local.entity.MessageEntity
import com.example.myapplication.prestador.data.local.entity.ConversationEntity
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.prestador.data.local.dao.PlantillaPresupuestoDao
import com.example.myapplication.prestador.data.local.entity.PlantillaPresupuestoEntity
import com.example.myapplication.prestador.data.local.dao.DireccionDao
import com.example.myapplication.prestador.data.local.dao.ReferenteDao
import com.example.myapplication.prestador.data.local.entity.DireccionEntity
import com.example.myapplication.prestador.data.local.entity.ReferenteEntity


/**
 * BASE DE DATOS DEL MÓDULO PRESTADOR
 */
@Database(
    entities = [
        PresupuestoEntity::class,
        ClienteEntity::class,
        ProviderEntity::class,
        PromotionEntity::class,
        AppointmentEntity::class,
        BusinessEntity::class,
        SucursalEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        EmpleadoEntity::class,
        AvailabilityScheduleEntity::class,
        RentalSpaceEntity::class,
        PlantillaPresupuestoEntity::class,
        DireccionEntity::class,
        ReferenteEntity::class,

   ],
    version = 29,
    exportSchema = true
)
abstract class PrestadorDatabase : RoomDatabase() {
    abstract fun presupuestoDao(): PresupuestoDao
    abstract fun clienteDao(): ClienteDao
    abstract fun providerDao(): ProviderDao
    abstract fun promotionDao(): PromotionDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun businessDao(): BusinessDao
    abstract fun sucursalDao(): SucursalDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun empleadoDao(): EmpleadoDao
    abstract fun availabilityScheduleDao(): AvailabilityScheduleDao
    abstract fun rentalSpaceDao(): RentalSpaceDao

    abstract fun plantillaPresupuestoDao(): PlantillaPresupuestoDao

    abstract fun direccionDao(): DireccionDao
    abstract fun referenteDao(): ReferenteDao
}
