package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.api.SimpleMetroApiServer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
 
@HiltAndroidApp
class MetroApplication : Application() {
    @Inject
    lateinit var simpleMetroApiServer: SimpleMetroApiServer
    
    override fun onCreate() {
        super.onCreate()
        
        // Start the Metro API Server
        simpleMetroApiServer.startServer(port = 8080)
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        // Stop the server when the application is terminated
        simpleMetroApiServer.stop()
    }
} 