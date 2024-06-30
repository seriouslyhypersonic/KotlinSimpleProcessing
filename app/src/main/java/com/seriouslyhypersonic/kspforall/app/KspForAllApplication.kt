package com.seriouslyhypersonic.kspforall.app

import android.app.Application
import com.seriouslyhypersonic.kspforall.core.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class KspForAllApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(applicationContext)
            modules(AppModule().module)
        }
    }
}
