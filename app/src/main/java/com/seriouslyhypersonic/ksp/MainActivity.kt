package com.seriouslyhypersonic.ksp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.seriouslyhypersonic.ksp.demo.weather.WeatherOverview
import com.seriouslyhypersonic.ksp.demo.weather.data.location.LocationProviderUpdater
import com.seriouslyhypersonic.ksp.demo.weather.data.location.LocationService
import com.seriouslyhypersonic.ksp.ui.theme.KspForAllTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val locationUpdater: LocationProviderUpdater by inject()
    private val locationService: LocationService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationUpdater.update()
        enableEdgeToEdge()

        lifecycleScope.launch {
            locationService.locations.flowWithLifecycle(lifecycle).collect {
                Log.e("NUNO", "Locations: $it")
            }
        }

        setContent {
            KspForAllTheme {
                WeatherOverview(modifier = Modifier)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KspForAllTheme {
        Greeting("Android")
    }
}
