package com.demo.sample.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network DTO (Data Transfer Object) for tasks.
 *
 * ## Why Separate from Domain Model?
 *
 * Network DTOs are separate from domain models because:
 *
 * 1. **API Shape Independence**: The API response format may differ from
 *    what the app needs. For example, the API uses `is_completed` but
 *    internally we use `isCompleted`.
 *
 * 2. **Serialization Annotations**: @Serializable, @SerialName annotations
 *    stay in the network layer. Domain models remain clean.
 *
 * 3. **Versioning**: If the API changes, we update DTOs and mappers,
 *    not the entire codebase.
 *
 * ## Kotlinx Serialization
 *
 * We use kotlinx.serialization instead of Gson/Moshi because:
 * - Compile-time safety (no runtime reflection)
 * - Kotlin-first design (handles nullability properly)
 * - Smaller binary size
 * - Works with Kotlin Multiplatform
 */
@Serializable
data class NetworkTask(
    /**
     * Unique identifier from the server.
     */
    val id: String,

    /**
     * Task title.
     */
    val title: String,

    /**
     * Task description.
     */
    val description: String,

    /**
     * Whether the task is completed.
     * Using @SerialName because API uses snake_case.
     */
    @SerialName("is_completed")
    val isCompleted: Boolean,

    /**
     * ISO-8601 formatted creation timestamp.
     * Example: "2024-01-15T10:30:00Z"
     */
    @SerialName("created_at")
    val createdAt: String,

    /**
     * Priority as a string: "LOW", "MEDIUM", or "HIGH".
     */
    val priority: String,
)

/**
 * API response wrapper for list endpoints.
 *
 * Many APIs wrap their responses:
 * ```json
 * {
 *   "data": [...],
 *   "meta": { "total": 100, "page": 1 }
 * }
 * ```
 */
@Serializable
data class NetworkResponse<T>(
    val data: T,
)
