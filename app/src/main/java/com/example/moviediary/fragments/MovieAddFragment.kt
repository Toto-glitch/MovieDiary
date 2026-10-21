package com.example.moviediary.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.moviediary.R
import com.example.moviediary.models.Movie
import com.example.moviediary.network.FilmItem
import com.example.moviediary.viewmodels.MovieViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import java.util.Calendar

class AddMovieFragment : Fragment(R.layout.fragment_add_movie) {

    private val viewModel: MovieViewModel by activityViewModels()

    // Состояние формы
    private var selectedGenreId: Long? = null
    private var selectedGenreChip: Chip? = null
    private var selectedPosterUrl: String? = null
    private var pickedDateMillis: Long? = null
    private var currentRating = 5
    private var isWatched = true

    // Режим редактирования
    private var editingMovieId: Long? = null
    private var editingMovie: Movie? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etYear = view.findViewById<EditText>(R.id.etYear)
        val etDuration = view.findViewById<EditText>(R.id.etDuration)
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
        val btnSearchPoster = view.findViewById<TextView>(R.id.btnSearchPoster)
        val progressSearch = view.findViewById<ProgressBar>(R.id.progressSearch)
        val posterImage = view.findViewById<ImageView>(R.id.posterImage)
        val posterPlaceholder = view.findViewById<View>(R.id.posterPlaceholder)

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
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorOnAccent))
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
                        tag = genre.id // метка, чтобы потом найти чип по id жанра
                    }
                    chip.setOnClickListener { selectGenreChip(chip, genre.id) }
                    chipGroup.addView(chip)
                }
            }
            editingMovie?.let { m ->
                chipGroup.findViewWithTag<Chip>(m.genreId)?.let { selectGenreChip(it, m.genreId) }
            }
        }

        fun showPoster(url: String?) {
            selectedPosterUrl = url
            if (url.isNullOrBlank()) {
                posterImage.visibility = View.GONE
                posterPlaceholder.visibility = View.VISIBLE
            } else {
                posterPlaceholder.visibility = View.GONE
                posterImage.visibility = View.VISIBLE
                posterImage.load(url) { crossfade(true) }
            }
        }

        fun selectGenreByName(name: String): Boolean {
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as? Chip ?: continue
                if (chip.text.toString().equals(name, ignoreCase = true)) {
                    selectGenreChip(chip, chip.tag as Long)
                    return true
                }
            }
            return false
        }

        fun parseFilmLength(value: String?): Int? {
            if (value.isNullOrBlank()) return null
            val parts = value.split(":")
            if (parts.size != 2) return value.toIntOrNull()
            val hours = parts[0].toIntOrNull() ?: return null
            val minutes = parts[1].toIntOrNull() ?: return null
            return hours * 60 + minutes
        }

        fun applyFilm(film: FilmItem) {
            (film.nameRu ?: film.nameEn)?.let { etTitle.setText(it) }
            film.year?.toIntOrNull()?.let { etYear.setText(it.toString()) }
            parseFilmLength(film.filmLength)?.let { etDuration.setText(it.toString()) }
            showPoster(film.posterUrl)
            film.genres?.firstOrNull { g -> g.genre != null && selectGenreByName(g.genre) }
        }

        fun showResultsDialog(films: List<FilmItem>) {
            val shown = films.take(10)
            val titles = shown.map {
                "${it.nameRu ?: it.nameEn ?: "Без названия"} (${it.year ?: "?"})"
            }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle("Выберите фильм")
                .setItems(titles) { _, which -> applyFilm(shown[which]) }
                .setNegativeButton("Отмена", null)
                .show()
        }

        btnSearchPoster.setOnClickListener {
            val query = etTitle.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(requireContext(), "Сначала введите название фильма", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressSearch.visibility = View.VISIBLE
            btnSearchPoster.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                val result = viewModel.searchFilms(query)
                progressSearch.visibility = View.GONE
                btnSearchPoster.isEnabled = true
                result
                    .onSuccess { films ->
                        if (films.isEmpty())
                            Toast.makeText(requireContext(), "Ничего не найдено", Toast.LENGTH_SHORT).show()
                        else showResultsDialog(films)
                    }
                    .onFailure { error ->
                        Toast.makeText(requireContext(), error.message ?: "Ошибка поиска", Toast.LENGTH_LONG).show()
                    }
            }
        }

        // ---------- Отмена ----------
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
                posterUrl = selectedPosterUrl,
                durationMinutes = etDuration.text.toString().trim().toIntOrNull()?.takeIf { it > 0 },
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
                etDuration.setText(movie.durationMinutes?.toString() ?: "")
                etReview.setText(movie.review ?: "")
                etTags.setText(viewModel.getTagNamesForMovie(movieId).joinToString(", "))
                showPoster(movie.posterUrl)

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