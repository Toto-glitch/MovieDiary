package com.example.moviediary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moviediary.fragments.MovieListFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MovieListFragment())
                .commit()
        }
    }
}