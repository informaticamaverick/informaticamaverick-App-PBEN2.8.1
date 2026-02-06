package com.example.myapplication.data.local

import androidx.room.TypeConverter
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.AddressProvider
import com.example.myapplication.data.model.CompanyClient
import com.example.myapplication.data.model.CompanyProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * --- CONVERTERS UNIVERSALES ---
 * Este archivo maneja TODAS las conversiones de datos complejos para:
 * - UserEntity
 * - ProviderEntity
 * - CategoryEntity
 */
class Converters {
    private val gson = Gson()

    // ==========================================
    // 1. LISTAS DE TEXTO (String)
    // Sirve para: additionalEmails, galleryImages, providerIds, etc.
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

    // --- List<CompanyClient> ---
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

    // --- List<AddressClient> ---
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

    // --- AddressProvider (Objeto Individual) ---
    @TypeConverter
    fun fromAddressProvider(value: AddressProvider?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAddressProvider(value: String?): AddressProvider? {
        return gson.fromJson(value, AddressProvider::class.java)
    }

    // --- List<AddressProvider> (Lista de Direcciones) ---
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

    // --- List<CompanyProvider> ---
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
}



