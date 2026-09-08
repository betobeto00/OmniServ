package com.omnimargen.omniserv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val telefono: String,
    val direccion: String,
    val fechaCreacion: Long
)
