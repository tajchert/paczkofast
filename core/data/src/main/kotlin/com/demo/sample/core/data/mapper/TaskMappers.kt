package com.demo.sample.core.data.mapper

import com.demo.sample.core.database.entity.TaskEntity
import com.demo.sample.core.model.Task
import com.demo.sample.core.model.TaskPriority
import com.demo.sample.core.network.dto.NetworkTask
import kotlin.time.Instant

/**
 * Mapping functions between network DTOs, database entities, and domain models.
 *
 * ## Architecture
 *
 * We have three representations of a Task:
 * 1. **NetworkTask**: What the API returns (with @Serializable)
 * 2. **TaskEntity**: What Room stores (with @Entity)
 * 3. **Task**: What the app works with (clean domain model)
 *
 * These mappers convert between representations at layer boundaries.
 *
 * ## Why Not Just Use One Model?
 *
 * Separate models allow:
 * - API changes without affecting the whole app
 * - Database schema changes isolated to data layer
 * - Domain model stays clean without framework annotations
 */

// =============================================================================
// Network DTO → Database Entity
// =============================================================================

/**
 * Converts a network DTO to a database entity.
 *
 * Used when syncing data from the network to local storage.
 */
fun NetworkTask.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    createdAt = Instant.parse(createdAt).toEpochMilli(),
    priority = priority.uppercase(),
)

/**
 * Extension to convert Instant to epoch milliseconds.
 */
private fun Instant.toEpochMilli(): Long =
    (this - Instant.fromEpochMilliseconds(0)).inWholeMilliseconds

// =============================================================================
// Domain Model → Network DTO
// =============================================================================

/**
 * Converts a domain model to a network DTO.
 *
 * Used when sending data to the server.
 */
fun Task.toNetworkDto(): NetworkTask = NetworkTask(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    createdAt = createdAt.toString(),
    priority = priority.name,
)

// =============================================================================
// Note: Entity ↔ Domain mappings are in core:database module
// to keep dependencies clean (database doesn't depend on data)
// =============================================================================
