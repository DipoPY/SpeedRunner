package com.dipo.data.repo

import androidx.room.withTransaction
import com.dipo.data.db.AppDb
import com.dipo.data.db.LocationPointDao
import com.dipo.data.db.RunSessionDao
import com.dipo.data.mapper.toDomain
import com.dipo.data.mapper.toEntity
import com.dipo.data.model.RunSessionEntity
import com.dipo.domain.entity.LocationPoint
import com.dipo.domain.entity.RunSession
import com.dipo.domain.entity.RunStats
import com.dipo.domain.repo.RunRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi

class RunRepositoryImpl(
    private val db: AppDb,
    private val runDao: RunSessionDao,
    private val pointDao: LocationPointDao,
) : RunRepository {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createSession(startTime: Long): String {
        val id = UUID.randomUUID().toString()
        val entity = RunSessionEntity(
            id = id,
            startTime = startTime,
            endTime = null,
            distanceMeters = 0.0,
            durationMillis = 0,
            paceSecPerKm = 0,
            previewPath = null
        )
        runDao.insert(entity)
        return id
    }

    override suspend fun closeSession(
        sessionId: String,
        endTime: Long,
        stats: RunStats?,
    ) {
        val distance = stats?.distanceMeters ?: 0.0
        val duration = stats?.durationMillis ?: 0L
        val pace = stats?.paceSecPerKm ?: 0
        runDao.close(sessionId, endTime, distance, duration, pace)
    }

    override suspend fun appendPoints(points: List<LocationPoint>) {
        if (points.isEmpty()) return
        db.withTransaction {
            pointDao.insertAll(points.map { it.toEntity() })
        }
    }

    override fun observeStats(sessionId: String): Flow<RunStats> = emptyFlow()

    override suspend fun getHistory(): List<RunSession> = runDao.history().map { it.toDomain() }

    override suspend fun getById(sessionId: String): RunSession {
        return runDao.byId(sessionId)?.toDomain() ?: error("Run not found: $sessionId")
    }

    override suspend fun updatePreview(sessionId: String, path: String) {
        runDao.updatePreview(sessionId, path)
    }

    override suspend fun getPolyline(sessionId: String): List<LocationPoint> {
        return pointDao.pointsBySession(sessionId).map { it.toDomain() }
    }
}
