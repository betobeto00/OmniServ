package com.omnimargen.omniserv.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateInstaller @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var downloadId: Long = -1

    fun downloadAndInstall(updateInfo: UpdateInfo, onProgress: (Int) -> Unit = {}) {
        val fileName = "OmniServ_${updateInfo.versionName}.apk"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        if (file.exists()) {
            installApk(file)
            return
        }

        val request = DownloadManager.Request(Uri.parse(updateInfo.apkUrl))
            .setTitle("Descargando OmniServ ${updateInfo.versionName}")
            .setDescription("Descargando actualización...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(file))
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = downloadManager.enqueue(request)
    }

    fun installApk(file: File) {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    }

    fun cancelDownload() {
        if (downloadId != -1L) {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.remove(downloadId)
            downloadId = -1
        }
    }
}
