package com.seriouslyhypersonic.kspforall.demo.weather.data.location

import android.content.Context
import com.seriouslyhypersonic.library.content.observeValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Single

@Single
class ContentProviderLocationService(
    context: Context,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : LocationService {
    override val locations = context.contentResolver
        .observeValues(LocationContract)
        .stateIn(scope, started = SharingStarted.Lazily, initialValue = emptyList())
}
