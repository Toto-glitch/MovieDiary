package com.example.moviediary.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.moviediary.models.Genre
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {

    @Insert
    suspend fun insert(genre: Genre): Long

    @Query("SELECT * FROM genres")
    fun gerAllGenres(): Flow<List<Genre>>
}