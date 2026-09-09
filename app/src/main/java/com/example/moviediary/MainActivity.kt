package com.example.moviediary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviediary.adapters.MovieAdapter
import com.example.moviediary.models.Movie

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val movies = listOf(
            Movie(id = 1, title = "Начало", year = 2010, rating = 8.8f, status = "WATCHED", genreId = 1),
            Movie(id = 2, title = "Интерстеллар", year = 2014, rating = 8.6f, status = "WATCHED", genreId = 1),
            Movie(id = 3, title = "Матрица", year = 1999, rating = 8.7f, status = "WATCHED", genreId = 1),
            Movie(id = 4, title = "Джентльмены", year = 2019, rating = 8.3f, status = "PLANNED", genreId = 2),
            Movie(id = 5, title = "Довод", year = 2020, rating = 7.4f, status = "PLANNED", genreId = 2)
        )

        val rvMovies = findViewById<RecyclerView>(R.id.rvMovie)
        rvMovies.layoutManager = LinearLayoutManager(this)
        rvMovies.adapter = MovieAdapter(movies)
    }
}