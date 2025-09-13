// LocationRepository.kt
package com.athleteiqapp

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

// Data class to parse the API response
data class NominatimResponse(val display_name: String)

object LocationRepository {

    suspend fun getSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        // This is a free, open-source API for location data
        val urlString = "https://nominatim.openstreetmap.org/search?q=${query.replace(" ", "+")}&format=json&countrycodes=in&limit=10"

        try {
            val jsonString = URL(urlString).readText()
            val listType = object : TypeToken<List<NominatimResponse>>() {}.type
            val suggestions: List<NominatimResponse> = Gson().fromJson(jsonString, listType)
            return@withContext suggestions.map { it.display_name }
        } catch (e: Exception) {
            // Log the error to see what went wrong
            e.printStackTrace()
            return@withContext emptyList<String>()
        }
    }
}