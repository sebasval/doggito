package com.example.doggitoapp.android.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doggitoapp.android.core.util.DateUtils
import com.example.doggitoapp.android.data.local.dao.StreakDao
import com.example.doggitoapp.android.data.local.entity.StreakEntity
import com.example.doggitoapp.android.domain.model.DailyTask
import com.example.doggitoapp.android.domain.model.Streak
import com.example.doggitoapp.android.domain.model.TaskCategory
import com.example.doggitoapp.android.domain.model.TransactionType
import com.example.doggitoapp.android.domain.repository.CoinRepository
import com.example.doggitoapp.android.domain.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TasksUiState(
    val tasks: List<DailyTask> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val streak: Streak? = null,
    val justCompleted: String? = null, // taskId for animation
    val activeTimerTaskId: String? = null,
    val timerSecondsRemaining: Int = 0,
    val timerTotalSeconds: Int = 0,
    val timerFinished: Boolean = false
)

class TasksViewModel(
    private val taskRepository: TaskRepository,
    private val coinRepository: CoinRepository,
    private val streakDao: StreakDao,
    private val networkMonitor: com.example.doggitoapp.android.core.util.NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val userId = "local_user"
    private val today = DateUtils.todayStartMillis()

    private var countdownJob: Job? = null

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            taskRepository.generateDailyTasks(userId, today)
        }

        viewModelScope.launch {
            taskRepository.getTasksByDate(userId, today).collect { tasks ->
                _uiState.value = _uiState.value.copy(tasks = tasks)
            }
        }

        viewModelScope.launch {
            taskRepository.getCompletedCountByDate(userId, today).collect { count ->
                _uiState.value = _uiState.value.copy(completedCount = count)
            }
        }

        viewModelScope.launch {
            taskRepository.getTotalCountByDate(userId, today).collect { count ->
                _uiState.value = _uiState.value.copy(totalCount = count)
            }
        }

        viewModelScope.launch {
            streakDao.getStreak(userId).collect { streak ->
                _uiState.value = _uiState.value.copy(streak = streak?.toDomain())
            }
        }
    }

    fun startTimer(task: DailyTask) {
        if (task.isCompleted) return

        // Cancel any existing timer
        countdownJob?.cancel()

        val totalSeconds = getTimerDuration(task.category)

        _uiState.value = _uiState.value.copy(
            activeTimerTaskId = task.id,
            timerSecondsRemaining = totalSeconds,
            timerTotalSeconds = totalSeconds,
            timerFinished = false
        )

        countdownJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _uiState.value = _uiState.value.copy(
                    timerSecondsRemaining = remaining,
                    timerFinished = remaining == 0
                )
            }
        }
    }

    fun cancelTimer() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.value = _uiState.value.copy(
            activeTimerTaskId = null,
            timerSecondsRemaining = 0,
            timerTotalSeconds = 0,
            timerFinished = false
        )
    }

    fun completeTask(task: DailyTask) {
        if (task.isCompleted) return
        // Only allow completion if timer finished for this task
        val state = _uiState.value
        if (state.activeTimerTaskId != task.id || !state.timerFinished) return

        countdownJob?.cancel()
        countdownJob = null

        viewModelScope.launch {
            taskRepository.completeTask(task.id)

            // Award coins
            coinRepository.addCoins(
                userId = userId,
                amount = task.rewardCoins,
                type = TransactionType.TASK,
                description = task.title
            )

            _uiState.value = _uiState.value.copy(
                justCompleted = task.id,
                activeTimerTaskId = null,
                timerSecondsRemaining = 0,
                timerTotalSeconds = 0,
                timerFinished = false
            )

            // Check if all tasks completed -> update streak
            val completed = (_uiState.value.completedCount) + 1
            val total = _uiState.value.totalCount
            if (completed >= total) {
                updateStreak()
            }
        }
    }

    private fun getTimerDuration(category: TaskCategory): Int = when (category) {
        TaskCategory.BASIC_CARE -> 45
        TaskCategory.HEALTH -> 90
        TaskCategory.EXERCISE -> 120
        TaskCategory.TRAINING -> 90
        TaskCategory.WELLNESS -> 60
    }

    private suspend fun updateStreak() {
        val currentStreak = _uiState.value.streak
        val yesterday = DateUtils.yesterdayStartMillis()

        val newCurrent = if (currentStreak != null && currentStreak.lastCompletedDate >= yesterday) {
            currentStreak.currentStreak + 1
        } else {
            1
        }

        val newLongest = maxOf(newCurrent, currentStreak?.longestStreak ?: 0)

        streakDao.insertOrUpdateStreak(
            StreakEntity(
                userId = userId,
                currentStreak = newCurrent,
                longestStreak = newLongest,
                lastCompletedDate = today
            )
        )

        // Streak bonus every 7 days
        if (newCurrent % 7 == 0) {
            val bonus = newCurrent / 7 * 25
            coinRepository.addCoins(
                userId = userId,
                amount = bonus,
                type = TransactionType.STREAK_BONUS,
                description = "Racha de $newCurrent días"
            )
        }
    }

    fun clearJustCompleted() {
        _uiState.value = _uiState.value.copy(justCompleted = null)
    }
}
