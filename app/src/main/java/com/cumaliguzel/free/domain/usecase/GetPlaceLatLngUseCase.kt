package com.cumaliguzel.free.domain.usecase

import com.cumaliguzel.free.data.repository.MapsRepository
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class GetPlaceLatLngUseCase @Inject constructor(
    private val mapsRepository: MapsRepository
) {
    suspend operator fun invoke(placeId: String): LatLng? {
        return mapsRepository.getPlaceLatLng(placeId)
    }
}
