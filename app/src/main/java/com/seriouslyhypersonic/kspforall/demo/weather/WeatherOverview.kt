package com.seriouslyhypersonic.kspforall.demo.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seriouslyhypersonic.kspforall.ui.preview.KspForAllPreview
import com.seriouslyhypersonic.kspforall.ui.theme.AppIcons

@Composable
fun WeatherOverview(
    modifier: Modifier = Modifier,
    viewModel: SomeWeatherOverviewViewModel = injectWeatherOverviewViewModel()
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.update()
    }

    WeatherSurface(simulation = viewModel.simulation, modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = viewModel.location,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row {
                Text(
                    text = viewModel.currentTemperature,
                    fontSize = 150.sp,
                    fontWeight = FontWeight.ExtraLight
                )

                Box {
                    Text(
                        text = "º",
                        fontSize = 150.sp,
                        fontWeight = FontWeight.ExtraLight
                    )

                    Text(
                        text = viewModel.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 10.dp, top = 115.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                Text(
                    text = viewModel.perceivedTemperature,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    SmallLabel(text = viewModel.maxTemperature, icon = AppIcons.ArrowUpward)
                    SmallLabel(text = viewModel.minTemperature, icon = AppIcons.ArrowDownward)
                }

            }
        }
    }
}

@Composable
private fun WeatherSurface(
    simulation: WeatherSimulation,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Scaffold { innerPadding ->
        Box(modifier) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(simulation.gradient)
            )

            CompositionLocalProvider(LocalContentColor provides simulation.foreground) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun SmallLabel(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview
@Composable
fun WeatherOverviewPreview() {
    KspForAllPreview {
        WeatherOverview()
    }
}
