package com.omnimargen.omniserv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "license")
data class LicenseEntity(
    @PrimaryKey
    val id: Int = 1,
    val licenseKey: String,
    val empresaId: String,
    val fechaEmision: Long,
    val fechaExpiracion: Long,
    val modulosActivos: String,
    val firma: String
)
