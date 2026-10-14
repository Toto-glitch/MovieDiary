package com.example.moviediary.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.moviediary.R
import com.example.moviediary.models.Movie
import com.example.moviediary.utils.SettingsManager
import com.example.moviediary.viewmodels.MovieViewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: MovieViewModel by activityViewModels()
    private var currentMovies: List<Movie> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        viewModel.allMovies.observe(viewLifecycleOwner) { currentMovies = it }

        val switchTheme = view.findViewById<CompoundButton>(R.id.switchTheme)
        switchTheme.isChecked = SettingsManager.isDarkTheme(ctx)
        switchTheme.setOnCheckedChangeListener { _, checked ->
            SettingsManager.setDarkTheme(ctx, checked)
            SettingsManager.applyTheme(ctx)
        }

        val tvSortValue = view.findViewById<TextView>(R.id.tvSortValue)
        fun showSortValue() {
            val key = SettingsManager.getDefaultSort(ctx)
            tvSortValue.text = SettingsManager.sortOptions.firstOrNull { it.first == key }?.second ?: "По году"
        }
        showSortValue()
        view.findViewById<LinearLayout>(R.id.rowSort).setOnClickListener {
            val options = SettingsManager.sortOptions
            AlertDialog.Builder(ctx)
                .setTitle("Сортировка по умолчанию")
                .setItems(options.map { it.second }.toTypedArray()) { _, which ->
                    SettingsManager.setDefaultSort(ctx, options[which].first)
                    showSortValue()
                }
                .show()
        }

        val switchDuration = view.findViewById<CompoundButton>(R.id.switchDuration)
        val tvUnitMin = view.findViewById<TextView>(R.id.tvUnitMin)
        val tvUnitHours = view.findViewById<TextView>(R.id.tvUnitHours)
        fun refreshUnitLabels(hours: Boolean) {
            val active = ContextCompat.getColor(ctx, R.color.colorTextPrimary)
            val muted = ContextCompat.getColor(ctx, R.color.colorTextMuted)
            tvUnitMin.setTextColor(if (hours) muted else active)
            tvUnitHours.setTextColor(if (hours) active else muted)
        }
        val inHours = SettingsManager.isDurationInHours(ctx)
        switchDuration.isChecked = inHours
        refreshUnitLabels(inHours)
        switchDuration.setOnCheckedChangeListener { _, checked ->
            SettingsManager.setDurationInHours(ctx, checked)
            refreshUnitLabels(checked)
        }

        view.findViewById<LinearLayout>(R.id.rowExport).setOnClickListener {
            val json = buildString {
                append("[")
                currentMovies.forEachIndexed { index, m ->
                    val title = m.title.replace("\\", "\\\\").replace("\"", "\\\"")
                    append("""{"title":"$title","year":${m.year},"status":"${m.status}","rating":${m.rating ?: "null"}}""")
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
            AlertDialog.Builder(ctx)
                .setTitle("Очистить все данные?")
                .setMessage("Это действие нельзя отменить. Все фильмы будут удалены без возможности восстановления.")
                .setPositiveButton("Удалить всё") { _, _ ->
                    viewModel.deleteAllMovies()
                    Toast.makeText(ctx, "Данные очищены", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }
}