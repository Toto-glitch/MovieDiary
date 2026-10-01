package com.example.moviediary.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.moviediary.R
import com.example.moviediary.models.Movie
import com.example.moviediary.viewmodels.MovieViewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: MovieViewModel by activityViewModels()
    private var currentMovies: List<Movie> = emptyList()
    private val sortOptions = listOf("Год (новые)", "Год (старые)", "Рейтинг", "Название А-Я")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.allMovies.observe(viewLifecycleOwner) { movies ->
            currentMovies = movies
        }

        val tvSortValue = view.findViewById<TextView>(R.id.tvSortValue)
        view.findViewById<LinearLayout>(R.id.rowSort).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Сортировка по умолчанию")
                .setItems(sortOptions.toTypedArray()) { _, which ->
                    tvSortValue.text = sortOptions[which]
                }
                .show()
        }

        view.findViewById<LinearLayout>(R.id.rowExport).setOnClickListener {
            val json = buildString {
                append("[")
                currentMovies.forEachIndexed { index, m ->
                    append("""{"title":"${m.title}","year":${m.year},"status":"${m.status}","rating":${m.rating ?: "null"}}""")
                    if (index < currentMovies.size - 1) append(",")
                }
                append("]")
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, json)
            }
            startActivity(Intent.createChooser(shareIntent, "Экспорт данных MovieDiary"))
        }

        view.findViewById<LinearLayout>(R.id.rowClear).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Очистить все данные?")
                .setMessage("Это действие нельзя отменить. Все фильмы будут удалены без возможности восстановления.")
                .setPositiveButton("Удалить всё") { _, _ ->
                    viewModel.deleteAllMovies()
                    Toast.makeText(requireContext(), "Данные очищены", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }
}