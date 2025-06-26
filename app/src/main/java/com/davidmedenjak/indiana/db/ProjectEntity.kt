package com.davidmedenjak.indiana.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "project",
    indices = [
        Index("project_type")
    ]
)
data class ProjectEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "avatar") val avatar: String?,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "project_type") val projectType: String?,
)
