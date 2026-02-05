package com.example.myapplication.data.local

import androidx.room.TypeConverter
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.CompanyClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // --- Converter para List<CompanyClient> ---
    @TypeConverter
    fun fromCompanyList(value: List<CompanyClient>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCompanyList(value: String?): List<CompanyClient> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<CompanyClient>>() {}.type
        return gson.fromJson(value, type)
    }

    // --- Converter para List<AddressClient> (Mis Direcciones personales) ---
    @TypeConverter
    fun fromAddressList(value: List<AddressClient>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAddressList(value: String?): List<AddressClient> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<AddressClient>>() {}.type
        return gson.fromJson(value, type)
    }

    // --- Converter para List<String> (Para listas simples como fotos o categorías) ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
}