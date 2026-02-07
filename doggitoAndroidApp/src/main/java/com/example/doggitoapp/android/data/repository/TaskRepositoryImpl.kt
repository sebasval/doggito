package com.example.doggitoapp.android.data.repository

import com.example.doggitoapp.android.data.local.dao.DailyTaskDao
import com.example.doggitoapp.android.data.local.entity.DailyTaskEntity
import com.example.doggitoapp.android.domain.model.DailyTask
import com.example.doggitoapp.android.domain.model.TaskCategory
import com.example.doggitoapp.android.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class TaskRepositoryImpl(
    private val taskDao: DailyTaskDao
) : TaskRepository {

    private val taskPool = mapOf(
        TaskCategory.BASIC_CARE to listOf(
            "Alimentar a tu perro" to "Asegúrate de darle su comida a la hora adecuada",
            "Cambiar el agua" to "Agua fresca y limpia para tu mascota",
            "Cepillar el pelaje" to "Un buen cepillado mantiene el pelo sano",
            "Revisar las uñas" to "Verifica si necesitan un corte",
            "Limpiar el comedero" to "La higiene es importante para su salud"
        ),
        TaskCategory.HEALTH to listOf(
            "Revisar ojos y orejas" to "Busca signos de irritación o infección",
            "Dar medicamento" to "Si tu perro tiene algún tratamiento activo",
            "Revisar la piel" to "Busca parásitos, irritaciones o anomalías",
            "Pesar a tu perro" to "Mantén un control de su peso",
            "Revisar dientes" to "La salud dental es fundamental"
        ),
        TaskCategory.EXERCISE to listOf(
            "Paseo matutino" to "Al menos 20 minutos de caminata",
            "Juego en el parque" to "Tiempo de ejercicio y diversión al aire libre",
            "Juego de buscar" to "Lanza una pelota o juguete para que lo traiga",
            "Paseo vespertino" to "Un segundo paseo para cerrar el día",
            "Ejercicio de agilidad" to "Practica saltos y obstáculos simples"
        ),
        TaskCategory.TRAINING to listOf(
            "Práctica de sentarse" to "Refuerza el comando 'siéntate'",
            "Socialización" to "Presenta a tu perro a otro perro amigable",
            "Práctica de quedarse" to "Trabaja el comando 'quédate'",
            "Paseo con correa" to "Practica caminar sin jalar la correa",
            "Nuevo truco" to "Enseña un truco nuevo como dar la pata"
        ),
        TaskCategory.WELLNESS to listOf(
            "Momento de cariño" to "Dedica 10 minutos de mimos y caricias",
            "Música relajante" to "Pon música tranquila para tu perro",
            "Masaje canino" to "Un suave masaje para relajar a tu mascota",
            "Tiempo de juego" to "Juega con su juguete favorito en casa",
            "Espacio seguro" to "Verifica que su cama esté limpia y cómoda"
        )
    )

    override fun getTasksByDate(userId: String, date: Long): Flow<List<DailyTask>> =
        taskDao.getTasksByDate(userId, date).map { entities -> entities.map { it.toDomain() } }

    override fun getCompletedCountByDate(userId: String, date: Long): Flow<Int> =
        taskDao.getCompletedCountByDate(userId, date)

    override fun getTotalCountByDate(userId: String, date: Long): Flow<Int> =
        taskDao.getTotalCountByDate(userId, date)

    override suspend fun hasTasksForDate(userId: String, date: Long): Boolean =
        taskDao.hasTasksForDate(userId, date)

    override suspend fun generateDailyTasks(userId: String, date: Long) {
        if (taskDao.hasTasksForDate(userId, date)) return

        val tasks = mutableListOf<DailyTaskEntity>()
        val categories = TaskCategory.entries.toList()
        val seed = (date / 86400000).toInt() // day-based seed for variety

        categories.forEachIndexed { catIndex, category ->
            val pool = taskPool[category] ?: return@forEachIndexed
            val selectedIndex = (seed + catIndex) % pool.size
            val (title, description) = pool[selectedIndex]
            val reward = when (category) {
                TaskCategory.BASIC_CARE -> 5
                TaskCategory.HEALTH -> 10
                TaskCategory.EXERCISE -> 15
                TaskCategory.TRAINING -> 10
                TaskCategory.WELLNESS -> 5
            }
            tasks.add(
                DailyTaskEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = title,
                    description = description,
                    category = category.name,
                    rewardCoins = reward,
                    assignedDate = date
                )
            )
        }

        // Add 1-2 bonus tasks randomly
        val bonusCategory = categories[(seed + 7) % categories.size]
        val bonusPool = taskPool[bonusCategory] ?: emptyList()
        if (bonusPool.isNotEmpty()) {
            val bonusIndex = (seed + 3) % bonusPool.size
            val (title, desc) = bonusPool[bonusIndex]
            val existingTitles = tasks.map { it.title }
            if (title !in existingTitles) {
                tasks.add(
                    DailyTaskEntity(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        title = "$title (Bonus)",
                        description = desc,
                        category = bonusCategory.name,
                        rewardCoins = 20,
                        assignedDate = date
                    )
                )
            }
        }

        taskDao.insertTasks(tasks)
    }

    override suspend fun completeTask(taskId: String) {
        taskDao.completeTask(taskId, System.currentTimeMillis())
    }
}
