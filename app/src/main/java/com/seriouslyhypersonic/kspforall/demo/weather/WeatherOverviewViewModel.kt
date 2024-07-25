package com.seriouslyhypersonic.kspforall.demo.weather

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seriouslyhypersonic.annotations.Previewable
import com.seriouslyhypersonic.kspforall.demo.weather.WeatherSimulation.Companion.simulationFor
import com.seriouslyhypersonic.kspforall.demo.weather.data.WeatherService
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@Previewable
@KoinViewModel
class WeatherOverviewViewModel(
    private val service: WeatherService
) : SomeWeatherOverviewViewModel, ViewModel() {
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

val ClearSkiesWeatherOverviewViewModel = WeatherOverviewPreviewViewModel(
    location = "Cascais",
    currentTemperature = "23",
    description = "Sunny",
    perceivedTemperature = "Currently feels like 25º",
    maxTemperature = "31",
    minTemperature = "19",
    simulation = WeatherSimulation.ClearSkies
)
