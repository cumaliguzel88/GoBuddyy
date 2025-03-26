package com.cumaliguzel.free.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cumaliguzel.free.domain.usecase.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getAutocompleteSuggestionsUseCase: GetAutocompleteSuggestionsUseCase,
    private val getPlaceLatLngUseCase: GetPlaceLatLngUseCase,
    private val getLastKnownLocationUseCase: GetLastKnownLocationUseCase,
    private val calculateDistanceUseCase: CalculateDistanceUseCase,
    private val fetchRouteUseCase: FetchRouteUseCase,
    private val calculateTaxiFareUseCase: CalculateTaxiFareUseCase,
    private val reverseGeocodingUseCase: ReverseGeocodingUseCase, // 📌 Yeni eklenen UseCase
    @ApplicationContext private val context: Context
) : ViewModel() {

    // 📌 **Kullanıcının mevcut konumunu saklayan değişken**
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    // 📌 **Kullanıcının seçtiği konumu saklayan değişken**
    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation

    // 📌 **Mevcut konumun mahalle/ilçe bilgisini saklayan değişken**
    private val _fromLocationText = MutableStateFlow("Konum alınıyor...")
    val fromLocationText: StateFlow<String> = _fromLocationText

    // 📌 **Seçilen konumun mahalle/ilçe bilgisini saklayan değişken**
    private val _toLocationText = MutableStateFlow("Konum alınıyor...")
    val toLocationText: StateFlow<String> = _toLocationText

    // 📌 **Harita üzerindeki çizgi için rota noktalarını saklayan değişken**
    private val _route = MutableStateFlow<List<LatLng>>(emptyList())
    val route: StateFlow<List<LatLng>> = _route

    private val _distance = MutableStateFlow(0.0)  // 🔥 İlk başta 0.0 olarak başlasın
    val distance: StateFlow<Double> = _distance

    // 📌 **Google Places API'den gelen konum önerilerini saklayan değişken**
    private val _autocompleteSuggestions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val autocompleteSuggestions: StateFlow<List<AutocompletePrediction>> = _autocompleteSuggestions

    // 📌 **Tahmini ulaşım ücretleri**
    private val _taxiFares = MutableStateFlow<Map<String, Double>>(emptyMap())
    val taxiFares: StateFlow<Map<String, Double>> = _taxiFares

    // 📌 **Seçilen ulaşım türleri**
    private val _selectedTransportOptions = MutableStateFlow<List<String>>(emptyList())
    val selectedTransportOptions: StateFlow<List<String>> = _selectedTransportOptions


    private val _duration = MutableStateFlow("")
    val duration: StateFlow<String> = _duration  // 📌 Trafik süresi eklend



    // 📌 **Kullanıcının mevcut konumunu al ve mahalle/ilçe bilgisine çevir**
    fun getLastKnownLocation(fusedLocationClient: FusedLocationProviderClient) {
        viewModelScope.launch {
            getLastKnownLocationUseCase.execute(fusedLocationClient) { location ->
                _userLocation.value = location

                // 📌 Konumu mahalle/ilçe formatına çevir ve kaydet
                viewModelScope.launch {
                    _fromLocationText.value = reverseGeocodingUseCase(location)
                }
            }
        }
    }

    // 📌 **Google Places API'den konum önerileri al**
    fun getAutocompleteSuggestions(query: String) {
        viewModelScope.launch {
            val suggestions = getAutocompleteSuggestionsUseCase(query)
            _autocompleteSuggestions.value = suggestions
        }
    }

    // 📌 **Seçilen konumun koordinatlarını al ve mahalle/ilçe formatına çevir**
    fun selectPlace(placeId: String, onPlaceSelected: (LatLng?) -> Unit) {
        viewModelScope.launch {
            val latLng = getPlaceLatLngUseCase(placeId)
            _selectedLocation.value = latLng
            onPlaceSelected(latLng)

            latLng?.let {
                viewModelScope.launch {
                    _toLocationText.value = reverseGeocodingUseCase(it)
                }
            }
        }
    }

    // 📌 **Kullanıcının seçtiği konumu güncelle**
    fun updateSelectedLocation(location: LatLng) {
        _selectedLocation.value = location

        viewModelScope.launch {
            _toLocationText.value = reverseGeocodingUseCase(location)
        }
    }



    // 📌 **Kullanıcının mevcut konumu ile seçtiği konum arasındaki rotayı getir**
    fun fetchRoute() {
        viewModelScope.launch {
            val start = _userLocation.value
            val end = _selectedLocation.value

            if (start != null && end != null) {
                val routePoints = fetchRouteUseCase(start, end)
                _route.value = routePoints
            }
        }
    }

    fun calculateDistanceAndFares() {
        viewModelScope.launch {
            val start = _userLocation.value
            val end = _selectedLocation.value

            if (start != null && end != null) {
                Log.d("MapViewModel", "📍 Başlangıç: $start, Bitiş: $end")

                // ✅ Mesafe ve süreyi al
                val (distanceKm, durationMin) = calculateDistanceUseCase(start, end)

                // ✅ Mesafeyi güncelle (Double olarak saklıyoruz)
                _distance.value = distanceKm

                // ✅ Süreyi güncelle (String formatına çevirdik)
                _duration.value = "Tahmini Süre: %d dk".format(durationMin)

                Log.d("MapViewModel", "📊 Hesaplanan Mesafe: %.2f km, Süre: %d dk".format(distanceKm, durationMin))

                // ✅ Taksi ücretlerini hesapla
                val fares = calculateTaxiFareUseCase(distanceKm, durationMin.toDouble())
                _taxiFares.value = fares

                Log.d("MapViewModel", "✅ Taksi ücretleri güncellendi: $fares")
            } else {
                Log.e("MapViewModel", "❌ Başlangıç veya bitiş konumu NULL!")
            }
        }
    }
    // 📌 **Seçilen ulaşım türünü güncelle**
    fun updateSelectedTransportOptions(option: String, isSelected: Boolean) {
        _selectedTransportOptions.value = if (isSelected) {
            _selectedTransportOptions.value + option
        } else {
            _selectedTransportOptions.value - option
        }
    }

    // 📌 **Ulaşım seçimlerini sıfırla**
    fun clearTransportSelections() {
        _selectedTransportOptions.value = emptyList()
    }
}