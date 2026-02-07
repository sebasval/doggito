package com.example.doggitoapp.android.feature.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
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
                        isJustCompleted = uiState.justCompleted == task.id,
                        onComplete = { viewModel.completeTask(task) }
                    )
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun TaskCard(
    task: DailyTask,
    isJustCompleted: Boolean,
    onComplete: () -> Unit
) {
    val color = categoryColor(task.category)

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { if (!task.isCompleted) onComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = color,
                    uncheckedColor = color.copy(alpha = 0.5f)
                )
            )
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
    }
}

private fun categoryColor(category: TaskCategory): Color = when (category) {
    TaskCategory.BASIC_CARE -> BasicCareColor
    TaskCategory.HEALTH -> HealthColor
    TaskCategory.EXERCISE -> ExerciseColor
    TaskCategory.TRAINING -> TrainingColor
    TaskCategory.WELLNESS -> WellnessColor
}
