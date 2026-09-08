package com.omnimargen.omniserv.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "services",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ServiceTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceTypeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["clienteId"]),
        Index(value = ["serviceTypeId"]),
        Index(value = ["fechaServicio"]),
        Index(value = ["estado"])
    ]
)
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clienteId: Long,
    val serviceTypeId: Long? = null,
    val tipoServicio: String,
    val fechaServicio: Long,
    val monto: Double,
    val estado: String = "PENDIENTE",
    val notas: String = "",
    val fechaCreacion: Long
)
