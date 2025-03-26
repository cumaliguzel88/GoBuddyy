package com.cumaliguzel.free.domain.usecase

import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import com.google.maps.android.PolyUtil

class FetchRouteUseCase @Inject constructor() {
    suspend operator fun invoke(start: LatLng, end: LatLng): List<LatLng> {
        val apiKey = "AIzaSyCxC3gxRm4aRKISlwgp0aUXWOIgktXFizY"
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&mode=driving&key=$apiKey"

        return try {
            withContext(Dispatchers.IO) {
                val result = URL(url).readText()
                val jsonObject = JSONObject(result)
                val routes = jsonObject.optJSONArray("routes")
                if (routes == null || routes.length() == 0) {
                    emptyList()
                } else {
                    val points = routes
                        .getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")

                    PolyUtil.decode(points)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
