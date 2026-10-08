package com.example.moviediary.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviediary.R
import com.example.moviediary.adapters.MovieAdapter
import com.example.moviediary.models.Movie
import com.example.moviediary.viewmodels.MovieViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MovieListFragment : Fragment(R.layout.fragment_movie_list) {

    private enum class SortMode(val label: String) {
        YEAR("По году"), TITLE("А–Я"), RATING("По оценке")
    }

    private val viewModel: MovieViewModel by activityViewModels()
    private lateinit var adapter: MovieAdapter
    private lateinit var chipRow: LinearLayout
    private lateinit var tvEmpty: TextView

    private var allMovies: List<Movie> = emptyList()
    private var genreNames: Map<Long, String> = emptyMap()
    private var query = ""
    private var statusFilter: String? = null
    private var genreFilter: Long? = null
    private var sortMode = SortMode.YEAR

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chipRow = view.findViewById(R.id.chipRow)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        adapter = MovieAdapter { movie ->
            val fragment = MovieDetailFragment()
            fragment.arguments = Bundle().apply { putLong("movieId", movie.id) }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        val rv = view.findViewById<RecyclerView>(R.id.rvMovies)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        view.findViewById<EditText>(R.id.etSearch).doOnTextChanged { text, _, _, _ ->
            query = text?.toString()?.trim() ?: ""
            refreshList()
        }

        view.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddMovieFragment())
                .addToBackStack(null)
                .commit()
        }

        viewModel.allGenres.observe(viewLifecycleOwner) { genres ->
            genreNames = genres.associate { it.id to it.name }
            refreshAll()
        }
        viewModel.allMovies.observe(viewLifecycleOwner) { movies ->
            allMovies = movies
            refreshAll()
        }
    }

    private fun refreshAll() {
        if (genreFilter != null && allMovies.none { it.genreId == genreFilter }) genreFilter = null
        renderChips()
        refreshList()
    }

    private fun refreshList() {
        val filtered = allMovies.filter { movie ->
            val matchesQuery = query.isEmpty() ||
                    movie.title.contains(query, ignoreCase = true) ||
                    (movie.review?.contains(query, ignoreCase = true) == true)
            val matchesStatus = statusFilter == null || movie.status == statusFilter
            val matchesGenre = genreFilter == null || movie.genreId == genreFilter
            matchesQuery && matchesStatus && matchesGenre
        }
        val sorted = when (sortMode) {
            SortMode.YEAR -> filtered.sortedByDescending { it.year }
            SortMode.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SortMode.RATING -> filtered.sortedByDescending { it.rating ?: 0f }
        }
        adapter.update(sorted, genreNames)

        if (sorted.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = if (allMovies.isEmpty())
                "Пока нет ни одного фильма\nНажмите «+», чтобы добавить первый"
            else
                "Ничего не найдено"
        } else {
            tvEmpty.visibility = View.GONE
        }
    }

    private fun renderChips() {
        chipRow.removeAllViews()
        addChip("Все", statusFilter == null && genreFilter == null) {
            statusFilter = null
            genreFilter = null
            refreshAll()
        }
        addChip("Смотрю", statusFilter == "PLANNED") {
            statusFilter = if (statusFilter == "PLANNED") null else "PLANNED"
            refreshAll()
        }
        addChip("Просмотрено", statusFilter == "WATCHED") {
            statusFilter = if (statusFilter == "WATCHED") null else "WATCHED"
            refreshAll()
        }
        allMovies.map { it.genreId }.distinct().forEach { id ->
            val name = genreNames[id] ?: return@forEach
            addChip(name, genreFilter == id) {
                genreFilter = if (genreFilter == id) null else id
                refreshAll()
            }
        }
        addChip("↕ ${sortMode.label}", false) {
            sortMode = SortMode.values()[(sortMode.ordinal + 1) % SortMode.values().size]
            refreshAll()
        }
    }

    private fun addChip(label: String, active: Boolean, onClick: () -> Unit) {
        val ctx = requireContext()
        val chip = Chip(ctx).apply {
            text = label
            isCheckable = false
            checkedIcon = null
            chipStrokeWidth = if (active) 0f else 1f
            setChipStrokeColorResource(R.color.colorBorder)
            setChipBackgroundColorResource(if (active) R.color.colorAccent else R.color.colorBackground)
            setTextColor(ContextCompat.getColor(ctx, if (active) R.color.colorBackground else R.color.colorTextMuted))
            typeface = if (active) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            setOnClickListener { onClick() }
        }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { marginEnd = (8 * resources.displayMetrics.density).toInt() }
        chipRow.addView(chip, params)
    }
}
