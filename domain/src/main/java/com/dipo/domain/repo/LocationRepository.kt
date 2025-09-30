package com.dipo.domain.repo

import com.dipo.domain.entity.LocationPoint
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun locationFlow(): Flow<LocationPoint>
}