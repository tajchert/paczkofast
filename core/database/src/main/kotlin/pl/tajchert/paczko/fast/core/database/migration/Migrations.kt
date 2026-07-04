package pl.tajchert.paczko.fast.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** v2 → v3: multi-compartment + ownership metadata. */
private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `parcels` ADD COLUMN `multiCompartmentUuid` TEXT")
        db.execSQL("ALTER TABLE `parcels` ADD COLUMN `multiPackageShipmentNumbers` TEXT")
        db.execSQL("ALTER TABLE `parcels` ADD COLUMN `ownershipStatus` TEXT")
    }
}

/**
 * v3 → v4: drop the unused `mobileCollectPossible` column. SQLite only supports
 * DROP COLUMN on recent versions, so recreate the table for broad compatibility.
 */
private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `parcels_new` (" +
                "`shipmentNumber` TEXT NOT NULL, `status` TEXT NOT NULL, `statusGroup` TEXT, " +
                "`openCode` TEXT, `qrCode` TEXT, `pickupPointName` TEXT, `pickupPointDescription` TEXT, " +
                "`pickupPointAddress` TEXT, `pickupPointLatitude` REAL, `pickupPointLongitude` REAL, " +
                "`expiryDate` TEXT, `storedDate` TEXT, `collectOperation` INTEGER NOT NULL, " +
                "`multiCompartmentUuid` TEXT, `multiPackageShipmentNumbers` TEXT, `ownershipStatus` TEXT, " +
                "PRIMARY KEY(`shipmentNumber`))",
        )
        db.execSQL(
            "INSERT INTO `parcels_new` (" +
                "`shipmentNumber`, `status`, `statusGroup`, `openCode`, `qrCode`, `pickupPointName`, " +
                "`pickupPointDescription`, `pickupPointAddress`, `pickupPointLatitude`, `pickupPointLongitude`, " +
                "`expiryDate`, `storedDate`, `collectOperation`, `multiCompartmentUuid`, " +
                "`multiPackageShipmentNumbers`, `ownershipStatus`) " +
                "SELECT `shipmentNumber`, `status`, `statusGroup`, `openCode`, `qrCode`, `pickupPointName`, " +
                "`pickupPointDescription`, `pickupPointAddress`, `pickupPointLatitude`, `pickupPointLongitude`, " +
                "`expiryDate`, `storedDate`, `collectOperation`, `multiCompartmentUuid`, " +
                "`multiPackageShipmentNumbers`, `ownershipStatus` FROM `parcels`",
        )
        db.execSQL("DROP TABLE `parcels`")
        db.execSQL("ALTER TABLE `parcels_new` RENAME TO `parcels`")
    }
}

/** v4 → v5: sender + size columns for list/detail display. */
private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `parcels` ADD COLUMN `senderName` TEXT")
        db.execSQL("ALTER TABLE `parcels` ADD COLUMN `parcelSize` TEXT")
    }
}

/** v5 → v6: pickup / returned-to-sender dates for history. */
private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `parcels` ADD COLUMN `pickUpDate` TEXT")
        db.execSQL("ALTER TABLE `parcels` ADD COLUMN `returnedToSenderDate` TEXT")
    }
}

/** v6 → v7: offline cache for parcel detail fields + tracking timeline. */
private val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `parcel_details` (" +
                "`shipmentNumber` TEXT NOT NULL, `sizeCode` TEXT, `senderName` TEXT, " +
                "`shipmentType` TEXT, PRIMARY KEY(`shipmentNumber`))",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `tracking_events` (" +
                "`shipmentNumber` TEXT NOT NULL, `position` INTEGER NOT NULL, `status` TEXT NOT NULL, " +
                "`date` TEXT, PRIMARY KEY(`shipmentNumber`, `position`))",
        )
    }
}

/**
 * Explicit schema migrations for [pl.tajchert.paczko.fast.core.database.PaczkofastDatabase].
 *
 * These replace the previous `fallbackToDestructiveMigration`, which wiped the
 * offline cache on every schema bump. Each migration corresponds to an exported
 * schema under `core/database/schemas/…` and preserves cached data.
 */
internal val MIGRATIONS: Array<Migration> = arrayOf(
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
)
