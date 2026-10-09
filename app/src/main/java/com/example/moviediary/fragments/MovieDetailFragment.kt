package com.example.moviediary.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.moviediary.R
import com.example.moviediary.models.Movie
import com.example.moviediary.viewmodels.MovieViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class MovieDetailFragment : Fragment(R.layout.fragment_movie_detail) {

    private val viewModel: MovieViewModel by activityViewModels()
    private var currentMovie: Movie? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movieId = arguments?.getLong("movieId") ?: return

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvYearGenre = view.findViewById<TextView>(R.id.tvYearGenre)
        val starsContainer = view.findViewById<LinearLayout>(R.id.starsContainer)
        val tvStatusBadge = view.findViewById<TextView>(R.id.tvStatusBadge)
        val metaRow = view.findViewById<LinearLayout>(R.id.metaRow)
        val tvTagsLabel = view.findViewById<TextView>(R.id.tvTagsLabel)
        val chipGroupTags = view.findViewById<ChipGroup>(R.id.chipGroupTags)
        val tvReviewLabel = view.findViewById<TextView>(R.id.tvReviewLabel)
        val tvReview = view.findViewById<TextView>(R.id.tvReview)
        val btnBack = view.findViewById<TextView>(R.id.btnBack)
        val btnDelete = view.findViewById<TextView>(R.id.btnDelete)

        view.findViewById<TextView>(R.id.btnEdit).setOnClickListener {
            val fragment = AddMovieFragment()
            fragment.arguments = Bundle().apply { putLong("movieId", movieId) }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        viewLifecycleOwner.lifecycleScope.launch {
            val movie = viewModel.getMovieById(movieId) ?: return@launch
            currentMovie = movie
            val genre = viewModel.getGenreById(movie.genreId)

            tvTitle.text = movie.title
            tvYearGenre.text = "${movie.year} · ${genre?.name ?: ""}"
            tvStatusBadge.text = if (movie.status == "WATCHED") "ПРОСМОТРЕНО" else "СМОТРЮ"

            val fullStars = ((movie.rating ?: 0f) / 2).toInt()
            for (i in 1..5) {
                val star = TextView(requireContext()).apply {
                    text = "★"
                    textSize = 18f
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            if (i <= fullStars) R.color.colorAccent else R.color.colorTextMuted
                        )
                    )
                }
                starsContainer.addView(star)
            }
            if (movie.status == "WATCHED" && movie.rating != null) {
                val ratingText = TextView(requireContext()).apply {
                    text = "${movie.rating.toInt()}/10"
                    textSize = 15f
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextMuted))
                    setPadding(12, 0, 0, 0)
                }
                starsContainer.addView(ratingText)
            }

            if (movie.watchedDate != null) {
                val dateText = android.text.format.DateFormat.format("dd.MM.yyyy", movie.watchedDate)
                addMetaItem(metaRow, "ПРОСМОТРЕНО", dateText.toString())
            } else {
                metaRow.visibility = View.GONE
            }

            if (!movie.review.isNullOrBlank()) {
                tvReviewLabel.visibility = View.VISIBLE
                tvReview.visibility = View.VISIBLE
                tvReview.text = movie.review
            }
        }

        viewModel.getTagsForMovie(movieId).observe(viewLifecycleOwner) { tags ->
            chipGroupTags.removeAllViews()
            if (tags.isNotEmpty()) {
                tvTagsLabel.visibility = View.VISIBLE
                tags.forEach { tag ->
                    val chip = Chip(requireContext()).apply {
                        text = "#${tag.name}"
                        isClickable = false
                        setChipBackgroundColorResource(R.color.colorAccentSoftSolid)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                        checkedIcon = null
                        chipStrokeWidth = 0f
                        elevation = 0f
                        rippleColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
                    }
                    chipGroupTags.addView(chip)
                }
            }
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Удалить фильм?")
                .setMessage("Вы уверены, что хотите удалить «${currentMovie?.title}»?")
                .setPositiveButton("Удалить") { _, _ ->
                    currentMovie?.let { viewModel.deleteMovie(it) }
                    parentFragmentManager.popBackStack()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun addMetaItem(container: LinearLayout, label: String, value: String) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }
        val lbl = TextView(requireContext()).apply {
            text = label
            textSize = 11f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextMuted))
        }
        val value_ = TextView(requireContext()).apply {
            text = value
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
        }
        layout.addView(lbl)
        layout.addView(value_)
        container.addView(layout)
    }
}