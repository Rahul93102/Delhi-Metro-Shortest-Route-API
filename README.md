# Delhi Metro API

A comprehensive API for Delhi Metro network information, route planning, and navigation.

## Overview

This project implements a RESTful API to provide information about the Delhi Metro network, including stations, lines, and route planning functionality. It uses a graph-based approach for pathfinding to determine optimal routes between stations.

## Features

- Complete Delhi Metro network data
- Station information retrieval
- Metro line information
- Efficient route planning using Dijkstra's algorithm
- Line transfer penalties for realistic route suggestions
- JSON-based responses for easy integration

## API Endpoints

| Endpoint                 | Method | Description                 | Example                               |
| ------------------------ | ------ | --------------------------- | ------------------------------------- |
| `/health`                | GET    | Check API health            | `/health`                             |
| `/stations`              | GET    | Get all stations            | `/stations`                           |
| `/stations/{name}`       | GET    | Get station by name         | `/stations/Dwarka%20Sector%2021`      |
| `/lines`                 | GET    | Get all metro lines         | `/lines`                              |
| `/lines/{name}`          | GET    | Get line by name            | `/lines/yellow`                       |
| `/lines/{name}/stations` | GET    | Get stations by line        | `/lines/blue/stations`                |
| `/route`                 | GET    | Find route between stations | `/route?from=Rajiv%20Chowk&to=Dwarka` |

## Technical Approach

### Data Model

The project uses an index-based approach for representing the Delhi Metro network:

1. **Station Model**: Each station has:

   - Name
   - Line(s) it belongs to
   - Index number on each line

2. **Line Model**: Each metro line has:

   - Name
   - Color
   - List of stations with their indices

3. **Route Model**: Used to represent paths between stations:
   - List of stations in order
   - Total number of stations
   - Number of line changes

### Pathfinding Algorithm

The route planning functionality uses Dijkstra's algorithm with the following considerations:

- Stations are represented as nodes in a graph
- Connections between adjacent stations on the same line have a weight of 1
- Line transfers incur a penalty (higher weight) to represent the time and effort of changing lines
- The algorithm finds the shortest path considering both distance and line changes

### Data Source

The metro network data is sourced from JSON files for each line, which contain:

- Station names
- Station indices
- Line information
- Transfer points

This replaced the initial approach of using CSV data with geographical coordinates.

## Local Setup

### Prerequisites

- Android Studio
- JDK 8 or higher
- Gradle

### Installation

1. Clone the repository:

   ```
   git clone https://github.com/yourusername/delhi-metro-api.git
   ```

2. Open the project in Android Studio

3. Build the project:

   ```
   ./gradlew assembleDebug
   ```

4. Install on a device or emulator:
   ```
   ./gradlew installDebug
   ```

## Testing the API

### Using ADB

1. Forward the port from your device to your computer:

   ```
   adb forward tcp:8080 tcp:8080
   ```

2. Launch the app on your device:

   ```
   adb shell am start -n com.example.myapplication/.ui.ApiTestActivity
   ```

3. Test endpoints using curl:

   ```
   # Health check
   adb shell curl -s http://localhost:8080/health

   # Get all stations
   adb shell curl -s http://localhost:8080/stations

   # Get station by name
   adb shell curl -s "http://localhost:8080/stations/Dwarka%20Sector%2021"

   # Get all lines
   adb shell curl -s http://localhost:8080/lines

   # Get line by name
   adb shell curl -s "http://localhost:8080/lines/yellow"

   # Get stations on a line
   adb shell curl -s "http://localhost:8080/lines/blue/stations"

   # Find route between stations
   adb shell curl -s "http://localhost:8080/route?from=Rajiv%20Chowk&to=Dwarka"
   ```


For most platforms, the process involves:

1. Converting your Android API to a standalone Kotlin application (using Ktor or Spring Boot)
2. Setting up the appropriate build configuration (build.gradle)
3. Creating a Procfile (for Heroku) or equivalent config file
4. Configuring environment variables
5. Connecting your repository for automatic deployments

## Future Enhancements

- Real-time train information
- Fare calculation
- Platform information
- Accessibility features
- User authentication for personalized routes
- Station amenities information

## License

[MIT License](LICENSE)
