package com.seriouslyhypersonic.kspforall.weather.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

class MockWeatherService : WeatherService {
    override suspend fun fetchForecast() = withContext(Dispatchers.IO) {
        delay(1.seconds)
        Forecasts.random()
    }

    companion object {
        val Forecasts = listOf(
            WeatherForecast(
                location = "Lisbon",
                temperature = WeatherForecast
                    .Temperature(current = 19, perceived = 20, max = 25, min = 17),
                description = WeatherForecast.Description.ClearSkies
            )
        )
    }
}
