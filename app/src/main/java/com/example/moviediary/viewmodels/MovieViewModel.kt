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

    fun insert(movie: Movie) = viewModelScope.launch {
        movieDao.insert(movie)
    }

    fun insertMovieWithTags(movie: Movie, tagNames: List<String>) = viewModelScope.launch {
        val movieId = movieDao.insert(movie);
        tagNames.forEach { tagName ->
            val existing = tagDao.findByName(tagName);
            val tagId = existing?.id ?: tagDao.insert(Tag(name = tagName))
            movieTagDao.insertRef(MovieTagRef(movieId = movieId, tagId = tagId))
        }
    }
}
