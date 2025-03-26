package com.cumaliguzel.free.domain.usecase

import com.cumaliguzel.free.data.repository.MapsRepository
import com.google.android.libraries.places.api.model.AutocompletePrediction
import javax.inject.Inject

class GetAutocompleteSuggestionsUseCase @Inject constructor(
    private val mapsRepository: MapsRepository
) {
    suspend operator fun invoke(query: String): List<AutocompletePrediction> {
        return mapsRepository.getAutocompleteSuggestions(query)
    }
}
