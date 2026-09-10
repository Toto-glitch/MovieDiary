package com.example.moviediary.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.moviediary.models.Movie
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Insert
    suspend fun insert(movie: Movie): Long

    @Update
    suspend fun update(movie: Movie)

    @Delete
    suspend fun delete(movie: Movie)

    @Query("SELECT * FROM movies ORDER BY year DESC")
    fun getAllMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Long): Movie?

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%'")
    fun findMovies(query: String): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE genreId = :genreId")
    fun getMoviesByGenre(genreId: Long): Flow<List<Movie>>
}