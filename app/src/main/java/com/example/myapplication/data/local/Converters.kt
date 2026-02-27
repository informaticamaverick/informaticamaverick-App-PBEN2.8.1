package com.example.myapplication.data.local

import androidx.room.TypeConverter
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.AddressProvider
import com.example.myapplication.data.model.CompanyClient
import com.example.myapplication.data.model.CompanyProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.myapplication.data.model.MessageType
/**
 * --- CONVERTERS UNIVERSALES ---
 * Este archivo maneja TODAS las conversiones de datos complejos para:
 * - UserEntity
 * - ProviderEntity
 * - CategoryEntity
 * - BudgetEntity (Presupuestos - ¡NUEVO!)
 * - MessageEntity (Mensajes de Chats - ¡NUEVO!)
 *
 */
class Converters {
    private val gson = Gson()

    // ==========================================
    // 1. LISTAS DE TEXTO (String)
    // Sirve para: emails adicionales, fotos de galería, IDs de imágenes del presupuesto.
    // ==========================================
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // ==========================================
    // 2. MODELOS DE USUARIO (UserEntity)
    // ==========================================

    @TypeConverter
    fun fromCompanyClientList(value: List<CompanyClient>?): String {
        return gson.toJson(value ?: emptyList<CompanyClient>())
    }

    @TypeConverter
    fun toCompanyClientList(value: String?): List<CompanyClient> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<CompanyClient>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromAddressClientList(value: List<AddressClient>?): String {
        return gson.toJson(value ?: emptyList<AddressClient>())
    }

    @TypeConverter
    fun toAddressClientList(value: String?): List<AddressClient> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<AddressClient>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // ==========================================
    // 3. MODELOS DE PRESTADOR (ProviderEntity)
    // ==========================================

    @TypeConverter
    fun fromAddressProvider(value: AddressProvider?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAddressProvider(value: String?): AddressProvider? {
        return gson.fromJson(value, AddressProvider::class.java)
    }

    @TypeConverter
    fun fromAddressProviderList(value: List<AddressProvider>?): String {
        return gson.toJson(value ?: emptyList<AddressProvider>())
    }

    @TypeConverter
    fun toAddressProviderList(value: String?): List<AddressProvider> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<AddressProvider>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromCompanyProviderList(value: List<CompanyProvider>?): String {
        return gson.toJson(value ?: emptyList<CompanyProvider>())
    }

    @TypeConverter
    fun toCompanyProviderList(value: String?): List<CompanyProvider> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<CompanyProvider>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // ==========================================
    // 4. MODELOS DE PRESUPUESTO (BudgetEntity) - ¡AÑADIDO!
    // ==========================================

    // --- Convertir lista de Artículos/Productos ---
    @TypeConverter
    fun fromBudgetItemList(value: List<BudgetItem>?): String {
        // Convierte la lista de objetos a un texto JSON para la BD
        return gson.toJson(value ?: emptyList<BudgetItem>())
    }

    @TypeConverter
    fun toBudgetItemList(value: String?): List<BudgetItem> {
        // Convierte el texto JSON de la BD de vuelta a una lista de objetos para Kotlin
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetItem>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- Convertir lista de Servicios (Mano de Obra) ---
    @TypeConverter
    fun fromBudgetServiceList(value: List<BudgetService>?): String {
        return gson.toJson(value ?: emptyList<BudgetService>())
    }

    @TypeConverter
    fun toBudgetServiceList(value: String?): List<BudgetService> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetService>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- Convertir lista de Honorarios Profesionales ---
    @TypeConverter
    fun fromBudgetFeeList(value: List<BudgetProfessionalFee>?): String {
        return gson.toJson(value ?: emptyList<BudgetProfessionalFee>())
    }

    @TypeConverter
    fun toBudgetFeeList(value: String?): List<BudgetProfessionalFee> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetProfessionalFee>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- Convertir lista de Gastos Varios ---
    @TypeConverter
    fun fromBudgetMiscList(value: List<BudgetMiscExpense>?): String {
        return gson.toJson(value ?: emptyList<BudgetMiscExpense>())
    }

    @TypeConverter
    fun toBudgetMiscList(value: String?): List<BudgetMiscExpense> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetMiscExpense>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- Convertir lista de Impuestos Aplicados ---
    @TypeConverter
    fun fromBudgetTaxList(value: List<BudgetTax>?): String {
        return gson.toJson(value ?: emptyList<BudgetTax>())
    }

    @TypeConverter
    fun toBudgetTaxList(value: String?): List<BudgetTax> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<BudgetTax>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }


// ==========================================
// 5. MODELOS DE CHATS (MessageEntity) - ¡AÑADIDO!
// ==========================================
// --- Convertir MessageType a String (para guardar en DB) ---
@TypeConverter
fun fromMessageType(value: MessageType): String {
    return value.name // Guarda "IMAGE", "TEXT", etc.
}

    // --- Convertir String a MessageType (para leer de DB) ---
    @TypeConverter
    fun toMessageType(value: String): MessageType {
        return try {
            MessageType.valueOf(value)
        } catch (e: Exception) {
            MessageType.TEXT // Si falla, asumimos que es texto por seguridad
        }
    }

}