package pl.tajchert.paczko.fast.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.tajchert.paczko.fast.core.database.dao.TaskDao
import pl.tajchert.paczko.fast.core.database.entity.TaskEntity

/**
 * Room database for the Paczkofast app.
 *
 * ## Architecture Role
 *
 * This database is the **single source of truth** for task data.
 * In our offline-first architecture:
 * - UI reads always come from this database
 * - Network syncs update this database
 * - The database emits changes via Flow to the UI
 *
 * ## Schema Export
 *
 * We set `exportSchema = true` to:
 * 1. Enable migration testing
 * 2. Track schema history in version control
 * 3. Auto-generate migration helpers
 *
 * Schemas are exported to `core/database/schemas/`
 *
 * ## Adding New Entities
 *
 * When adding a new entity:
 * 1. Add it to the [entities] array
 * 2. Create an abstract DAO function
 * 3. Increment the version number
 * 4. Add migration if needed (or use `fallbackToDestructiveMigration` in dev)
 *
 * ## Migrations
 *
 * For production apps, always create proper migrations:
 *
 * ```kotlin
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(database: SupportSQLiteDatabase) {
 *         database.execSQL("ALTER TABLE tasks ADD COLUMN due_date INTEGER")
 *     }
 * }
 * ```
 */
@Database(
    entities = [
        TaskEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class TaskDatabase : RoomDatabase() {

    /**
     * Get the DAO for task operations.
     */
    abstract fun taskDao(): TaskDao
}
