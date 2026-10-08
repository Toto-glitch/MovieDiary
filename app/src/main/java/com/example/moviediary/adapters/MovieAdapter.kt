package com.example.moviediary.adapters

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moviediary.R
import com.example.moviediary.models.Movie

class MovieAdapter(
    private var movies: List<Movie> = emptyList(),
    private var genreNames: Map<Long, String> = emptyMap(),
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val posterLetter: TextView = view.findViewById(R.id.tvPosterLetter)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val sub: TextView = view.findViewById(R.id.tvSub)
        val stars: TextView = view.findViewById(R.id.tvStars)
        val badge: TextView = view.findViewById(R.id.tvBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        val ctx = holder.itemView.context
        val accent = ContextCompat.getColor(ctx, R.color.colorAccent)
        val muted = ContextCompat.getColor(ctx, R.color.colorTextMuted)

        holder.posterLetter.text = movie.title.take(1).uppercase()
        holder.title.text = movie.title
        holder.sub.text = "${movie.year} · ${genreNames[movie.genreId] ?: ""}"

        val rating = movie.rating
        if (movie.status == "WATCHED" && rating != null && rating > 0) {
            val filled = Math.round(rating / 2f).coerceIn(0, 5)
            val stars = SpannableString("★★★★★")
            if (filled > 0) stars.setSpan(ForegroundColorSpan(accent), 0, filled, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (filled < 5) stars.setSpan(ForegroundColorSpan(muted), filled, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.stars.text = stars
        } else {
            holder.stars.text = "—"
            holder.stars.setTextColor(muted)
        }

        if (movie.status == "WATCHED") {
            holder.badge.text = "Просмотрено"
            holder.badge.setTextColor(accent)
            holder.badge.setBackgroundResource(R.drawable.bg_badge)
        } else {
            holder.badge.text = "Смотрю"
            holder.badge.setTextColor(ContextCompat.getColor(ctx, R.color.colorBlue))
            holder.badge.setBackgroundResource(R.drawable.bg_badge_blue)
        }

        holder.itemView.setOnClickListener { onMovieClick(movie) }
    }

    override fun getItemCount(): Int = movies.size

    fun update(newMovies: List<Movie>, newGenreNames: Map<Long, String>) {
        movies = newMovies
        genreNames = newGenreNames
        notifyDataSetChanged()
    }
}
