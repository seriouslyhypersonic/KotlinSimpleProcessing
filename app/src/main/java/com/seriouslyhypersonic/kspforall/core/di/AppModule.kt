package com.seriouslyhypersonic.kspforall.core.di

import com.seriouslyhypersonic.kspforall.demo.weather.data.MockWeatherService
import com.seriouslyhypersonic.kspforall.demo.weather.data.WeatherService
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.seriouslyhypersonic.kspforall")
class AppModule {
    @Single
    fun weatherService(): WeatherService = MockWeatherService()
}
