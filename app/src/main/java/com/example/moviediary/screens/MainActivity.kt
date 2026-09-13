package com.example.moviediary.screens

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviediary.R
import com.example.moviediary.adapters.MovieAdapter
import com.example.moviediary.viewmodels.MovieViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MovieViewModel by viewModels()
    private lateinit var adapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = MovieAdapter(emptyList())
        val rvMovies = findViewById<RecyclerView>(R.id.rvMovie)
        rvMovies.layoutManager = LinearLayoutManager(this)
        rvMovies.adapter = adapter

        viewModel.allMovies.observe(this) {
            movies -> adapter.updateMovies(movies)
        }
    }
}