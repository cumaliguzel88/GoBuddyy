package com.cumaliguzel.free.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun LocationDialogComponent(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = "Konum Seçimi") },
            text = { Text(text = "Bu konumu seçmek istiyor musunuz?") },
            confirmButton = {
                Button(onClick = { onConfirm() }) {
                    Text("Evet")
                }
            },
            dismissButton = {
                Button(onClick = { onDismiss() }) {
                    Text("Hayır")
                }
            }
        )
    }
}
