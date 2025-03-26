package com.cumaliguzel.free.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cumaliguzel.free.R

@Composable
fun TransportationSelectionComponent(
    taxiFares: Map<String, Double>,
    duration: String, // 🆕 Trafik süresi parametresi eklendi
    selectedOptions: Set<String>,
    onOptionSelected: (String, Boolean) -> Unit,
    onApply: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Pop-up kapanmasın */ },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ulaşım Türünü Seçin")
            }
        },
        text = {
            Column {
                // 📌 Fiyat bilgisi metni
                Text(
                    "Gösterilen fiyatlar tahminidir. Trafik durumu ve yoğunluğuna göre değişiklik gösterebilir!",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = duration, // 🆕 Trafik süresi üst sağ köşede
                    color = Color.Red,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                LazyColumn {
                    items(taxiFares.keys.toList()) { transportType ->
                        val fare = taxiFares[transportType] ?: 0.0

                        val iconRes = when (transportType) {
                            "Tag" -> R.drawable.transport
                            "Sarı Taksi" -> R.drawable.taxi
                            "Turkuaz Taksi" -> R.drawable.turkuaztaxi
                            "Siyah Taksi" -> R.drawable.siyahtaxi
                            else -> R.drawable.transport // Varsayılan ikon
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = transportType,
                                modifier = Modifier.size(64.dp) // 📌 64px ikon
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = transportType)
                                Text(text = "${"%.2f".format(fare)} TL", style = MaterialTheme.typography.bodySmall)
                            }

                            Checkbox(
                                checked = transportType in selectedOptions,
                                onCheckedChange = { isChecked ->
                                    onOptionSelected(transportType, isChecked)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onApply, enabled = selectedOptions.isNotEmpty()) {
                Text("Uygula")
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text("İptal")
            }
        }
    )
}