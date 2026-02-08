package com.example.doggitoapp.android.feature.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doggitoapp.android.core.service.LocationTrackingService
import com.example.doggitoapp.android.domain.model.RunningMode
import com.example.doggitoapp.android.domain.model.RunningSession
import com.example.doggitoapp.android.domain.model.TransactionType
import com.example.doggitoapp.android.domain.repository.CoinRepository
import com.example.doggitoapp.android.domain.repository.RunRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.util.UUID

data class RunningUiState(
    val sessions: List<RunningSession> = emptyList(),
    val totalDistance: Float = 0f,
    val sessionCount: Int = 0,
    // Active session state
    val isActive: Boolean = false,
    val currentDistance: Float = 0f,
    val elapsedMillis: Long = 0L,
    val currentMode: RunningMode = RunningMode.WALK,
    val startTime: Long = 0L
)

class RunningViewModel(
    private val runRepository: RunRepository,
    private val coinRepository: CoinRepository,
    private val petRepository: com.example.doggitoapp.android.domain.repository.PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState.asStateFlow()

    private val userId = "local_user"

    init {
        loadHistory()
        observeTracking()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            runRepository.getSessionsByUser(userId).collect { sessions ->
                _uiState.value = _uiState.value.copy(sessions = sessions)
            }
        }
        viewModelScope.launch {
            runRepository.getTotalDistance(userId).collect { dist ->
                _uiState.value = _uiState.value.copy(totalDistance = dist)
            }
        }
        viewModelScope.launch {
            runRepository.getSessionCount(userId).collect { count ->
                _uiState.value = _uiState.value.copy(sessionCount = count)
            }
        }
    }

    private fun observeTracking() {
        viewModelScope.launch {
            LocationTrackingService.isTracking.collect { tracking ->
                _uiState.value = _uiState.value.copy(isActive = tracking)
            }
        }
        viewModelScope.launch {
            LocationTrackingService.distanceMeters.collect { dist ->
                val elapsed = if (_uiState.value.startTime > 0)
                    System.currentTimeMillis() - _uiState.value.startTime else 0L
                _uiState.value = _uiState.value.copy(
                    currentDistance = dist,
                    elapsedMillis = elapsed
                )
            }
        }
    }

    fun startSession(mode: RunningMode) {
        LocationTrackingService.resetTracking()
        _uiState.value = _uiState.value.copy(
            currentMode = mode,
            startTime = System.currentTimeMillis(),
            currentDistance = 0f,
            elapsedMillis = 0L
        )
    }

    fun finishSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val endTime = System.currentTimeMillis()
            val durationMillis = endTime - state.startTime
            val distanceMeters = state.currentDistance
            val durationHours = durationMillis / 3600000.0
            val avgSpeed = if (durationHours > 0) (distanceMeters / 1000f) / durationHours.toFloat() else 0f

            // Calories estimation
            val caloriesUser = estimateUserCalories(distanceMeters, durationMillis, state.currentMode)
            val caloriesDog = estimateDogCalories(distanceMeters, state.currentMode)

            // DoggiCoins: 1 per 100m
            val coins = (distanceMeters / 100f).toInt()

            // Build route JSON
            val points = LocationTrackingService.locationPoints.value
            val routeData = points.map { listOf(it.latitude, it.longitude) }
            val routeJson = try { Json.encodeToString(routeData) } catch (_: Exception) { "[]" }

            // Get pet ID
            var petId = ""
            petRepository.getFirstPetByUser(userId).first()?.let { petId = it.id }

            val session = RunningSession(
                id = UUID.randomUUID().toString(),
                userId = userId,
                petId = petId,
                distanceMeters = distanceMeters,
                durationMillis = durationMillis,
                avgSpeedKmh = avgSpeed,
                caloriesUser = caloriesUser,
                caloriesDog = caloriesDog,
                mode = state.currentMode,
                routeJson = routeJson,
                coinsEarned = coins,
                startedAt = state.startTime,
                endedAt = endTime
            )

            runRepository.saveSession(session)

            if (coins > 0) {
                coinRepository.addCoins(
                    userId = userId,
                    amount = coins,
                    type = TransactionType.RUNNING,
                    description = "${state.currentMode.displayName}: ${String.format("%.1f", distanceMeters / 1000)} km"
                )
            }

            _uiState.value = _uiState.value.copy(startTime = 0L, currentDistance = 0f, elapsedMillis = 0L)
        }
    }

    private fun estimateUserCalories(distanceMeters: Float, durationMillis: Long, mode: RunningMode): Float {
        val km = distanceMeters / 1000f
        return when (mode) {
            RunningMode.WALK -> km * 50f  // ~50 cal/km walking
            RunningMode.RUN -> km * 80f   // ~80 cal/km running
            RunningMode.HIKE -> km * 65f  // ~65 cal/km hiking
        }
    }

    private fun estimateDogCalories(distanceMeters: Float, mode: RunningMode): Float {
        val km = distanceMeters / 1000f
        return when (mode) {
            RunningMode.WALK -> km * 30f
            RunningMode.RUN -> km * 55f
            RunningMode.HIKE -> km * 45f
        }
    }
}
