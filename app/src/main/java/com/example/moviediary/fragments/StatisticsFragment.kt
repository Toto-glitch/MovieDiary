package com.example.moviediary.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.moviediary.R
import com.example.moviediary.models.Movie
import com.example.moviediary.viewmodels.MovieViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel: MovieViewModel by activityViewModels()

    private var movies: List<Movie> = emptyList()
    private var genreNames: Map<Long, String> = emptyMap()

    private lateinit var tvTotal: TextView
    private lateinit var tvAvg: TextView
    private lateinit var tvHours: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var tvBarTitle: TextView
    private lateinit var chartsContent: LinearLayout
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

    private val palette = listOf(
        "#E8A33D", "#5B9BD5", "#7EC8A0", "#C97BB2", "#E87B5B", "#8DA3E8", "#8B8B96"
    ).map { Color.parseColor(it) }

    private val monthLabels = listOf(
        "Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTotal = view.findViewById(R.id.tvTotal)
        tvAvg = view.findViewById(R.id.tvAvg)
        tvHours = view.findViewById(R.id.tvHours)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        tvBarTitle = view.findViewById(R.id.tvBarTitle)
        chartsContent = view.findViewById(R.id.chartsContent)
        pieChart = view.findViewById(R.id.pieChart)
        barChart = view.findViewById(R.id.barChart)

        viewModel.allMovies.observe(viewLifecycleOwner) {
            movies = it
            render()
        }
        viewModel.allGenres.observe(viewLifecycleOwner) { genres ->
            genreNames = genres.associate { it.id to it.name }
            render()
        }
    }

    private fun render() {
        val watched = movies.filter { it.status == "WATCHED" }

        tvTotal.text = movies.size.toString()

        val ratings = watched.mapNotNull { it.rating }.filter { it > 0f }
        tvAvg.text = if (ratings.isEmpty()) "—"
        else String.format(Locale.US, "%.1f", ratings.average())

        val totalMinutes = watched.sumOf { it.durationMinutes ?: 0 }
        tvHours.text = (totalMinutes / 60f).roundToInt().toString()

        if (watched.isEmpty()) {
            chartsContent.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            return
        }
        chartsContent.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        showGenrePie(watched)
        showMonthlyBars(watched)
    }

    private fun showGenrePie(watched: List<Movie>) {
        val counts = watched.groupingBy { it.genreId }.eachCount().entries
            .sortedByDescending { it.value }


        val entries = counts.take(6).map {
            PieEntry(it.value.toFloat(), "${genreNames[it.key] ?: "—"} · ${it.value}")
        }.toMutableList()
        val rest = counts.drop(6).sumOf { it.value }
        if (rest > 0) entries.add(PieEntry(rest.toFloat(), "Другое · $rest"))

        val dataSet = PieDataSet(entries, "").apply {
            colors = palette
            sliceSpace = 2f
            setDrawValues(false)
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 58f
            transparentCircleRadius = 0f
            setHoleColor(Color.TRANSPARENT)
            setDrawEntryLabels(false)
            setTouchEnabled(false)

            legend.apply {
                isEnabled = true
                orientation = Legend.LegendOrientation.VERTICAL
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                setDrawInside(false)
                form = Legend.LegendForm.CIRCLE
                textSize = 12f
                textColor = ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)
                isWordWrapEnabled = true
            }
            animateY(600)
            invalidate()
        }
    }

    private fun showMonthlyBars(watched: List<Movie>) {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val perMonth = IntArray(12)
        watched.forEach { movie ->
            movie.watchedDate?.let { date ->
                val cal = Calendar.getInstance().apply { timeInMillis = date }
                if (cal.get(Calendar.YEAR) == year) perMonth[cal.get(Calendar.MONTH)]++
            }
        }
        tvBarTitle.text = "ПО МЕСЯЦАМ · $year"

        val muted = ContextCompat.getColor(requireContext(), R.color.colorTextMuted)
        val border = ContextCompat.getColor(requireContext(), R.color.colorBorder)
        val intFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = value.toInt().toString()
        }

        val entries = perMonth.mapIndexed { i, count -> BarEntry(i.toFloat(), count.toFloat()) }
        val dataSet = BarDataSet(entries, "").apply {
            color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)
            valueTextSize = 10f
            // над пустыми месяцами число 0 не показываем
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String =
                    if (value > 0f) value.toInt().toString() else ""
            }
        }

        barChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.6f }
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(monthLabels)
                granularity = 1f
                setLabelCount(12, false)
                textColor = muted
                textSize = 9f
                setDrawGridLines(false)
                axisLineColor = border
            }
            axisLeft.apply {
                axisMinimum = 0f
                granularity = 1f
                valueFormatter = intFormatter
                textColor = muted
                gridColor = border
                setDrawAxisLine(false)
            }
            animateY(600)
            invalidate()
        }
    }
}