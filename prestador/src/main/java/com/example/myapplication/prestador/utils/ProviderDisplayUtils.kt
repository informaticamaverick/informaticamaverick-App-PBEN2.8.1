package com.example.myapplication.prestador.utils

import com.example.myapplication.prestador.data.PPrestadorProfileFalso
import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import com.example.myapplication.prestador.data.local.entity.DireccionEntity
import com.example.myapplication.prestador.data.local.entity.ProviderEntity

fun DireccionEntity.formatInline(): String {
    val calleNumero = listOfNotNull(calle?.takeIf { it.isNotBlank() }, numero?.takeIf { it.isNotBlank() })
        .joinToString(" ")
        .trim()
    val locProv = listOfNotNull(localidad?.takeIf { it.isNotBlank() }, provincia?.takeIf { it.isNotBlank() })
        .joinToString(", ")
        .trim()
    val cp = codigoPostal?.takeIf { it.isNotBlank() }

    return listOfNotNull(
        calleNumero.takeIf { it.isNotBlank() },
        locProv.takeIf { it.isNotBlank() },
        cp
    ).joinToString(" • ").trim()
}

fun ProviderEntity.displayCompanyOrFullName(business: BusinessEntity? = null): String {
    val company = nombreEmpresa?.takeIf { it.isNotBlank() }
        ?: business?.nombreNegocio?.takeIf { it.isNotBlank() }

    if (tieneEmpresa && company != null) return company

    val fullName = (name + " " + (apellido ?: "")).trim()
    return fullName.ifBlank { "Prestador" }
}

fun ProviderEntity.displayAddress(business: BusinessEntity? = null): String {
    return when {
        tieneEmpresa && business != null && business.direccion.isNotBlank() -> business.direccion
        tieneEmpresa && !direccionEmpresa.isNullOrBlank() -> direccionEmpresa!!
        turnosEnLocal && !direccionLocal.isNullOrBlank() -> direccionLocal!!
        !address.isNullOrBlank() -> address!!
        else -> ""
    }
}

fun ProviderEntity.toPrestadorProfileFalso(business: BusinessEntity? = null): PPrestadorProfileFalso {
    val company = nombreEmpresa?.takeIf { it.isNotBlank() }
        ?: business?.nombreNegocio?.takeIf { it.isNotBlank() }

    return PPrestadorProfileFalso(
        id = id,
        name = name.ifBlank { "Prestador" },
        lastName = apellido.orEmpty(),
        profileImageUrl = imageUrl.orEmpty(),
        bannerImageUrl = null,
        rating = rating,
        services = emptyList(),
        companyName = company,
        address = displayAddress(business),
        email = email,
        doesHomeVisits = vaDomicilio,
        hasPhysicalLocation = turnosEnLocal || (tieneEmpresa && (!direccionEmpresa.isNullOrBlank() || (business != null && business.direccion.isNotBlank()))),
        works24h = false,
        galleryImages = emptyList(),
        isFavorite = favorito,
        isVerified = verificado,
        isOnline = false,
        isSubscribed = suscripto
    )
}
