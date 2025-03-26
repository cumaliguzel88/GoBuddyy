package com.cumaliguzel.free.domain.usecase

import com.cumaliguzel.free.data.repository.MapsRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class GetLastKnownLocationUseCase @Inject constructor(
    private val mapsRepository: MapsRepository
) {
    fun execute(fusedLocationClient: FusedLocationProviderClient, onLocationRetrieved: (LatLng) -> Unit) {
        mapsRepository.getLastKnownLocation(fusedLocationClient, onLocationRetrieved)
    }
}
