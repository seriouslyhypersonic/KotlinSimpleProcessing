package com.seriouslyhypersonic.ksp.ui.preview

import com.seriouslyhypersonic.ksp.demo.weather.ClearSkiesWeatherOverviewViewModel
import com.seriouslyhypersonic.ksp.demo.weather.WeatherOverviewViewModel
import com.seriouslyhypersonic.library.preview.previewModule
import com.seriouslyhypersonic.library.preview.with

class PreviewModule

private val com_seriouslyhypersonic_kspforall_ui_preview_module = previewModule {
    preview { WeatherOverviewViewModel::class with { ClearSkiesWeatherOverviewViewModel } }
}

@Suppress("UnusedReceiverParameter")
val PreviewModule.module get() = com_seriouslyhypersonic_kspforall_ui_preview_module
