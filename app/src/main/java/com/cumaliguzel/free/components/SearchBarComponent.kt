package com.cumaliguzel.free.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.model.AutocompletePrediction

@Composable
fun SearchBarComponent(
    searchText: String,
    searchResults: List<AutocompletePrediction>,
    onSearchTextChange: (String) -> Unit,
    onResultClick: (AutocompletePrediction) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedPrediction by remember { mutableStateOf<AutocompletePrediction?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()


    ) {
        // 📌 Arama Çubuğu
        OutlinedTextField(
            value = searchText,
            onValueChange = { query -> onSearchTextChange(query) },
            label = { Text("Konum Ara") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
                .clip(RoundedCornerShape(16.dp)),
            singleLine = true,
            maxLines = 1,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = Color.Gray
                )
            },
            shape = RoundedCornerShape(16.dp),
        )

        // 📌 Konum Önerileri Listesi
        if (searchText.isNotEmpty() && searchResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 4.dp)
            ) {
                items(searchResults) { prediction ->
                    Text(
                        text = prediction.getPrimaryText(null).toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPrediction = prediction
                                showDialog = true
                            }
                            .padding(12.dp)
                            .background(Color.White)
                    )
                }
            }
        }
    }

    // 📌 Kullanıcı bir konum önerisine tıklayınca onay iste
    if (showDialog && selectedPrediction != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Konum Seçimi") },
            text = { Text("Bu konumu seçmek istiyor musunuz?") },
            confirmButton = {
                Button(onClick = {
                    selectedPrediction?.let { onResultClick(it) }
                    showDialog = false
                }) {
                    Text("Evet")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Hayır")
                }
            }
        )
    }
}
