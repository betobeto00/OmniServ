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

    /** Archivo local donde DownloadManager escribe el APK de la actualización. */
    fun getTargetFile(updateInfo: UpdateInfo): File? {
        val fileName = "OmniServ_${updateInfo.versionName}.apk"
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return null
        return File(dir, fileName)
    }

    fun downloadAndInstall(updateInfo: UpdateInfo) {
        val file = getTargetFile(updateInfo) ?: return

        if (file.exists() && file.length() > 0) {
            installApk(file)
            return
        }

        val request = DownloadManager.Request(Uri.parse(updateInfo.apkUrl))
            .setTitle("Descargando OmniServ ${updateInfo.versionName}")
            .setDescription("Descargando actualización...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, file.name)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            ?: return
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
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun cancelDownload() {
        if (downloadId != -1L) {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                ?: return
            downloadManager.remove(downloadId)
            downloadId = -1
        }
    }
}
