package com.omnimargen.omniserv.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_operators",
    foreignKeys = [
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OperatorEntity::class,
            parentColumns = ["id"],
            childColumns = ["operatorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["serviceId", "operatorId"], unique = true),
        Index(value = ["serviceId"]),
        Index(value = ["operatorId"])
    ]
)
data class ServiceOperatorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serviceId: Long,
    val operatorId: Long,
    val montoPago: Double = 0.0
)
