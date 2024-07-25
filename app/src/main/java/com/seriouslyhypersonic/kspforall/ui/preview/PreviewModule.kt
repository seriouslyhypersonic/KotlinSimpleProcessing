package com.seriouslyhypersonic.kspforall.ui.preview

import com.seriouslyhypersonic.kspforall.demo.weather.ClearSkiesWeatherOverviewViewModel
import com.seriouslyhypersonic.kspforall.demo.weather.WeatherOverviewViewModel
import com.seriouslyhypersonic.library.kotlin.preview.previewModule
import com.seriouslyhypersonic.library.kotlin.preview.with

class PreviewModule

private val com_seriouslyhypersonic_kspforall_ui_preview_module = previewModule {
    preview { WeatherOverviewViewModel::class with { ClearSkiesWeatherOverviewViewModel } }
}

@Suppress("UnusedReceiverParameter")
val PreviewModule.module get() = com_seriouslyhypersonic_kspforall_ui_preview_module
