package com.seriouslyhypersonic.kspforall.ui.preview

import com.seriouslyhypersonic.kspforall.weather.WeatherOverviewPreviewViewModel
import com.seriouslyhypersonic.kspforall.weather.WeatherOverviewViewModel
import com.seriouslyhypersonic.library.kotlin.previewModule
import com.seriouslyhypersonic.library.kotlin.with

class PreviewModule

private val com_seriouslyhypersonic_kspforall_ui_preview_module = previewModule {
    preview { WeatherOverviewViewModel::class with { WeatherOverviewPreviewViewModel.ClearSkies } }
}

@Suppress("UnusedReceiverParameter")
val PreviewModule.module get() = com_seriouslyhypersonic_kspforall_ui_preview_module
