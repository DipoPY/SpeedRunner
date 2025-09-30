package com.dipo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dipo.data.model.LocationPointEntity

@Dao
interface LocationPointDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(points: List<LocationPointEntity>): List<Long>

    @Query("SELECT * FROM location_point WHERE sessionId = :sessionId ORDER BY time ASC")
    suspend fun pointsBySession(sessionId: String): List<LocationPointEntity>
}