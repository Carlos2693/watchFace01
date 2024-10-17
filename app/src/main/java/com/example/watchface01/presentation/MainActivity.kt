/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.watchface01.presentation

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)


        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContent {
            WearApp("Android")
        }
    }


}

@Composable
fun WearApp(greetingName: String) {
    WatchFace01Theme {
        WearAppUI(city = "Inverness")
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

    // Format the time to separate hours and minutes
    val hourFormatter = remember { DateTimeFormatter.ofPattern("HH") }
    val minuteFormatter = remember { DateTimeFormatter.ofPattern("mm") }

    Column(
        modifier = Modifier
            .offset(x = 20.dp)
            .wrapContentWidth(),
        horizontalAlignment = Alignment.CenterHorizontally, // Center align hour and minute
        verticalArrangement = Arrangement.Center
    ) {
        // Display the hour
        WearText(
            text = currentTime.format(hourFormatter),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            style = TimeTextDefaults.timeTextStyle(
                fontSize = 50.sp // Large font for hour
            ),
            color = Color(0xFF00D9FF)
        )
        // Display the minute
        WearText(
            text = currentTime.format(minuteFormatter),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            style = TimeTextDefaults.timeTextStyle(
                fontSize = 40.sp // Smaller font for minute
            ),
            color = Color.White
        )
    }
}

@Composable
fun WeatherDisplay(city: String) {
    var weatherState by remember { mutableStateOf<WeatherState>(WeatherState.Loading) }
    val currentDate by remember { mutableStateOf(LocalDate.now()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") } // Date format like Tue, Oct 16

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
        is WeatherState.Success -> WeatherContent(state, currentDate.format(dateFormatter))
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
private fun WeatherContent(state: WeatherState.Success, formattedDate: String) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Time on the left (assuming this stays unchanged)
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            // Your time content here
        }

        // Date and Weather on the Right
        Column(
            modifier = Modifier.padding(start = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Date
            WearText(
                text = formattedDate,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Row for weather icon and temperature next to each other
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Weather Icon on the left
                WeatherIcon(state.condition)

                Spacer(modifier = Modifier.width(8.dp))

                // Temperature on the right of the icon
                WearText(
                    text = "${state.temperature.toInt()}°F",
                    style = MaterialTheme.typography.title2,
                    color = MaterialTheme.colors.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Weather Condition Text below the row
            WearText(
                text = state.condition,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onBackground
            )
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
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time on the left
            TimeDisplay()

            // Spacer to adjust the layout
            Spacer(modifier = Modifier.width(8.dp))

            // Weather on the right
            WeatherDisplay(city = city)
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

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = false)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}