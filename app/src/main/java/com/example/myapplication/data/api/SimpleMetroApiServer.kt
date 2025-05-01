package com.example.myapplication.data.api

import android.content.Context
import android.util.Log
import com.example.myapplication.data.repository.MetroRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleMetroApiServer @Inject constructor(
    private val metroRepository: MetroRepository
) : NanoHTTPD(8080) {
    
    private val TAG = "SimpleMetroApiServer"
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val serverScope = CoroutineScope(Dispatchers.IO)
    private var customPort = 8080
    
    fun startServer(port: Int = 8080) {
        customPort = port
        serverScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    stop() // Ensure we stop any previous instance
                    closeAllConnections()
                    
                    // Create a new server with the custom port
                    val newServer = object : NanoHTTPD(port) {
                        override fun serve(session: IHTTPSession): Response {
                            return this@SimpleMetroApiServer.serve(session)
                        }
                    }
                    
                    try {
                        newServer.start(SOCKET_READ_TIMEOUT, false)
                        Log.i(TAG, "Server started on port $port")
                    } catch (e: IOException) {
                        Log.e(TAG, "Port $port is already in use. Trying another port.")
                        newServer.stop()
                        
                        // Try a different port
                        val alternativePort = port + 1
                        val alternativeServer = object : NanoHTTPD(alternativePort) {
                            override fun serve(session: IHTTPSession): Response {
                                return this@SimpleMetroApiServer.serve(session)
                            }
                        }
                        
                        alternativeServer.start(SOCKET_READ_TIMEOUT, false)
                        customPort = alternativePort
                        Log.i(TAG, "Server started on alternative port $alternativePort")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start server: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        Log.d(TAG, "Request: ${session.method} $uri")
        
        return try {
            when {
                uri == "/stations" && session.method == Method.GET -> handleGetAllStations()
                uri.matches(Regex("/stations/[^/]+")) && session.method == Method.GET -> handleGetStation(uri)
                uri == "/lines" && session.method == Method.GET -> handleGetAllLines()
                uri.matches(Regex("/lines/[^/]+")) && session.method == Method.GET -> handleGetLine(uri)
                uri.matches(Regex("/lines/[^/]+/stations")) && session.method == Method.GET -> handleGetStationsByLine(uri)
                uri.matches(Regex("/route/[^/]+/[^/]+")) && session.method == Method.GET -> handleGetRoute(uri)
                uri == "/health" && session.method == Method.GET -> handleHealthCheck()
                else -> notFoundResponse()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling request: ${e.message}")
            errorResponse(e.message ?: "Unknown error")
        }
    }
    
    private fun handleGetAllStations(): Response {
        val stations = runBlocking { metroRepository.getAllStations() }
        return successResponse(gson.toJson(stations))
    }
    
    private fun handleGetStation(uri: String): Response {
        val stationName = uri.substringAfter("/stations/")
        val station = runBlocking { metroRepository.getStation(stationName) }
        
        return if (station != null) {
            successResponse(gson.toJson(station))
        } else {
            notFoundResponse("Station not found: $stationName")
        }
    }
    
    private fun handleGetAllLines(): Response {
        val lines = runBlocking { metroRepository.getAllLines() }
        return successResponse(gson.toJson(lines))
    }
    
    private fun handleGetLine(uri: String): Response {
        val lineName = uri.substringAfter("/lines/")
        val line = runBlocking { metroRepository.getLine(lineName) }
        
        return if (line != null) {
            successResponse(gson.toJson(line))
        } else {
            notFoundResponse("Line not found: $lineName")
        }
    }
    
    private fun handleGetStationsByLine(uri: String): Response {
        val lineName = uri.substringAfter("/lines/").substringBefore("/stations")
        val stations = runBlocking { metroRepository.getStationsByLine(lineName) }
        return successResponse(gson.toJson(stations))
    }
    
    private fun handleGetRoute(uri: String): Response {
        val parts = uri.substringAfter("/route/").split("/")
        if (parts.size != 2) {
            return badRequestResponse("Invalid route request format")
        }
        
        val sourceStation = parts[0]
        val destinationStation = parts[1]
        
        val route = runBlocking { metroRepository.getRoute(sourceStation, destinationStation) }
        return successResponse(gson.toJson(route))
    }
    
    private fun handleHealthCheck(): Response {
        val response = mapOf("status" to "OK", "version" to "1.0")
        return successResponse(gson.toJson(response))
    }
    
    private fun successResponse(data: String): Response {
        return newFixedLengthResponse(Response.Status.OK, "application/json", data)
    }
    
    private fun notFoundResponse(message: String = "Resource not found"): Response {
        val response = mapOf("error" to message)
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", gson.toJson(response))
    }
    
    private fun badRequestResponse(message: String): Response {
        val response = mapOf("error" to message)
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", gson.toJson(response))
    }
    
    private fun errorResponse(message: String): Response {
        val response = mapOf("error" to message)
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", gson.toJson(response))
    }
    
    fun getServerUrl(): String {
        return "http://localhost:$customPort"
    }
} 