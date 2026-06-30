package pl.tajchert.paczko.fast.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.tajchert.paczko.fast.core.database.dao.ParcelDao
import pl.tajchert.paczko.fast.core.database.entity.ParcelEntity

@Database(
    entities = [ParcelEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class PaczkofastDatabase : RoomDatabase() {
    abstract fun parcelDao(): ParcelDao
}
