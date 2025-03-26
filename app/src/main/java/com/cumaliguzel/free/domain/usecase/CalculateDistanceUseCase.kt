package com.cumaliguzel.free.domain.usecase

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject

class CalculateDistanceUseCase @Inject constructor() {
    suspend operator fun invoke(start: LatLng, end: LatLng): Pair<Double, Int> {
        val apiKey = "AIzaSyCxC3gxRm4aRKISlwgp0aUXWOIgktXFizY"  // BuildConfig üzerinden çağırılması önerilir.
        val url = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                "origins=${start.latitude},${start.longitude}" +
                "&destinations=${end.latitude},${end.longitude}" +
                "&departure_time=now" +  // Trafik durumuna göre tahmini süre
                "&key=$apiKey"

        Log.d("TrafficUseCase", "🚀 API çağrısı yapılıyor: $url")

        return try {
            withContext(Dispatchers.IO) {
                val response = URL(url).readText()
                val jsonObject = JSONObject(response)

                val rows = jsonObject.getJSONArray("rows")
                if (rows.length() > 0) {
                    val elements = rows.getJSONObject(0).getJSONArray("elements")
                    if (elements.length() > 0) {
                        val distanceValue = elements.getJSONObject(0)
                            .getJSONObject("distance").getDouble("value") / 1000.0  // Metreden KM'ye çevir
                        val durationValue = elements.getJSONObject(0)
                            .getJSONObject("duration_in_traffic").getInt("value") / 60  // Saniyeden dakikaya çevir

                        Log.d("TrafficUseCase", "✅ API yanıtı başarılı - Mesafe: ${"%.2f".format(distanceValue)} km, Süre: $durationValue dk")

                        return@withContext Pair(distanceValue, durationValue)
                    }
                }

                Log.e("TrafficUseCase", "❌ API yanıtı boş döndü!")
                return@withContext Pair(0.0, 0)
            }
        } catch (e: Exception) {
            Log.e("TrafficUseCase", "⚠️ API çağrısında hata oluştu: ${e.message}", e)
            return Pair(0.0, 0)
        }
    }
}