package com.omnimargen.omniserv.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_types",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class ServiceTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val activo: Boolean = true,
    val fechaCreacion: Long
)
