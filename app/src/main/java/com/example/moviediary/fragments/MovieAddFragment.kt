package com.example.moviediary.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
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
import java.util.Calendar

class AddMovieFragment : Fragment(R.layout.fragment_add_movie) {

    private val viewModel: MovieViewModel by activityViewModels()

    private var selectedGenreId: Long? = null
    private var selectedGenreChip: Chip? = null
    private var pickedDateMillis: Long? = null
    private var currentRating = 5
    private var isWatched = true

    private var editingMovieId: Long? = null
    private var editingMovie: Movie? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etYear = view.findViewById<EditText>(R.id.etYear)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupGenre)
        val rbWatching = view.findViewById<RadioButton>(R.id.rbWatching)
        val rbWatched = view.findViewById<RadioButton>(R.id.rbWatched)
        val tvDateLabel = view.findViewById<TextView>(R.id.tvDateLabel)
        val etDate = view.findViewById<EditText>(R.id.etDate)
        val tvRatingLabel = view.findViewById<TextView>(R.id.tvRatingLabel)
        val ratingStarsContainer = view.findViewById<LinearLayout>(R.id.ratingStarsContainer)
        val etTags = view.findViewById<EditText>(R.id.etTags)
        val etReview = view.findViewById<EditText>(R.id.etReview)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)
        val btnSave = view.findViewById<TextView>(R.id.btnSave)

        etYear.setText(Calendar.getInstance().get(Calendar.YEAR).toString())

        fun updateStatusUi(watched: Boolean) {
            isWatched = watched
            rbWatched.background = if (watched)
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected) else null
            rbWatching.background = if (!watched)
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_selected) else null
            tvDateLabel.visibility = if (watched) View.VISIBLE else View.GONE
            etDate.visibility = if (watched) View.VISIBLE else View.GONE
        }
        updateStatusUi(true)
        rbWatched.setOnClickListener { updateStatusUi(true) }
        rbWatching.setOnClickListener { updateStatusUi(false) }

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

        val starViews = mutableListOf<TextView>()
        fun refreshStars() {
            starViews.forEachIndexed { index, star ->
                star.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (index < currentRating) R.color.colorAccent else R.color.colorTextMuted
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

        fun selectGenreChip(chip: Chip, genreId: Long) {
            selectedGenreChip?.let {
                it.setChipBackgroundColorResource(R.color.colorSurface)
                it.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
            }
            chip.setChipBackgroundColorResource(R.color.colorAccent)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBackground))
            selectedGenreChip = chip
            selectedGenreId = genreId
        }

        viewModel.allGenres.observe(viewLifecycleOwner) { genres ->
            if (chipGroup.childCount == 0) {
                genres.forEach { genre ->
                    val chip = Chip(requireContext()).apply {
                        text = genre.name
                        isCheckable = false
                        isClickable = true
                        setChipBackgroundColorResource(R.color.colorSurface)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
                        chipStrokeWidth = 1f
                        setChipStrokeColorResource(R.color.colorBorder)
                        checkedIcon = null
                        tag = genre.id
                    }
                    chip.setOnClickListener { selectGenreChip(chip, genre.id) }
                    chipGroup.addView(chip)
                }
            }
            editingMovie?.let { m ->
                chipGroup.findViewWithTag<Chip>(m.genreId)?.let { selectGenreChip(it, m.genreId) }
            }
        }
        btnCancel.setOnClickListener { parentFragmentManager.popBackStack() }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            var hasError = false

            if (title.isEmpty()) {
                etTitle.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_input_error)
                Toast.makeText(requireContext(), "Введите название фильма", Toast.LENGTH_SHORT).show()
                hasError = true
            } else {
                etTitle.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_input)
            }
            if (selectedGenreId == null) {
                if (!hasError) Toast.makeText(requireContext(), "Выберите жанр фильма", Toast.LENGTH_SHORT).show()
                hasError = true
            }
            if (hasError) return@setOnClickListener

            val movie = Movie(
                id = editingMovieId ?: 0,
                title = title,
                year = etYear.text.toString().toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR),
                posterUrl = editingMovie?.posterUrl,
                durationMinutes = editingMovie?.durationMinutes,
                status = if (isWatched) "WATCHED" else "PLANNED",
                watchedDate = if (isWatched) pickedDateMillis else null,
                rating = if (isWatched) currentRating.toFloat() else null,
                review = etReview.text.toString().trim().ifEmpty { null },
                genreId = selectedGenreId!!
            )
            val tags = etTags.text.toString().split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()

            if (editingMovieId != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.updateMovieWithTags(movie, tags).join()
                    parentFragmentManager.popBackStack()
                }
            } else {
                viewModel.insertMovieWithTags(movie, tags)
                parentFragmentManager.popBackStack()
            }
        }

        val movieId = arguments?.getLong("movieId", -1L) ?: -1L
        if (movieId != -1L) {
            editingMovieId = movieId
            tvHeaderTitle.text = "Редактировать"
            viewLifecycleOwner.lifecycleScope.launch {
                val movie = viewModel.getMovieById(movieId) ?: return@launch
                editingMovie = movie

                etTitle.setText(movie.title)
                etYear.setText(movie.year.toString())
                etReview.setText(movie.review ?: "")
                etTags.setText(viewModel.getTagNamesForMovie(movieId).joinToString(", "))

                val watched = movie.status == "WATCHED"
                if (watched) rbWatched.isChecked = true else rbWatching.isChecked = true
                updateStatusUi(watched)

                currentRating = (movie.rating ?: 5f).toInt().coerceIn(1, 10)
                refreshStars()

                pickedDateMillis = movie.watchedDate
                movie.watchedDate?.let {
                    etDate.setText(DateFormat.format("dd.MM.yyyy", it).toString())
                }

                chipGroup.findViewWithTag<Chip>(movie.genreId)?.let { selectGenreChip(it, movie.genreId) }
            }
        }
    }
}