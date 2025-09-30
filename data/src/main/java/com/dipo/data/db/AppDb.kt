package com.dipo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dipo.data.model.LocationPointEntity
import com.dipo.data.model.RunSessionEntity

@Database(
    entities = [RunSessionEntity::class, LocationPointEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDb : RoomDatabase() {
    abstract fun runSessionDao(): RunSessionDao
    abstract fun locationPointDao(): LocationPointDao
}