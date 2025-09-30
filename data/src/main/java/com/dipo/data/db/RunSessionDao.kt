package com.dipo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dipo.data.model.RunSessionEntity

@Dao
interface RunSessionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: RunSessionEntity)

    @Query("UPDATE run_session SET endTime=:endTime, distanceMeters=:distanceMeters, durationMillis=:durationMillis, paceSecPerKm=:paceSecPerKm WHERE id=:id")
    suspend fun close(
        id: String,
        endTime: Long,
        distanceMeters: Double,
        durationMillis: Long,
        paceSecPerKm: Int,
    )

    @Query("UPDATE run_session SET previewPath=:path WHERE id=:id")
    suspend fun updatePreview(id: String, path: String)

    @Query("SELECT * FROM run_session ORDER BY startTime DESC")
    suspend fun history(): List<RunSessionEntity>

    @Query("SELECT * FROM run_session WHERE id=:id")
    suspend fun byId(id: String): RunSessionEntity?
}
