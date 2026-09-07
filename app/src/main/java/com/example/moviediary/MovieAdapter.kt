package com.example.moviediary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MovieViewHandler(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.movieTitle)
    val year: TextView = view.findViewById(R.id.movieYear)
    val rating: TextView = view.findViewById(R.id.movieRating)
}


class MovieAdapter(private val movies: List<Movie>) : RecyclerView.Adapter<MovieViewHandler>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHandler {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return MovieViewHandler(view)
    }

    override fun onBindViewHolder(holder: MovieViewHandler, position: Int) {
        val movie = movies[position]
        holder.title.text = movie.title
        holder.year.text = movie.year.toString()
        holder.rating.text = "⭐ ${movie.rating}"
    }

    override fun getItemCount(): Int = movies.size
}
