package com.davidmedenjak.indiana.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Upsert
    suspend fun upsert(project: List<ProjectEntity>)

    @Upsert
    suspend fun updateLastViewed(projectLastViewed: ProjectLastViewed)

    @Query(
        """
        SELECT * FROM project_last_viewed
            LEFT JOIN project ON project_id = id 
            WHERE last_viewed IS NOT NULL
            ORDER BY last_viewed DESC LIMIT :limit
        """
    )
    fun lastViewed(limit: Int): Flow<List<ProjectEntity>>

    @Query(
        """
        SELECT DISTINCT project_type from project
            WHERE project_type IS NOT NULL
            ORDER BY project_type
    """
    )
    fun projectTypes(): Flow<List<String>>
}
