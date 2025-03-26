package com.cumaliguzel.free.domain.usecase

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale

class ReverseGeocodingUseCase(private val context: Context) {

    suspend operator fun invoke(location: LatLng): String {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = "AIzaSyCxC3gxRm4aRKISlwgp0aUXWOIgktXFizY" // BuildConfig üzerinden çağır
                val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=${location.latitude},${location.longitude}&key=$apiKey"
                val response = URL(url).readText()
                val jsonObject = JSONObject(response)
                val results = jsonObject.getJSONArray("results")

                if (results.length() > 0) {
                    val addressComponents = results.getJSONObject(0).getJSONArray("address_components")
                    var neighborhood = ""
                    var sublocality = ""
                    var route = ""
                    var district = ""
                    var city = ""

                    for (i in 0 until addressComponents.length()) {
                        val component = addressComponents.getJSONObject(i)
                        val types = component.getJSONArray("types")

                        when {
                            types.toString().contains("neighborhood") -> neighborhood = component.getString("long_name")
                            types.toString().contains("sublocality_level_1") -> sublocality = component.getString("long_name")
                            types.toString().contains("route") -> route = component.getString("long_name")
                            types.toString().contains("administrative_area_level_2") -> district = component.getString("long_name") // İlçe
                            types.toString().contains("locality") -> city = component.getString("long_name") // Şehir
                        }
                    }

                    // 📌 **Daha geniş format: Mahalle / İlçe / Şehir**
                    val finalArea = when {
                        sublocality.isNotEmpty() -> "$sublocality, $district"
                        neighborhood.isNotEmpty() -> "$neighborhood, $district"
                        route.isNotEmpty() -> "$route, $district"
                        district.isNotEmpty() -> "$district, $city"
                        city.isNotEmpty() -> city
                        else -> "Adres bulunamadı"
                    }

                    return@withContext finalArea // 📌 En iyi formatı döndür
                } else {
                    return@withContext "Adres bulunamadı"
                }

            } catch (e: Exception) {
                e.printStackTrace()

                // 📌 **Alternatif: Android Geocoder Kullan**
                return@withContext try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val address = addresses?.get(0)

                    val geocoderNeighborhood = address?.subLocality ?: address?.thoroughfare ?: ""
                    val geocoderDistrict = address?.subAdminArea ?: ""
                    val geocoderCity = address?.locality ?: ""

                    // 📌 Mahalle + İlçe + Şehir formatı
                    listOf(geocoderNeighborhood, geocoderDistrict, geocoderCity)
                        .filter { it.isNotEmpty() }
                        .joinToString(", ")

                } catch (e: Exception) {
                    "Adres bulunamadı"
                }
            }
        }
    }
}


