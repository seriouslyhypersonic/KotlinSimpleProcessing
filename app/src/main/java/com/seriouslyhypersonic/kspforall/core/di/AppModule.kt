package com.seriouslyhypersonic.kspforall.core.di

import com.seriouslyhypersonic.kspforall.weather.domain.MockWeatherService
import com.seriouslyhypersonic.kspforall.weather.domain.WeatherService
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.seriouslyhypersonic.kspforall")
class AppModule {
    @Single
    fun weatherService(): WeatherService = MockWeatherService()
}
