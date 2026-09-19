package com.example.moviediary.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviediary.R
import com.example.moviediary.adapters.MovieAdapter
import com.example.moviediary.viewmodels.MovieViewModel

class MovieListFragment : Fragment(R.layout.fragment_movie_list) {
    private val viewModel: MovieViewModel by activityViewModels()
    private lateinit var adapter: MovieAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MovieAdapter(emptyList())
        val rvMovies = view.findViewById<RecyclerView>(R.id.rvMovies)
        rvMovies.layoutManager = LinearLayoutManager(requireContext())
        rvMovies.adapter = adapter

        viewModel.allMovies.observe(viewLifecycleOwner) { movies ->
            adapter.updateMovies(movies)
        }
    }
}
