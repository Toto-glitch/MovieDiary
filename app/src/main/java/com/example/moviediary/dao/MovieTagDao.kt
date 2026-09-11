package com.example.moviediary.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.moviediary.models.MovieTagRef
import com.example.moviediary.models.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieTagDao {

    @Insert
    suspend fun insertRef(ref: MovieTagRef)

    @Delete
    suspend fun deleteRef(ref: MovieTagRef)

    @Query("""
        SELECT tags.*
        FROM tags INNER JOIN movie_tags ON tags.id = movie_tags.tagId
        WHERE movie_tags.movieId = :movieId
    """)
    fun getTagsForMovie(movieId: Long): Flow<List<Tag>>
}