package com.example.doggitoapp.android.feature.tasks

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doggitoapp.android.core.theme.*
import com.example.doggitoapp.android.domain.model.DailyTask
import com.example.doggitoapp.android.domain.model.TaskCategory
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onBack: () -> Unit,
    viewModel: TasksViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas del Dia", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Progress header
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DoggitoGreen.copy(alpha = 0.1f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Progreso",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "${uiState.completedCount}/${uiState.totalCount}",
                                fontWeight = FontWeight.Bold,
                                color = DoggitoGreenDark
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        val progress = if (uiState.totalCount > 0) uiState.completedCount.toFloat() / uiState.totalCount else 0f
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = DoggitoGreen,
                            trackColor = DoggitoGreenLight.copy(alpha = 0.3f)
                        )
                        uiState.streak?.let { streak ->
                            if (streak.currentStreak > 0) {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalFireDepartment, null, tint = DoggitoAmber, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Racha actual: ${streak.currentStreak} dias | Mejor: ${streak.longestStreak} dias",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tasks grouped by category
            val grouped = uiState.tasks.groupBy { it.category }
            TaskCategory.entries.forEach { category ->
                val tasksInCategory = grouped[category] ?: return@forEach
                item {
                    Text(
                        category.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor(category),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(tasksInCategory, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        isTimerActive = uiState.activeTimerTaskId == task.id,
                        timerSecondsRemaining = uiState.timerSecondsRemaining,
                        timerTotalSeconds = uiState.timerTotalSeconds,
                        timerFinished = uiState.timerFinished,
                        isJustCompleted = uiState.justCompleted == task.id,
                        onStartTimer = { viewModel.startTimer(task) },
                        onCancelTimer = { viewModel.cancelTimer() },
                        onComplete = { viewModel.completeTask(task) }
                    )
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskCard(
    task: DailyTask,
    isTimerActive: Boolean,
    timerSecondsRemaining: Int,
    timerTotalSeconds: Int,
    timerFinished: Boolean,
    isJustCompleted: Boolean,
    onStartTimer: () -> Unit,
    onCancelTimer: () -> Unit,
    onComplete: () -> Unit
) {
    val color = categoryColor(task.category)
    val isLast10Seconds = timerSecondsRemaining in 1..10

    // Card click logic:
    // - Not completed, no timer active -> start timer
    // - Timer active and finished -> complete task
    // - Otherwise (completed or timer running) -> do nothing
    val onCardClick: () -> Unit = {
        when {
            task.isCompleted -> { /* already done */ }
            isTimerActive && timerFinished -> onComplete()
            isTimerActive && !timerFinished -> { /* timer still running */ }
            else -> onStartTimer()
        }
    }

    Card(
        onClick = onCardClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isTimerActive) {
                if (timerFinished) SuccessGreen.copy(alpha = 0.06f) else color.copy(alpha = 0.06f)
            } else CardSurface
        )
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(animationSpec = tween(300))
                .padding(12.dp)
        ) {
            // Main row: task info
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Static visual indicator (not interactive - the whole card handles clicks)
                if (task.isCompleted) {
                    // Filled check circle
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = color
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                } else if (isTimerActive) {
                    // Circular timer indicator
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val targetProgress = if (timerTotalSeconds > 0) {
                            1f - (timerSecondsRemaining.toFloat() / timerTotalSeconds)
                        } else 1f
                        val animatedProgress by animateFloatAsState(
                            targetValue = targetProgress,
                            animationSpec = tween(900),
                            label = "timer_progress"
                        )
                        CircularProgressIndicator(
                            progress = animatedProgress,
                            modifier = Modifier.size(36.dp),
                            color = if (timerFinished) SuccessGreen
                                    else if (isLast10Seconds) DoggitoAmber
                                    else DoggitoGreen,
                            trackColor = Color.LightGray.copy(alpha = 0.3f),
                            strokeWidth = 3.dp,
                            strokeCap = StrokeCap.Round
                        )
                        if (timerFinished) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                formatSeconds(timerSecondsRemaining),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLast10Seconds) DoggitoAmber else TextPrimary
                            )
                        }
                    }
                } else {
                    // Empty circle indicator (pending task)
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .border(
                                    width = 2.dp,
                                    color = color.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        task.title,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) TextSecondary.copy(alpha = 0.5f) else TextPrimary
                    )
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (task.isCompleted) SuccessGreen.copy(alpha = 0.12f) else DoggiCoinGold.copy(alpha = 0.12f)
                ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "+${task.rewardCoins}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) SuccessGreen else DoggiCoinGoldDark
                        )
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            Icons.Default.MonetizationOn, null,
                            modifier = Modifier.size(14.dp),
                            tint = if (task.isCompleted) SuccessGreen else DoggiCoinGold
                        )
                    }
                }
            }

            // Expanded timer section
            if (isTimerActive) {
                Spacer(Modifier.height(12.dp))

                if (timerFinished) {
                    // Timer completed - tap card or tap button to complete
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessGreen.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Toca para completar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = onCancelTimer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "Cancelar",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    // Timer still running
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Large timer display
                        Text(
                            formatTime(timerSecondsRemaining),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isLast10Seconds) DoggitoAmber else color,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Realiza la tarea mientras esperas...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(8.dp))

                        // Progress bar
                        val barProgress = if (timerTotalSeconds > 0) {
                            1f - (timerSecondsRemaining.toFloat() / timerTotalSeconds)
                        } else 0f
                        val animatedBarProgress by animateFloatAsState(
                            targetValue = barProgress,
                            animationSpec = tween(900),
                            label = "bar_progress"
                        )
                        LinearProgressIndicator(
                            progress = animatedBarProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isLast10Seconds) DoggitoAmber else color,
                            trackColor = Color.LightGray.copy(alpha = 0.2f)
                        )
                        Spacer(Modifier.height(8.dp))

                        TextButton(
                            onClick = onCancelTimer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TextSecondary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Cancelar",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formats seconds as "M:SS" for the large timer display
 */
private fun formatTime(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "%d:%02d".format(min, sec)
}

/**
 * Formats seconds compactly for the small circular indicator
 */
private fun formatSeconds(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return if (min > 0) "${min}m" else "${sec}s"
}

private fun categoryColor(category: TaskCategory): Color = when (category) {
    TaskCategory.BASIC_CARE -> BasicCareColor
    TaskCategory.HEALTH -> HealthColor
    TaskCategory.EXERCISE -> ExerciseColor
    TaskCategory.TRAINING -> TrainingColor
    TaskCategory.WELLNESS -> WellnessColor
}
