package com.example.moviediary.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.moviediary.AppDatabase
import com.example.moviediary.models.Movie
import kotlinx.coroutines.launch

class MovieViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getDatabase(app).movieDao()
    val allMovies: LiveData<List<Movie>> = dao.getAllMovies().asLiveData()

    fun insert(movie: Movie) = viewModelScope.launch {
        dao.insert(movie)
    }
}
