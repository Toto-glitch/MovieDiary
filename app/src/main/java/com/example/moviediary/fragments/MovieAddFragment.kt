package com.example.moviediary.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.moviediary.R
import com.example.moviediary.viewmodels.MovieViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import androidx.core.view.isEmpty
import com.example.moviediary.models.Movie
import java.util.Calendar

class AddMovieFragment : Fragment(R.layout.fragment_add_movie) {

    private val viewModel: MovieViewModel by activityViewModels()
    private var selectedGenreId: Long? = null
    private var selectedGenreChip: Chip? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etYear = view.findViewById<EditText>(R.id.etYear)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupGenre)
        val rbWatching = view.findViewById<RadioButton>(R.id.rbWatching)
        val rbWatched = view.findViewById<RadioButton>(R.id.rbWatched)
        val tvDateLabel = view.findViewById<TextView>(R.id.tvDateLabel)
        val etDate = view.findViewById<EditText>(R.id.etDate)
        val tvRatingLabel = view.findViewById<TextView>(R.id.tvRatingLabel)
        val etTags = view.findViewById<EditText>(R.id.etTags)
        val etReview = view.findViewById<EditText>(R.id.etReview)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)
        val btnSave = view.findViewById<TextView>(R.id.btnSave)
        val ratingStarsContainer = view.findViewById<LinearLayout>(R.id.ratingStarsContainer)
        var currentRating = 5
        val starViews = mutableListOf<TextView>()

        fun refreshStars() {
            starViews.forEachIndexed { index, star ->
                val filled = index < currentRating
                star.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (filled) R.color.colorAccent else R.color.colorTextMuted
                    )
                )
            }
            tvRatingLabel.text = "ОЦЕНКА — $currentRating/10"
        }

        for (i in 1..10) {
            val star = TextView(requireContext()).apply {
                text = "★"
                textSize = 26f
                setPadding(4, 0, 4, 0)
                setOnClickListener {
                    currentRating = i
                    refreshStars()
                }
            }
            starViews.add(star)
            ratingStarsContainer.addView(star)
        }
        refreshStars()

        etYear.setText(Calendar.getInstance().get(Calendar.YEAR).toString())

        viewModel.allGenres.observe(viewLifecycleOwner) { genres ->
            if (chipGroup.isEmpty()) {
                genres.forEach { genre ->
                    val chip = Chip(requireContext()).apply {
                        text = genre.name
                        isCheckable = true
                        isClickable = true
                        setChipBackgroundColorResource(R.color.colorSurface)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
                        chipStrokeWidth = 1f
                        setChipStrokeColorResource(R.color.colorBorder)
                        checkedIcon = null
                    }
                    chip.setOnClickListener {
                        selectedGenreChip?.let {
                            it.setChipBackgroundColorResource(R.color.colorSurface)
                            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
                        }
                        chip.setChipBackgroundColorResource(R.color.colorAccent)
                        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBackground))
                        selectedGenreChip = chip
                        selectedGenreId = genre.id
                    }
                    chipGroup.addView(chip)
                }
            }
        }

        fun updateStatusUi(watchedSelected: Boolean) {
            rbWatched.background = if (watchedSelected)
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected) else null
            rbWatching.background = if (!watchedSelected)
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected) else null
            tvDateLabel.visibility = if (watchedSelected) View.VISIBLE else View.GONE
            etDate.visibility = if (watchedSelected) View.VISIBLE else View.GONE
        }
        updateStatusUi(watchedSelected = true)

        rbWatched.setOnClickListener { updateStatusUi(true) }
        rbWatching.setOnClickListener { updateStatusUi(false) }

        var pickedDateMillis: Long? = null

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, day, 0, 0, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    pickedDateMillis = cal.timeInMillis
                    etDate.setText("%02d.%02d.%04d".format(day, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            var hasError = false

            if (title.isEmpty()) {
                etTitle.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_input_error)
                hasError = true
            } else {
                etTitle.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_input)
            }
            if (selectedGenreId == null) {
                Toast.makeText(requireContext(), "Выберите жанр фильма", Toast.LENGTH_SHORT).show()
                hasError = true
            }
            if (hasError) {
                if (title.isEmpty()) Toast.makeText(requireContext(), "Введите название фильма", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val movie = Movie(
                title = title,
                year = etYear.text.toString().toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR),
                status = if (rbWatched.background != null) "WATCHED" else "PLANNED",
                watchedDate = if (rbWatched.background != null) pickedDateMillis else null,
                rating = if (rbWatched.background != null) currentRating.toFloat() else null,
                review = etReview.text.toString().trim().ifEmpty { null },
                genreId = selectedGenreId!!
            )
            val tags = etTags.text.toString().split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            viewModel.insertMovieWithTags(movie, tags)
            parentFragmentManager.popBackStack()
        }
    }
}