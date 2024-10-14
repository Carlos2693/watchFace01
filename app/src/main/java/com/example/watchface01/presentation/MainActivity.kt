/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.watchface01.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.watchface01.R
import com.example.watchface01.presentation.theme.WatchFace01Theme
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.wear.compose.material.MaterialTheme
import kotlinx.coroutines.delay
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Text as WearText
import androidx.compose.ui.Modifier
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("Android")
        }
    }
}

@Composable
fun WearApp(greetingName: String) {
    WatchFace01Theme {
        WearAppUI(city = "Guadalajara")
    }
}

// Add time display

@Composable
fun TimeDisplay() {
    val currentTime by produceState(initialValue = LocalTime.now()) {
        while (true) {
            value = LocalTime.now()
            delay(1000L)
        }
    }

    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    WearText(
        text = currentTime.format(formatter),
        style = TimeTextDefaults.timeTextStyle(
            fontSize = 50.sp
        ),
        color = MaterialTheme.colors.primary
    )
}

@Composable
fun WeatherDisplay(city: String) {
    var weatherState by remember { mutableStateOf<WeatherState>(WeatherState.Loading) }

    LaunchedEffect(city) {
        weatherState = try {
            val response = WeatherRepository.getWeather(city)
            WeatherState.Success(
                temperature = response.main.temp,
                condition = response.weather.firstOrNull()?.main ?: "Unknown"
            )
        } catch (e: Exception) {
            Log.e("WeatherDisplay", "Error fetching weather", e)
            WeatherState.Error("Failed to load weather: ${e.localizedMessage}")
        }
    }

    when (val state = weatherState) {
        is WeatherState.Loading -> LoadingView()
        is WeatherState.Success -> WeatherContent(state)
        is WeatherState.Error -> ErrorView(state.message)
    }
}


@Composable
private fun LoadingView() {
    CircularProgressIndicator(
        modifier = Modifier.size(36.dp),
        strokeWidth = 4.dp
    )

}

@Composable
private fun WeatherContent(state: WeatherState.Success) {
    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colors.primaryVariant.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WeatherIcon(state.condition)
                    Spacer(modifier = Modifier.height(8.dp))
                    WearText(
                        text = "${state.temperature.toInt()}°F",
                        style = MaterialTheme.typography.title2,
                        color = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    WearText(
                        text = state.condition,
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorView(message: String) {
    WearText(
        text = message,
        style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.error)
    )
}

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val temperature: Float, val condition: String) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

@Composable
fun WeatherIcon(
    condition: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.Unspecified
) {
    val iconResId = when (condition.lowercase()) {
        "rain", "drizzle", "shower rain" -> R.drawable.ic_rain
        "clear", "sunny" -> R.drawable.ic_sunny
        "clouds", "cloudy", "overcast" -> R.drawable.ic_cloudy
        "thunderstorm" -> R.drawable.ic_thunderstorm
        "snow" -> R.drawable.ic_snow
        "mist", "fog" -> R.drawable.ic_mist
        else -> R.drawable.ic_unknown
    }

    Log.d("WeatherIcon", "Condition: $condition, IconResId: $iconResId")

    Image(
        painter = painterResource(id = iconResId),
        contentDescription = "Weather condition: $condition",
        modifier = modifier.size(size),
        colorFilter = if (tint != Color.Unspecified) ColorFilter.tint(tint) else null
    )
}

@Composable
fun WearAppUI(
    city: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScalingLazyListState()

    Scaffold(
        timeText = {
            TimeText(
                timeTextStyle = TimeTextDefaults.timeTextStyle(
                    color = MaterialTheme.colors.onBackground
                )
            )
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = scrollState)
        }
    ) {
        ScalingLazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = scrollState
        ) {
            item { TimeDisplay() }
            item { WeatherDisplay(city = city) }
            item { BatteryStatus() }
            item { StepCounter() }
        }
    }
}

@Composable
fun BatteryStatus() {
    val batteryStatus by produceState(initialValue = 0) {
        value = getBatteryPercentage()
    }
    WearText(
        text = "Battery: $batteryStatus%",
        style = MaterialTheme.typography.body2
    )
}

@Composable
fun StepCounter() {
    val steps by produceState(initialValue = 0) {
        value = getStepCount()
    }
    WearText(
        text = "Steps: $steps",
        style = MaterialTheme.typography.body2
    )
}

// These functions would need to be implemented to get actual data
suspend fun getBatteryPercentage(): Int = withContext(Dispatchers.IO) {
    // Implement actual battery percentage retrieval
    75 // Placeholder
}

suspend fun getStepCount(): Int = withContext(Dispatchers.IO) {
    // Implement actual step count retrieval
    5000 // Placeholder
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}