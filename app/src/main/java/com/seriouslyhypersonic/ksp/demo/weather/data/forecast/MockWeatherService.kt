package com.seriouslyhypersonic.ksp.demo.weather.data.forecast

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds

@Single
class MockWeatherService : WeatherService {
    override suspend fun fetchForecast() = withContext(Dispatchers.IO) {
        delay(1.seconds)
        Forecasts.random()
    }

    companion object {
        val Forecasts = listOf(
            WeatherForecast(
                location = "Lisbon",
                temperature = WeatherForecast.Temperature(
                    current = 19,
                    perceived = 20,
                    max = 25,
                    min = 17
                ),
                description = WeatherForecast.Description.ClearSkies
            )
        )
    }
}