package com.cumaliguzel.free.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cumaliguzel.free.components.*
import com.cumaliguzel.free.data.model.Post
import com.cumaliguzel.free.viewmodel.MapViewModel
import com.cumaliguzel.free.viewmodel.PostViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsPage(viewModel: MapViewModel = hiltViewModel() , navController: NavController,postViewModel: PostViewModel = hiltViewModel()) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val userLocation by viewModel.userLocation.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val polylinePoints by viewModel.route.collectAsState()
    val distanceText by viewModel.distance.collectAsState()
    val taxiFares by viewModel.taxiFares.collectAsState()
    val selectedTransportOptions by viewModel.selectedTransportOptions.collectAsState()
    // 📌 Firebase Auth ile giriş yapan kullanıcıyı al
    val currentUser = Firebase.auth.currentUser

    var searchText by remember { mutableStateOf("") }
    val searchResults by viewModel.autocompleteSuggestions.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    val showTransportDialog = remember { mutableStateOf(false) }
    val tempLocation = remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation ?: LatLng(0.0, 0.0), 10f)
    }

    // 📌 **Konum izni isteyici**
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.getLastKnownLocation(LocationServices.getFusedLocationProviderClient(context))
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Uygulamayı kullanmak için konum izni vermelisiniz!")
            }
        }
    }

    // 📌 **Uygulama açıldığında konum izni kontrolü ve alma işlemi**
    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.getLastKnownLocation(LocationServices.getFusedLocationProviderClient(context))
        } else {
            locationPermissionLauncher.launch(permission)
        }
    }

    // 📌 Kullanıcı konumu güncellendiğinde kamerayı oraya götür
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            MapViewComponent(
                cameraPositionState = cameraPositionState,
                userLocation = userLocation,
                selectedLocation = selectedLocation,
                polylinePoints = polylinePoints,
                onMapClick = { latLng ->
                    tempLocation.value = latLng
                    showDialog.value = true
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize().padding(paddingValues)
                    .align(Alignment.TopCenter)
            ) {
                // 📌 **Konum Arama Çubuğu Üst Kısımda**
                SearchBarComponent(
                    searchText = searchText,
                    searchResults = searchResults,
                    onSearchTextChange = { query ->
                        searchText = query
                        viewModel.getAutocompleteSuggestions(query)
                    },
                    onResultClick = { prediction ->
                        coroutineScope.launch {
                            viewModel.selectPlace(prediction.placeId) { latLng ->
                                if (latLng != null) {
                                    tempLocation.value = latLng
                                    searchText = "" // 📌 Seçim yapıldığında arama metni temizleniyor

                                    viewModel.updateSelectedLocation(latLng)
                                    viewModel.calculateDistanceAndFares()
                                    viewModel.fetchRoute()
                                    cameraPositionState.position =
                                        CameraPosition.fromLatLngZoom(latLng, 15f) // 📌 Kamerayı konuma götür
                                    showTransportDialog.value = true // 📌 Ulaşım seçenekleri pop-up aç
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

            }
        }

        // 📌 **Konum Seçme Diyaloğu (Haritaya Dokunarak Seçim)**
        LocationDialogComponent(
            showDialog = showDialog.value,
            onDismiss = { showDialog.value = false },
            onConfirm = {
                tempLocation.value?.let { selectedLoc ->
                    viewModel.updateSelectedLocation(selectedLoc)
                    coroutineScope.launch {
                        viewModel.calculateDistanceAndFares()
                        viewModel.fetchRoute()
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(selectedLoc, 15f) // 📌 Seçilen konuma odaklan
                        showTransportDialog.value = true // 📌 Ulaşım seçenekleri pop-up aç
                    }
                    showDialog.value = false
                }
            }
        )

        // 📌 **Ulaşım Seçenekleri Component Kullanımı**
        if (showTransportDialog.value) {
            TransportationSelectionComponent(
                taxiFares = taxiFares,
                duration = viewModel.duration.collectAsState().value, // 📌 Eksik parametre eklendi
                selectedOptions = selectedTransportOptions.toSet(),
                onOptionSelected = { transportType, isChecked ->
                    viewModel.updateSelectedTransportOptions(transportType, isChecked)
                },
                onApply = {
                    showTransportDialog.value = false
                    viewModel.clearTransportSelections()

                    // 📌 PostViewModel'e ilan verilerini kaydet
                    // 📌 Eğer giriş yapan kullanıcı varsa, bilgilerini al
                    val postData = Post(
                        postId = UUID.randomUUID().toString(),
                        userId = currentUser?.uid ?: "",  // 🔥 Kullanıcı ID EKLENDİ!
                        username = currentUser?.displayName ?: "Bilinmeyen Kullanıcı", // 🔥 Kullanıcı adını kaydet
                        userPhotoUrl = currentUser?.photoUrl?.toString() ?: "", // 🔥 Kullanıcı fotoğrafını kaydet
                        fromLocation = viewModel.fromLocationText.value,
                        toLocation = viewModel.toLocationText.value,
                        estimatedPrice = taxiFares.entries.joinToString { "${it.key}: ${it.value} TL" },
                        transportType = selectedTransportOptions.toList(),
                        timestamp = Timestamp.now(),
                        distanceKm = "%.2f km".format(viewModel.distance.value),  // ✅ DOUBLE → STRING DÖNÜŞÜMÜ
                        estimatedDuration = viewModel.duration.value, // ✅ DÜZELTİLDİ
                    )
                   // 📌 Firestore'a kaydet
                    postViewModel.addPost(postData)

                    // 📌 Post ekranına yönlendir
                    navController.navigate("post_screen")
                },
                onCancel = {
                    viewModel.clearTransportSelections()
                    showTransportDialog.value = false
                }
            )
        }
        }
    }
