package com.seriouslyhypersonic.kspforall.demo.weather.data.location

import kotlinx.coroutines.flow.StateFlow

interface LocationService {
    val locations: StateFlow<List<Location>>
}
