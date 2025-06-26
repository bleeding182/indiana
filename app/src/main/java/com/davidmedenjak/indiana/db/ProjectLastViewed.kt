package com.davidmedenjak.indiana.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "project_last_viewed",
    indices = [
        Index("last_viewed", orders = arrayOf(Index.Order.DESC))
    ],
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("project_id"),
            onDelete = ForeignKey.Companion.CASCADE,
            onUpdate = ForeignKey.Companion.NO_ACTION,
            deferred = false,
        )
    ]
)
data class ProjectLastViewed(
    @PrimaryKey @ColumnInfo(name = "project_id") val id: String,
    @ColumnInfo(name = "last_viewed") val lastViewed: Instant?,
)
