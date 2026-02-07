package com.example.doggitoapp.android.core.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.doggitoapp.android.DoggitoApp
import com.example.doggitoapp.android.MainActivity
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocationPoint(val latitude: Double, val longitude: Double, val timestamp: Long)

class LocationTrackingService : Service() {

    companion object {
        private val _locationPoints = MutableStateFlow<List<LocationPoint>>(emptyList())
        val locationPoints: StateFlow<List<LocationPoint>> = _locationPoints.asStateFlow()

        private val _isTracking = MutableStateFlow(false)
        val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

        private val _distanceMeters = MutableStateFlow(0f)
        val distanceMeters: StateFlow<Float> = _distanceMeters.asStateFlow()

        fun resetTracking() {
            _locationPoints.value = emptyList()
            _distanceMeters.value = 0f
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val newPoint = LocationPoint(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = System.currentTimeMillis()
                    )

                    val currentPoints = _locationPoints.value
                    if (currentPoints.isNotEmpty()) {
                        val lastPoint = currentPoints.last()
                        val distance = calculateDistance(
                            lastPoint.latitude, lastPoint.longitude,
                            newPoint.latitude, newPoint.longitude
                        )
                        // Anti-fraud: ignore if speed > 25 km/h (unrealistic for dog walking/running)
                        val timeDiff = (newPoint.timestamp - lastPoint.timestamp) / 1000.0 // seconds
                        val speedKmh = if (timeDiff > 0) (distance / 1000.0) / (timeDiff / 3600.0) else 0.0
                        if (speedKmh <= 25.0 && distance > 2) { // minimum 2m to avoid GPS jitter
                            _distanceMeters.value += distance
                        }
                    }
                    _locationPoints.value = currentPoints + newPoint
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1001, createNotification())
        _isTracking.value = true
        startLocationUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        _isTracking.value = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateDistanceMeters(5f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (_: SecurityException) {
            stopSelf()
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DoggitoApp.CHANNEL_LOCATION)
            .setContentTitle("Doggito - Actividad en curso")
            .setContentText("Registrando tu paseo con tu perro")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
}
