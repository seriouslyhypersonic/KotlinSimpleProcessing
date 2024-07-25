package com.seriouslyhypersonic.ksp.app

import android.app.Application
import com.seriouslyhypersonic.ksp.core.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class KspApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(applicationContext)
            modules(AppModule().module)
        }
    }
}
