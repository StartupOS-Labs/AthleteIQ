// LocationApiService.kt
package com.athleteiqapp

import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApiService {
    @GET("autocomplete")
    suspend fun getSuggestions(@Query("query") query: String): List<String>
}