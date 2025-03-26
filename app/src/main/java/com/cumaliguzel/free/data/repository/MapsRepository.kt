package com.cumaliguzel.free.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapsRepository @Inject constructor(
    @ApplicationContext private val context: Context // Hilt'ten Context alıyoruz!
) {
    // 📌 Google Places API Konum Önerileri
    suspend fun getAutocompleteSuggestions(query: String): List<AutocompletePrediction> {
        if (query.isEmpty()) return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val placesClient = Places.createClient(context)
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setCountry("TR") // Ülke kısıtlaması
                    .build()
                val response = Tasks.await(placesClient.findAutocompletePredictions(request))
                response?.autocompletePredictions ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    // 📌 Seçilen Konumun Koordinatlarını Al
    suspend fun getPlaceLatLng(placeId: String): LatLng? {
        return withContext(Dispatchers.IO) {
            try {
                val placesClient = Places.createClient(context)
                val request = FetchPlaceRequest.newInstance(placeId, listOf(Place.Field.LAT_LNG))
                val response = Tasks.await(placesClient.fetchPlace(request))
                response?.place?.latLng
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // 📌 Kullanıcının En Son Bilinen Konumunu Al
    fun getLastKnownLocation(
        fusedLocationClient: FusedLocationProviderClient,
        onLocationRetrieved: (LatLng) -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        onLocationRetrieved(LatLng(it.latitude, it.longitude))
                    }
                }
                .addOnFailureListener { e -> e.printStackTrace() }
        }
    }
}
