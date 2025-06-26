package com.davidmedenjak.indiana.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant

@Database(
    entities = [
        ProjectEntity::class,
        ProjectLastViewed::class,
    ],
    version = 1,
    exportSchema = true,
    autoMigrations = [
    ],
)
@TypeConverters(TimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projects(): ProjectDao
}

class TimeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? =
        value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? =
        date?.toEpochMilli()
}
