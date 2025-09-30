package com.dipo.data.di

import android.content.Context
import androidx.room.Room
import com.dipo.data.db.AppDb
import com.dipo.data.db.LocationPointDao
import com.dipo.data.db.RunSessionDao
import com.dipo.data.net.HttpClientFactory
import com.dipo.data.net.YandexStaticApi
import com.dipo.data.net.YandexStaticApiImpl
import com.dipo.data.repo.LocationRepositoryImpl
import com.dipo.data.repo.PreviewRepositoryImpl
import com.dipo.data.repo.RunRepositoryImpl
import com.dipo.domain.repo.LocationRepository
import com.dipo.domain.repo.PreviewRepository
import com.dipo.domain.repo.RunRepository
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun dataModule() = module {

    // Room
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDb::class.java,
            "run.db"
        ).fallbackToDestructiveMigration()
            .build()
    }
    single<RunSessionDao> { get<AppDb>().runSessionDao() }
    single<LocationPointDao> { get<AppDb>().locationPointDao() }

    single { HttpClientFactory.provideOkHttpClient(debug = isDebug(androidContext())) }

    single<YandexStaticApi> { YandexStaticApiImpl() }

    single<RunRepository> { RunRepositoryImpl(get(), get(), get()) }
    single<LocationRepository> { LocationRepositoryImpl(androidContext(), get()) }
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }
    single<PreviewRepository> {
        val apiKey: String = "9046c20f-bf42-4bc4-b1ff-a24f6d396c68"
        PreviewRepositoryImpl(
            appContext = androidContext(),
            okHttp = get(),
            staticApi = get(),
            staticApiKey = apiKey
        )
    }
}

private fun isDebug(ctx: Context): Boolean {
    return try {
        val appInfo = ctx.applicationInfo
        (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    } catch (_: Throwable) { false }
}
