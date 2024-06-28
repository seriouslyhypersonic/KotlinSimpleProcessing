package com.seriouslyhypersonic.kspforall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.seriouslyhypersonic.annotations.CaseDetection
import com.seriouslyhypersonic.kspforall.ui.theme.KspForAllTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KspForAllTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
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

@CaseDetection
enum class Direction {
    Up, Down, Left, Right
}

@CaseDetection
enum class TextAlignment {
    FLUSH_LEFT, CENTER_ALIGNED, FLUSH_RIGHT, JUSTIFIED
}

@CaseDetection
sealed class Device(val brand: String) {
    class Laptop(brand: String) : Device(brand)
    class Smartphone(brand: String) : Device(brand)
    class Tablet(brand: String) : Device(brand)
}

@CaseDetection
sealed interface Vehicle {
    val powertrain: String

    class Bike(override val powertrain: String) : Vehicle
    object Bycicle : Vehicle {
        override val powertrain = "Leg-power"
    }
}
