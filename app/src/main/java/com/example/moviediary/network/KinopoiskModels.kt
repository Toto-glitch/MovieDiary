package com.example.moviediary.network

data class SearchResponse(
    val films: List<FilmItem>? = null
)

data class FilmItem(
    val filmId: Int? = null,
    val nameRu: String? = null,
    val nameEn: String? = null,
    val year: String? = null,
    val filmLength: String? = null,
    val posterUrl: String? = null,
    val genres: List<GenreItem>? = null
)

data class GenreItem(
    val genre: String? = null
)