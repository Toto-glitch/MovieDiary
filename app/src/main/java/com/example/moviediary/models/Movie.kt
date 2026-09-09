package com.example.moviediary.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movies",
    foreignKeys = [
        ForeignKey(
            entity = Genre::class,
            parentColumns = ["id"],
            childColumns = ["genreId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("genreId")]
)
data class Movie(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val year: Int,
    val posterUrl: String? = null,
    val rating: Float? = null,
    val status: String,
    val watchedDate: Long? = null,
    val durationMinutes: Int? = null,
    val review: String? = null,
    val genreId: Long
)