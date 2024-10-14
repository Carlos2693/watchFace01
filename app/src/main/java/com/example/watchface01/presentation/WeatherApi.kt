package com.example.watchface01.presentation


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.watchface01.BuildConfig

// Define the API service
interface WeatherApi {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial" // Fahrenheit
    ): WeatherResponse
}

// Data classes for API response
data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>
)

data class Main(val temp: Float)
data class Weather(val main: String, val description: String)

// Retrofit instance creation (can also be placed elsewhere)
object RetrofitInstance {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    // Access the API using Retrofit
    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}

// WeatherRepository for calling the API
object WeatherRepository {
    // Call this method to get the weather data
    suspend fun getWeather(city: String): WeatherResponse {
        // Retrieve the API key from BuildConfig
        val apiKey = BuildConfig.WEATHER_API_KEY

        // Make the API call using the Retrofit instance
        return RetrofitInstance.api.getWeather(city, apiKey)
    }
}
