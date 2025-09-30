package com.dipo.data.mapper

import com.dipo.data.model.LocationPointEntity
import com.dipo.data.model.RunSessionEntity
import com.dipo.domain.entity.LocationPoint
import com.dipo.domain.entity.RunSession

fun RunSessionEntity.toDomain() = RunSession(
    id = id,
    startTime = startTime,
    endTime = endTime,
    distanceMeters = distanceMeters,
    durationMillis = durationMillis,
    paceSecPerKm = paceSecPerKm,
    previewPath = previewPath,
)

fun LocationPointEntity.toDomain() = LocationPoint(
    sessionId = sessionId,
    time = time,
    lat = lat,
    lon = lon,
    accuracy = accuracy
)
fun LocationPoint.toEntity() = LocationPointEntity(
    sessionId = sessionId,
    time = time,
    lat = lat,
    lon = lon,
    accuracy = accuracy
)

