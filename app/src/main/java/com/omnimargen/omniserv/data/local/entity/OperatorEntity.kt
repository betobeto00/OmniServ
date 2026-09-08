package com.omnimargen.omniserv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operators")
data class OperatorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val telefono: String,
    val especialidad: String = "",
    val activo: Boolean = true,
    val fechaCreacion: Long
)
