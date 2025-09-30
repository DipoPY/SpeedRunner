package com.dipo.data.model

import androidx.room.Entity

@Entity(
    tableName = "location_point",
    primaryKeys = ["sessionId", "time"]
)
data class LocationPointEntity(
    val sessionId: String,
    val time: Long,
    val lat: Double,
    val lon: Double,
    val accuracy: Float
)