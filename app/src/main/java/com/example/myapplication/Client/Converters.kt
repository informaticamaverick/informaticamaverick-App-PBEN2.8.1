package com.example.myapplication.Client

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * CONVERTIDORES DE TIPO (TypeConverters)
 * 
 * Room solo sabe guardar tipos básicos (Int, String, Boolean). 
 * Estas funciones permiten guardar objetos complejos (como Listas de Direcciones o Empresas) 
 * convirtiéndolos a formato JSON automáticamente al guardar y de vuelta a Objetos al leer.
 */
class Converters {
    private val gson = Gson()

    // --- CONVERTIDORES PARA LISTAS DE STRINGS (Emails, Teléfonos) ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String? = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    // --- CONVERTIDORES PARA LISTAS DE DIRECCIONES ---
    @TypeConverter
    fun fromAddressList(value: List<Address>?): String? = gson.toJson(value)

    @TypeConverter
    fun toAddressList(value: String?): List<Address>? {
        val listType = object : TypeToken<List<Address>>() {}.type
        return gson.fromJson(value, listType)
    }

    // --- CONVERTIDORES PARA LISTAS DE EMPRESAS ---
    @TypeConverter
    fun fromCompanyList(value: List<Company>?): String? = gson.toJson(value)

    @TypeConverter
    fun toCompanyList(value: String?): List<Company>? {
        val listType = object : TypeToken<List<Company>>() {}.type
        return gson.fromJson(value, listType)
    }
}
