package com.example.moviediary.network

import retrofit2.http.GET
import retrofit2.http.Query

interface KinopoiskApi {

    @GET("api/v2.1/films/search-by-keyword")
    suspend fun searchByKeyword(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1
    ): SearchResponse
}