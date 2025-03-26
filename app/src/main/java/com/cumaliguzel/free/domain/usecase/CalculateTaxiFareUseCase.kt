package com.cumaliguzel.free.domain.usecase

import android.util.Log
import javax.inject.Inject

class CalculateTaxiFareUseCase @Inject constructor() {
    operator fun invoke(distanceKm: Double, durationMin: Double): Map<String, Double> {
        val trafficRatePerMin = 1.0 // Dakika başına ücret (Trafikte bekleme ücreti)

        // 📌 **Taksi Türlerine Göre Açılış Ücretleri ve Kilometre Başına Ücretler**
        val taxiRates = mapOf(
            "Sarı Taksi" to Pair(42.0, 28.0),      // Açılış: 42 TL, Km başına: 28 TL
            "Turkuaz Taksi" to Pair(48.30, 32.20), // Açılış: 48.30 TL, Km başına: 32.20 TL
            "Siyah Taksi" to Pair(71.40, 47.60),   // Açılış: 71.40 TL, Km başına: 47.60 TL
        )

        val fareResults = mutableMapOf<String, Double>()

        // 📌 **Tüm taksi türleri için hesaplama yap**
        for ((taxiType, rates) in taxiRates) {
            val baseFare = rates.first
            val perKmRate = rates.second

            val totalFare = baseFare + (distanceKm * perKmRate) + (durationMin * trafficRatePerMin)
            fareResults[taxiType] = totalFare
        }

        // 📌 **Tag Taksi için özel hesaplama (%10 daha ucuz)**
        val standardTaxiFare = fareResults["Sarı Taksi"] ?: 0.0
        fareResults["Tag"] = standardTaxiFare * 0.90

        // 📌 **Log kayıtları ile süreci takip et**
        Log.d("CalculateTaxiFareUseCase", "📊 Hesaplanan Fiyatlar:")
        fareResults.forEach { (taxiType, fare) ->
            Log.d("CalculateTaxiFareUseCase", "🚕 $taxiType: ${"%.2f".format(fare)} TL")
        }

        return fareResults
    }
}