package com.example.weathersovellus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathersovellus.ui.theme.WeatherSovellusTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherSovellusTheme {
                    WeatherScreen()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(modifier: Modifier = Modifier)
{
    val weatherApi = RetrofitInstance.api
    val scope = rememberCoroutineScope()
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Attempt to fetch data
                weatherData = weatherApi.getWeather(
                    city = "Helsinki",
                    apiKey = "ed30f6d6625cc717cb6fedeec7d12e00"
                )
                error = null // Clear errors if successful
            } catch (e: Exception) {
                // Log the error and set it to display
                error = e.message ?: "Unknown error occurred"
                weatherData = null // Ensure no stale data is shown
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather App", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
            )
        },
        content = { paddingValues -> // Apply Scaffold padding to the Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Apply padding here
                    .padding(16.dp), // Additional padding if needed
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    weatherData != null -> {
                        Text(text = "City: ${weatherData!!.name}", fontSize = 20.sp)
                        Text(text = "Temperature: ${weatherData!!.main.temp}°C", fontSize = 20.sp)
                        Text(text = "Wind Speed: ${weatherData!!.wind.speed} m/s", fontSize = 20.sp)
                    }
                    error != null -> {
                        Text(
                            text = "Error: $error",
                            fontSize = 20.sp,
                            color = androidx.compose.ui.graphics.Color.Red
                        )
                    }
                    else -> {
                        Text(text = "Loading...", fontSize = 20.sp)
                    }
                }
            }
        }
    )


    // UI layout


}
data class WeatherData(
    val name: String,
    val weather: List<WeatherDescription>,
    val main: Main,
    val wind: Wind
)
data class WeatherDescription(
    val description: String
)
data class Main(
    val temp: Double
)
data class Wind(
    val speed: Double
)

interface ApiService{
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String="metric"
    ): WeatherData
}
object RetrofitInstance {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

}
