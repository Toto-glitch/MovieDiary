package com.example.moviediary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moviediary.fragments.MovieListFragment
import com.example.moviediary.fragments.SettingsFragment
import com.example.moviediary.fragments.StatisticsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MovieListFragment())
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.navList -> MovieListFragment()
                R.id.navStats -> StatisticsFragment()
                R.id.navSettings -> SettingsFragment()
                else -> MovieListFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }
    }
}