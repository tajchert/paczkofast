package pl.tajchert.paczko.fast.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.tajchert.paczko.fast.core.model.Task
import pl.tajchert.paczko.fast.core.model.TaskPriority
import kotlin.time.Instant

/**
 * Room entity representing a task in the database.
 *
 * ## Why Separate from Domain Model?
 *
 * We maintain separate entities for the database layer because:
 *
 * 1. **Schema Independence**: Database schema can evolve without affecting
 *    the rest of the app. For example, we store [createdAt] as epoch millis
 *    for efficiency, but the domain model uses [Instant].
 *
 * 2. **Annotation Isolation**: Room annotations (@Entity, @ColumnInfo) stay
 *    in the data layer. Domain models remain pure Kotlin.
 *
 * 3. **Migration Safety**: Database migrations reference entities, not
 *    domain models. This prevents breaking changes in domain from
 *    breaking migrations.
 *
 * ## Column Naming Convention
 *
 * We use snake_case for column names (@ColumnInfo) to follow SQL conventions.
 * This also makes database inspection tools easier to read.
 *
 * ## Mapping
 *
 * See [toModel] and [Task.toEntity] extension functions below for
 * converting between entity and domain model.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    /**
     * Unique identifier for the task.
     * Using String (UUID) instead of Long for easier sync with remote APIs.
     */
    @PrimaryKey
    val id: String,

    /**
     * Task title - brief description of what needs to be done.
     */
    val title: String,

    /**
     * Optional detailed description.
     */
    val description: String,

    /**
     * Whether the task has been completed.
     */
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,

    /**
     * When the task was created, stored as epoch milliseconds.
     *
     * WHY epoch millis instead of Instant:
     * Room doesn't have built-in Instant support. While we could use
     * TypeConverters, storing as Long is simpler and more portable.
     */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    /**
     * Task priority stored as the enum name string.
     *
     * WHY String instead of Int ordinal:
     * String is more readable in the database and survives enum reordering.
     * If we used ordinal, inserting a new priority would shift all values.
     */
    val priority: String,
)

// =============================================================================
// MAPPING EXTENSIONS
// =============================================================================

/**
 * Converts a database entity to a domain model.
 *
 * This is the primary way to get domain models from the database.
 * DAOs return entities, which are mapped to domain models in repositories.
 */
fun TaskEntity.toModel(): Task = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    priority = TaskPriority.valueOf(priority),
)

/**
 * Converts a domain model to a database entity.
 *
 * Used when saving domain models to the database.
 */
fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    createdAt = createdAt.toEpochMilli(),
    priority = priority.name,
)

/**
 * Extension to convert Instant to epoch milliseconds.
 */
private fun Instant.toEpochMilli(): Long =
    (this - Instant.fromEpochMilliseconds(0)).inWholeMilliseconds
