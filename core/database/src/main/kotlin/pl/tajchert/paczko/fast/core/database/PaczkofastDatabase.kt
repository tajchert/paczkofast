package pl.tajchert.paczko.fast.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.tajchert.paczko.fast.core.database.dao.ParcelDao
import pl.tajchert.paczko.fast.core.database.dao.ParcelDetailsDao
import pl.tajchert.paczko.fast.core.database.entity.ParcelDetailsEntity
import pl.tajchert.paczko.fast.core.database.entity.ParcelEntity
import pl.tajchert.paczko.fast.core.database.entity.TrackingEventEntity

@Database(
    entities = [
        ParcelEntity::class,
        ParcelDetailsEntity::class,
        TrackingEventEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
abstract class PaczkofastDatabase : RoomDatabase() {
    abstract fun parcelDao(): ParcelDao
    abstract fun parcelDetailsDao(): ParcelDetailsDao
}
