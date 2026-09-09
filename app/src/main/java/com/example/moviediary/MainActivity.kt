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
            Movie(1, "Начало", 2010, 8.8f),
            Movie(2, "Интерстеллар", 2014, 8.6f),
            Movie(3, "Матрица", 1999, 8.7f),
            Movie(4, "Джентльмены", 2019, 8.3f),
            Movie(5, "Довод", 2020, 7.4f)
        )

        val rvMovies = findViewById<RecyclerView>(R.id.rvMovie)
        rvMovies.layoutManager = LinearLayoutManager(this)
        rvMovies.adapter = MovieAdapter(movies)
    }
}