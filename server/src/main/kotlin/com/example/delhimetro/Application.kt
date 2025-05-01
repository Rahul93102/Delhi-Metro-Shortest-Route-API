package com.example.delhimetro

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import java.io.File

// Sample model classes for Delhi Metro API
data class Station(val name: String, val line: String)
data class MetroLine(val name: String, val stations: List<String>)
data class Route(val stations: List<String>, val lineChanges: Int)

fun main() {
    // Get the port from environment variable (Render sets this)
    val port = System.getenv("PORT")?.toInt() ?: 8080
    
    embeddedServer(Netty, port = port) {
        install(ContentNegotiation) {
            gson()
        }
        
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    routing {
        // Health check endpoint
        get("/health") {
            call.respond(mapOf("status" to "UP"))
        }
        
        // Get all stations
        get("/stations") {
            // Sample data - replace with your actual implementation
            val stations = listOf(
                Station("Rajiv Chowk", "Yellow Line"),
                Station("Kashmere Gate", "Red Line"),
                Station("Central Secretariat", "Yellow Line"),
                Station("Dwarka Sector 21", "Blue Line")
            )
            call.respond(stations)
        }
        
        // Get station by name
        get("/stations/{name}") {
            val stationName = call.parameters["name"] ?: return@get call.respond(
                HttpStatusCode.BadRequest, 
                mapOf("error" to "Station name is required")
            )
            
            // Sample data - replace with your actual implementation
            val station = when(stationName) {
                "Rajiv Chowk" -> Station("Rajiv Chowk", "Yellow Line")
                "Kashmere Gate" -> Station("Kashmere Gate", "Red Line")
                "Central Secretariat" -> Station("Central Secretariat", "Yellow Line")
                "Dwarka Sector 21" -> Station("Dwarka Sector 21", "Blue Line")
                else -> null
            }
            
            if (station != null) {
                call.respond(station)
            } else {
                call.respond(
                    HttpStatusCode.NotFound, 
                    mapOf("error" to "Station not found")
                )
            }
        }
        
        // Get all lines
        get("/lines") {
            // Sample data - replace with your actual implementation
            val lines = listOf(
                "Yellow Line",
                "Blue Line",
                "Red Line",
                "Green Line",
                "Violet Line"
            )
            call.respond(lines)
        }
        
        // Get line by name
        get("/lines/{name}") {
            val lineName = call.parameters["name"] ?: return@get call.respond(
                HttpStatusCode.BadRequest, 
                mapOf("error" to "Line name is required")
            )
            
            // Sample data - replace with your actual implementation
            val line = when(lineName.lowercase()) {
                "yellow" -> MetroLine("Yellow Line", listOf("Samaypur Badli", "Jahangirpuri", "Adarsh Nagar", "Azadpur"))
                "blue" -> MetroLine("Blue Line", listOf("Dwarka Sector 21", "Dwarka", "Janakpuri West"))
                "red" -> MetroLine("Red Line", listOf("Dilshad Garden", "Shahdara", "Welcome"))
                else -> null
            }
            
            if (line != null) {
                call.respond(line)
            } else {
                call.respond(
                    HttpStatusCode.NotFound, 
                    mapOf("error" to "Line not found")
                )
            }
        }
        
        // Get stations by line
        get("/lines/{name}/stations") {
            val lineName = call.parameters["name"] ?: return@get call.respond(
                HttpStatusCode.BadRequest, 
                mapOf("error" to "Line name is required")
            )
            
            // Sample data - replace with your actual implementation
            val stations = when(lineName.lowercase()) {
                "yellow" -> listOf("Samaypur Badli", "Jahangirpuri", "Adarsh Nagar", "Azadpur")
                "blue" -> listOf("Dwarka Sector 21", "Dwarka", "Janakpuri West")
                "red" -> listOf("Dilshad Garden", "Shahdara", "Welcome")
                else -> null
            }
            
            if (stations != null) {
                call.respond(stations)
            } else {
                call.respond(
                    HttpStatusCode.NotFound, 
                    mapOf("error" to "Line not found")
                )
            }
        }
        
        // Find route between stations
        get("/route") {
            val from = call.request.queryParameters["from"]
            val to = call.request.queryParameters["to"]
            
            if (from == null || to == null) {
                return@get call.respond(
                    HttpStatusCode.BadRequest, 
                    mapOf("error" to "Both 'from' and 'to' parameters are required")
                )
            }
            
            // Sample data - replace with your actual implementation
            val route = when {
                from == "Rajiv Chowk" && to == "Dwarka" -> 
                    Route(listOf("Rajiv Chowk", "Karol Bagh", "Rajouri Garden", "Dwarka"), 0)
                from == "Kashmere Gate" && to == "Central Secretariat" -> 
                    Route(listOf("Kashmere Gate", "Chandni Chowk", "New Delhi", "Rajiv Chowk", "Central Secretariat"), 1)
                else -> 
                    Route(listOf(from, to), 0) // Fallback route
            }
            
            call.respond(route)
        }
    }
} 