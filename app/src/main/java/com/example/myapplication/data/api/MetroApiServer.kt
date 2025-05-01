package com.example.myapplication.data.api

import android.content.Context
import android.util.Log
import com.example.myapplication.data.model.MetroLine
import com.example.myapplication.data.model.Route
import com.example.myapplication.data.model.Station
import com.example.myapplication.data.repository.MetroRepository
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.BindException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetroApiServer @Inject constructor(
    private val metroRepository: MetroRepository
) {
    private var server: ApplicationEngine? = null
    private val serverScope = CoroutineScope(Dispatchers.IO)
    private val TAG = "MetroApiServer"
    
    fun start(context: Context, port: Int = 8080) {
        serverScope.launch {
            try {
                // Set system property to use Android logger for SLF4J
                System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "ERROR")
                System.setProperty("org.slf4j.simpleLogger.showThreadName", "false")
                System.setProperty("org.slf4j.simpleLogger.showLogName", "false")
                System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true")
                
                startServer(port)
                Log.i(TAG, "Server started on port $port")
            } catch (e: BindException) {
                Log.e(TAG, "Port $port is already in use. Trying another port.")
                startServer(port + 1)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start server: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    private fun startServer(port: Int) {
        server = embeddedServer(Netty, port = port, configure = {
            // Configure Netty to avoid using certain Java features
            connectionGroupSize = 1
            workerGroupSize = 1
            callGroupSize = 1
        }) {
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                    serializeNulls()
                }
            }
            
            install(CORS) {
                anyHost()
                allowHeader(HttpHeaders.ContentType)
                allowMethod(HttpMethod.Get)
            }
            
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (cause.message ?: "Unknown error"))
                    )
                    Log.e(TAG, "Server error", cause)
                }
                
                status(HttpStatusCode.NotFound) { call, _ ->
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Resource not found")
                    )
                }
            }
            
            routing {
                // Get all stations
                get("/stations") {
                    call.respond(metroRepository.getAllStations())
                }
                
                // Get station by name
                get("/stations/{stationName}") {
                    val stationName = call.parameters["stationName"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Station name is required")
                    )
                    
                    try {
                        val station = metroRepository.getStation(stationName)
                        if (station != null) {
                            call.respond(station)
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                mapOf("error" to "Station not found: $stationName")
                            )
                        }
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to e.message)
                        )
                    }
                }
                
                // Get all lines
                get("/lines") {
                    call.respond(metroRepository.getAllLines())
                }
                
                // Get line by name
                get("/lines/{lineName}") {
                    val lineName = call.parameters["lineName"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Line name is required")
                    )
                    
                    try {
                        val line = metroRepository.getLine(lineName)
                        if (line != null) {
                            call.respond(line) 
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                mapOf("error" to "Line not found: $lineName")
                            )
                        }
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to e.message)
                        )
                    }
                }
                
                // Get stations by line
                get("/lines/{lineName}/stations") {
                    val lineName = call.parameters["lineName"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Line name is required")
                    )
                    
                    try {
                        val stations = metroRepository.getStationsByLine(lineName)
                        call.respond(stations)
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to e.message)
                        )
                    }
                }
                
                // Find route between stations
                get("/route/{sourceStation}/{destinationStation}") {
                    val sourceStation = call.parameters["sourceStation"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Source station is required")
                    )
                    
                    val destinationStation = call.parameters["destinationStation"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Destination station is required")
                    )
                    
                    try {
                        val route = metroRepository.getRoute(sourceStation, destinationStation)
                        call.respond(route)
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to (e.message ?: "Error finding route"))
                        )
                    }
                }
                
                // Health check
                get("/health") {
                    call.respond(mapOf("status" to "OK", "version" to "1.0"))
                }
            }
        }.start(wait = false)
    }
    
    fun stop() {
        server?.stop(1000, 2000)
        server = null
        Log.i(TAG, "Server stopped")
    }
    
    fun getServerUrl(): String {
        return "http://localhost:${server?.environment?.connectors?.get(0)?.port ?: 8080}"
    }
} 