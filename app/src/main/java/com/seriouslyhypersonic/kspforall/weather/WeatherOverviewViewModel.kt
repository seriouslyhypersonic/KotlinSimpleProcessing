package com.seriouslyhypersonic.kspforall.weather

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seriouslyhypersonic.kspforall.weather.WeatherSimulation.Companion.simulationFor
import com.seriouslyhypersonic.kspforall.weather.domain.WeatherService
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel(binds = [SomeWeatherOverviewViewModel::class])
class WeatherOverviewViewModel(
    private val service: WeatherService
) : SomeWeatherOverviewViewModel() {
    override var location by mutableStateOf("--")
        private set

    override var currentTemperature by mutableStateOf("--")
        private set

    override var description by mutableStateOf("--")
        private set

    override var perceivedTemperature by mutableStateOf("--")
        private set

    override var maxTemperature by mutableStateOf("--")
        private set

    override var minTemperature by mutableStateOf("--")
        private set

    override var simulation by mutableStateOf(WeatherSimulation.ClearSkies)
        private set

    override fun update() {
        viewModelScope.launch {
            val forecast = service.fetchForecast()
            location = forecast.location
            currentTemperature = forecast.temperature.current.toString()
            description = forecast.description.message
            perceivedTemperature = "Currently feels like ${forecast.temperature.perceived}º"
            maxTemperature = "${forecast.temperature.max}º"
            minTemperature = "${forecast.temperature.min}º"
            simulation = simulationFor(forecast.description)
        }
    }
}

abstract class SomeWeatherOverviewViewModel : ViewModel() {
    abstract val location: String
    abstract val currentTemperature: String
    abstract val description: String
    abstract val perceivedTemperature: String
    abstract val maxTemperature: String
    abstract val minTemperature: String
    abstract val simulation: WeatherSimulation

    abstract fun update()
}

data class WeatherOverviewPreviewViewModel(
    override val location: String,
    override val currentTemperature: String,
    override val description: String,
    override val perceivedTemperature: String,
    override val maxTemperature: String,
    override val minTemperature: String,
    override val simulation: WeatherSimulation
) : SomeWeatherOverviewViewModel() {
    override fun update() { /* no-op */
    }

    companion object {
        val ClearSkies = WeatherOverviewPreviewViewModel(
            location = "Cascais",
            currentTemperature = "23",
            description = "Sunny",
            perceivedTemperature = "Currently feels like 25º",
            maxTemperature = "31",
            minTemperature = "19",
            simulation = WeatherSimulation.ClearSkies
        )
    }
}
