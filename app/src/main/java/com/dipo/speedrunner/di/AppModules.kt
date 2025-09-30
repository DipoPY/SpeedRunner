package com.dipo.speedrunner.di

import com.dipo.data.di.dataModule
import com.dipo.domain.usecase.AppendLocationPoint
import com.dipo.domain.usecase.ComputeStats
import com.dipo.domain.usecase.EnqueuePreviewGeneration
import com.dipo.domain.usecase.GetRunById
import com.dipo.domain.usecase.GetRunHistory
import com.dipo.domain.usecase.StartRunSession
import com.dipo.domain.usecase.StopRunSession
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun appModules() = listOf(
    dataModule(),
    module {
        single { ComputeStats() }
        single { StartRunSession(get()) }
        single { StopRunSession(get(), get()) }
        single { AppendLocationPoint(get()) }
        single { GetRunById(get()) }
        single { GetRunHistory(get()) }
        single { EnqueuePreviewGeneration(get(), get()) }

        single(named("STATIC_MAPS_API_KEY")) { com.dipo.speedrunner.BuildConfig.STATIC_MAPS_API_KEY }
    },
)
