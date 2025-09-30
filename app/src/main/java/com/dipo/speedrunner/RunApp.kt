package com.dipo.speedrunner

import android.app.Application
import com.dipo.speedrunner.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class RunApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RunApp)
            modules(appModules())
        }
    }
}