package com.dipo.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "run_session",
    indices = [
        Index("startTime"),
        Index("endTime")
    ]
)
data class RunSessionEntity(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long?,
    val distanceMeters: Double,
    val durationMillis: Long,
    val paceSecPerKm: Int,
    val previewPath: String?,
)