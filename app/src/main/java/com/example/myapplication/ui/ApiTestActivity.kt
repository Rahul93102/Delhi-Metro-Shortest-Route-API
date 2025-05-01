package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.MetroApiServer
import com.example.myapplication.data.api.SimpleMetroApiServer
import com.example.myapplication.data.model.MetroLine
import com.example.myapplication.data.model.Route
import com.example.myapplication.data.model.Station
import com.example.myapplication.data.repository.MetroRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class ApiTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApiTestScreen()
                }
            }
        }
    }
}

@HiltViewModel
class ApiTestViewModel @Inject constructor(
    private val simpleMetroApiServer: SimpleMetroApiServer,
    private val metroRepository: MetroRepository
) : ViewModel() {
    
    // Create a Retrofit instance for local testing
    private var _retrofit: Retrofit? = null
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    
    var serverUrl by mutableStateOf("")
        private set
    
    var stations by mutableStateOf<List<Station>>(emptyList())
        private set
    
    var lines by mutableStateOf<List<MetroLine>>(emptyList())
        private set
    
    var selectedStationName by mutableStateOf("")
    var selectedStationResult by mutableStateOf("")
    
    var selectedLineName by mutableStateOf("")
    var selectedLineResult by mutableStateOf("")
    var stationsByLineResult by mutableStateOf("")
    
    var sourceStation by mutableStateOf("")
    var destinationStation by mutableStateOf("")
    var routeResult by mutableStateOf("")
    
    var errorMessage by mutableStateOf<String?>(null)
    
    init {
        // Load initial data
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Start the server
                simpleMetroApiServer.startServer(port = 8080)
                
                // Wait for the server to start
                delay(1000)
                
                // Get server URL
                serverUrl = simpleMetroApiServer.getServerUrl()
                
                // Create Retrofit instance
                _retrofit = Retrofit.Builder()
                    .baseUrl(serverUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                
                // Load stations and lines for dropdowns
                stations = metroRepository.getAllStations()
                lines = metroRepository.getAllLines()
                
            } catch (e: Exception) {
                errorMessage = "Failed to initialize: ${e.message}"
            }
        }
    }
    
    fun testGetAllStations() {
        viewModelScope.launch {
            try {
                val api = createApiService()
                val response = api.getAllStations()
                selectedStationResult = gson.toJson(response)
                errorMessage = null
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun testGetStationByName() {
        viewModelScope.launch {
            try {
                if (selectedStationName.isBlank()) {
                    errorMessage = "Please select a station name"
                    return@launch
                }
                
                val api = createApiService()
                val response = api.getStation(selectedStationName)
                selectedStationResult = gson.toJson(response)
                errorMessage = null
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun testGetAllLines() {
        viewModelScope.launch {
            try {
                val api = createApiService()
                val response = api.getAllLines()
                selectedLineResult = gson.toJson(response)
                errorMessage = null
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun testGetLineByName() {
        viewModelScope.launch {
            try {
                if (selectedLineName.isBlank()) {
                    errorMessage = "Please select a line name"
                    return@launch
                }
                
                val api = createApiService()
                val response = api.getLine(selectedLineName)
                selectedLineResult = gson.toJson(response)
                errorMessage = null
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun testGetStationsByLine() {
        viewModelScope.launch {
            try {
                if (selectedLineName.isBlank()) {
                    errorMessage = "Please select a line name"
                    return@launch
                }
                
                val api = createApiService()
                val response = api.getStationsByLine(selectedLineName)
                stationsByLineResult = gson.toJson(response)
                errorMessage = null
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun testGetRoute() {
        viewModelScope.launch {
            try {
                if (sourceStation.isBlank() || destinationStation.isBlank()) {
                    errorMessage = "Please select source and destination stations"
                    return@launch
                }
                
                val api = createApiService()
                val response = api.getRoute(sourceStation, destinationStation)
                routeResult = gson.toJson(response)
                errorMessage = null
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    private fun handleError(e: Exception) {
        errorMessage = when (e) {
            is IOException -> "Network error: ${e.message}"
            is retrofit2.HttpException -> "HTTP error ${e.code()}: ${e.message()}"
            else -> "Error: ${e.message}"
        }
    }
    
    private fun createApiService(): ApiService {
        if (_retrofit == null) {
            throw IllegalStateException("Retrofit not initialized")
        }
        return _retrofit!!.create(ApiService::class.java)
    }
    
    interface ApiService {
        @GET("stations")
        suspend fun getAllStations(): List<Station>

        @GET("stations/{stationName}")
        suspend fun getStation(@Path("stationName") stationName: String): Station

        @GET("lines")
        suspend fun getAllLines(): List<MetroLine>

        @GET("lines/{lineName}")
        suspend fun getLine(@Path("lineName") lineName: String): MetroLine

        @GET("lines/{lineName}/stations")
        suspend fun getStationsByLine(@Path("lineName") lineName: String): List<Station>

        @GET("route/{sourceStation}/{destinationStation}")
        suspend fun getRoute(
            @Path("sourceStation") sourceStation: String,
            @Path("destinationStation") destinationStation: String
        ): Route
    }
}

@Composable
fun ApiTestScreen(viewModel: ApiTestViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Server Status
        Text(
            text = "Delhi Metro API Server",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Server URL: ${viewModel.serverUrl}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Error display
        viewModel.errorMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Station Endpoints
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Station Endpoints",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Get All Stations
                Button(
                    onClick = { viewModel.testGetAllStations() },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("GET /stations")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Get Station by Name
                Text(
                    text = "GET /stations/{stationName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                StationSelector(
                    stations = viewModel.stations,
                    selectedStation = viewModel.selectedStationName,
                    onStationSelected = { viewModel.selectedStationName = it }
                )
                
                Button(
                    onClick = { viewModel.testGetStationByName() },
                    modifier = Modifier.padding(vertical = 4.dp),
                    enabled = viewModel.selectedStationName.isNotBlank()
                ) {
                    Text("Test Endpoint")
                }
                
                // Results
                if (viewModel.selectedStationResult.isNotBlank()) {
                    JsonResultDisplay(json = viewModel.selectedStationResult)
                }
            }
        }
        
        // Line Endpoints
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Line Endpoints",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Get All Lines
                Button(
                    onClick = { viewModel.testGetAllLines() },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("GET /lines")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Get Line by Name
                Text(
                    text = "GET /lines/{lineName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                LineSelector(
                    lines = viewModel.lines,
                    selectedLine = viewModel.selectedLineName,
                    onLineSelected = { viewModel.selectedLineName = it }
                )
                
                Button(
                    onClick = { viewModel.testGetLineByName() },
                    modifier = Modifier.padding(vertical = 4.dp),
                    enabled = viewModel.selectedLineName.isNotBlank()
                ) {
                    Text("Test Endpoint")
                }
                
                // Results
                if (viewModel.selectedLineResult.isNotBlank()) {
                    JsonResultDisplay(json = viewModel.selectedLineResult)
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Get Stations by Line
                Text(
                    text = "GET /lines/{lineName}/stations",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = { viewModel.testGetStationsByLine() },
                    modifier = Modifier.padding(vertical = 4.dp),
                    enabled = viewModel.selectedLineName.isNotBlank()
                ) {
                    Text("Test Endpoint")
                }
                
                // Results
                if (viewModel.stationsByLineResult.isNotBlank()) {
                    JsonResultDisplay(json = viewModel.stationsByLineResult)
                }
            }
        }
        
        // Route Endpoint
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Route Endpoint",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "GET /route/{sourceStation}/{destinationStation}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Source Station:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                StationSelector(
                    stations = viewModel.stations,
                    selectedStation = viewModel.sourceStation,
                    onStationSelected = { viewModel.sourceStation = it }
                )
                
                Text(
                    text = "Destination Station:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                StationSelector(
                    stations = viewModel.stations,
                    selectedStation = viewModel.destinationStation,
                    onStationSelected = { viewModel.destinationStation = it }
                )
                
                Button(
                    onClick = { viewModel.testGetRoute() },
                    modifier = Modifier.padding(vertical = 4.dp),
                    enabled = viewModel.sourceStation.isNotBlank() && viewModel.destinationStation.isNotBlank()
                ) {
                    Text("Test Route")
                }
                
                // Results
                if (viewModel.routeResult.isNotBlank()) {
                    JsonResultDisplay(json = viewModel.routeResult)
                }
            }
        }
    }
}

@Composable
fun StationSelector(
    stations: List<Station>,
    selectedStation: String,
    onStationSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = selectedStation,
        onValueChange = { /* Read only */ },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Select a station") },
        readOnly = true
    )
    
    Button(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Choose Station")
    }
    
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        stations.forEach { station ->
            DropdownMenuItem(
                text = { Text(station.name) },
                onClick = {
                    onStationSelected(station.name)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun LineSelector(
    lines: List<MetroLine>,
    selectedLine: String,
    onLineSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = selectedLine,
        onValueChange = { /* Read only */ },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Select a line") },
        readOnly = true
    )
    
    Button(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Choose Line")
    }
    
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        lines.forEach { line ->
            DropdownMenuItem(
                text = { Text(line.name) },
                onClick = {
                    onLineSelected(line.name)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun JsonResultDisplay(json: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Response",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = json,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
} 