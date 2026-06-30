package com.demo.sample.core.model

import kotlin.time.Instant

/**
 * Domain model representing a task/note in the application.
 *
 * ## Architecture Note
 *
 * This is a **domain model** - it represents business entities in their purest form.
 * Domain models are:
 * - Located in the innermost layer of clean architecture
 * - Free from framework dependencies (no Android, no Room, no Retrofit annotations)
 * - Shared across all other layers (UI, data, domain)
 *
 * ## Why Separate from Database Entities and Network DTOs?
 *
 * We maintain separate models for each layer:
 * - **Domain Model (this)**: What the business logic works with
 * - **Database Entity**: What Room stores (has @Entity, @ColumnInfo annotations)
 * - **Network DTO**: What the API returns (has @Serializable, @SerialName annotations)
 *
 * This separation allows:
 * 1. API changes don't ripple through the entire codebase
 * 2. Database schema changes are isolated to the data layer
 * 3. Business logic remains clean and testable
 *
 * @property id Unique identifier for the task (UUID string)
 * @property title Short title of the task
 * @property description Detailed description (can be empty)
 * @property isCompleted Whether the task has been completed
 * @property createdAt When the task was created
 * @property priority Priority level affecting sort order
 */
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val createdAt: Instant,
    val priority: TaskPriority,
)
