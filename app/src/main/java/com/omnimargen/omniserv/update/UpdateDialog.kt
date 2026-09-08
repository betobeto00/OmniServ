package com.omnimargen.omniserv.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    isDownloading: Boolean,
    downloadProgress: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        title = {
            Text("Actualización disponible")
        },
        text = {
            Column {
                Text(
                    text = "Versión ${updateInfo.versionName}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tamaño: ${formatFileSize(updateInfo.apkSize)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Novedades:",
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = updateInfo.releaseNotes,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (isDownloading) {
                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { downloadProgress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Descargando... $downloadProgress%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDownloading
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Actualizar")
                }
            }
        },
        dismissButton = {
            if (!isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("Ahora no")
                }
            }
        }
    )
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024
    val mb = kb / 1024
    return if (mb > 1) {
        "${String.format("%.1f", mb)} MB"
    } else {
        "${kb} KB"
    }
}
