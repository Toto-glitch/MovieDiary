package com.example.moviediary.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.moviediary.AppDatabase
import com.example.moviediary.models.Genre
import com.example.moviediary.models.Movie
import kotlinx.coroutines.launch

class MovieViewModel(app: Application) : AndroidViewModel(app) {

    private val movieDao = AppDatabase.getDatabase(app).movieDao()
    private val genreDao = AppDatabase.getDatabase(app).genreDao()
    private val tagDao = AppDatabase.getDatabase(app).tagDao()
    private val movieTagDao = AppDatabase.getDatabase(app).movieTagDao()
    val allMovies: LiveData<List<Movie>> = movieDao.getAllMovies().asLiveData()
    val allGenres: LiveData<List<Genre>> = genreDao.getAllGenres().asLiveData()

    fun insert(movie: Movie) = viewModelScope.launch {
        dao.insert(movie)
    }
}
