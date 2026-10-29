package com.example.moviediary

import android.graphics.Rect
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.moviediary.fragments.MovieListFragment
import com.example.moviediary.fragments.SettingsFragment
import com.example.moviediary.fragments.StatisticsFragment
import com.example.moviediary.utils.SettingsManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var gestureDetector: GestureDetector
    private val tabs = listOf(R.id.navList, R.id.navStats, R.id.navSettings)
    private var swipeAllowed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SettingsManager.applyTheme(this)

        val container = findViewById<View>(R.id.fragmentContainer)
        ViewCompat.setOnApplyWindowInsetsListener(container) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }

        bottomNav = findViewById(R.id.bottomNav)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MovieListFragment())
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.navList -> MovieListFragment()
                R.id.navStats -> StatisticsFragment()
                R.id.navSettings -> SettingsFragment()
                else -> MovieListFragment()
            }
            supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }

        val config = ViewConfiguration.get(this)
        val minDistance = 100 * resources.displayMetrics.density
        val minVelocity = config.scaledMinimumFlingVelocity * 8

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null || !swipeAllowed) return false
                if (supportFragmentManager.backStackEntryCount > 0) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                if (abs(dx) < minDistance || abs(dx) < abs(dy) * 2 || abs(velocityX) < minVelocity) return false

                val current = tabs.indexOf(bottomNav.selectedItemId)
                val target = if (dx < 0) current + 1 else current - 1
                if (current == -1 || target !in tabs.indices) return false
                bottomNav.selectedItemId = tabs[target]
                return true
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            swipeAllowed = !startsInsideHorizontalScroller(ev)
        }
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun startsInsideHorizontalScroller(ev: MotionEvent): Boolean {
        val scroller = findViewById<View>(R.id.chipScroll) ?: return false
        val rect = Rect()
        if (!scroller.getGlobalVisibleRect(rect)) return false
        return rect.contains(ev.rawX.toInt(), ev.rawY.toInt())
    }
}
