package com.example.moviediary.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.moviediary.models.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert
    suspend fun insert(tag: Tag): Long

    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): Tag?
}