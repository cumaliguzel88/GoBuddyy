package com.cumaliguzel.free.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline


@Composable
fun MapViewComponent(
    cameraPositionState: CameraPositionState,
    userLocation: LatLng?,
    selectedLocation: LatLng?,
    polylinePoints: List<LatLng>,
    onMapClick: (LatLng) -> Unit
) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true),
        uiSettings = MapUiSettings(myLocationButtonEnabled = true),
        onMapClick = { latLng -> onMapClick(latLng) }
    ) {
        userLocation?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Mevcut Konum",
                snippet = "Buradasınız"
            )
        }

        selectedLocation?.let { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Seçilen Nokta",
                snippet = "Burayı seçtiniz"
            )
        }

        if (polylinePoints.isNotEmpty()) {
            Polyline(
                points = polylinePoints,
                color = Color.Red,
                width = 8f
            )
        }
    }
}
