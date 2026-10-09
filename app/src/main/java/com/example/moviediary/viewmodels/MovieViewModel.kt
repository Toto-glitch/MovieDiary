package com.example.moviediary.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.moviediary.AppDatabase
import com.example.moviediary.models.Genre
import com.example.moviediary.models.Movie
import com.example.moviediary.models.MovieTagRef
import com.example.moviediary.models.Tag
import kotlinx.coroutines.launch

class MovieViewModel(app: Application) : AndroidViewModel(app) {

    private val movieDao = AppDatabase.getDatabase(app).movieDao()
    private val genreDao = AppDatabase.getDatabase(app).genreDao()
    private val tagDao = AppDatabase.getDatabase(app).tagDao()
    private val movieTagDao = AppDatabase.getDatabase(app).movieTagDao()
    val allMovies: LiveData<List<Movie>> = movieDao.getAllMovies().asLiveData()
    val allGenres: LiveData<List<Genre>> = genreDao.getAllGenres().asLiveData()

    suspend fun getMovieById(id: Long): Movie? = movieDao.getMovieById(id)
    suspend fun getGenreById(id: Long): Genre? = genreDao.getGenreById(id)
    fun getTagsForMovie(movieId: Long): LiveData<List<Tag>> = movieTagDao.getTagsForMovie(movieId).asLiveData()
    fun deleteMovie(movie: Movie) = viewModelScope.launch { movieDao.delete(movie) }
    fun deleteAllMovies() = viewModelScope.launch { movieDao.deleteAll() }

    private suspend fun linkTags(movieId: Long, tagNames: List<String>) {
        tagNames.forEach { tagName ->
            val existing = tagDao.findByName(tagName)
            val tagId = existing?.id ?: tagDao.insert(Tag(name = tagName))
            movieTagDao.insertRef(MovieTagRef(movieId = movieId, tagId = tagId))
        }
    }

    fun insertMovieWithTags(movie: Movie, tagNames: List<String>) = viewModelScope.launch {
        val movieId = movieDao.insert(movie)
        linkTags(movieId, tagNames)
    }

    fun updateMovieWithTags(movie: Movie, tagNames: List<String>) = viewModelScope.launch {
        movieDao.update(movie)
        movieTagDao.deleteAllForMovie(movie.id)
        linkTags(movie.id, tagNames)
    }

    suspend fun getTagNamesForMovie(movieId: Long): List<String> =
        movieTagDao.getTagsForMovieOnce(movieId).map { it.name }
}
