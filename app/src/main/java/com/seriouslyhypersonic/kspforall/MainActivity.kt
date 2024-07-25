package com.seriouslyhypersonic.kspforall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.seriouslyhypersonic.kspforall.demo.weather.WeatherOverview
import com.seriouslyhypersonic.kspforall.demo.weather.data.location.LocationProviderUpdater
import com.seriouslyhypersonic.kspforall.ui.theme.KspForAllTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val locationUpdater: LocationProviderUpdater by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationUpdater.update()
        enableEdgeToEdge()

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
