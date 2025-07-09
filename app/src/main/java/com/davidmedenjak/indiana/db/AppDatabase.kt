package com.davidmedenjak.indiana.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant

@Database(
    entities = [
        ProjectEntity::class,
        ProjectLastViewed::class,
        DownloadEntity::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
)
@TypeConverters(TimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projects(): ProjectDao
    abstract fun downloads(): DownloadDao
}

class TimeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? =
        value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? =
        date?.toEpochMilli()
}
