package com.omnimargen.omniserv.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val GITHUB_REPO = "betobeto00/OmniServ"
        private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"
    }

    suspend fun checkForUpdate(): UpdateInfo? {
        // Fallo de red/conexión: propagar la excepción para que la UI
        // informe de un error de verificación en vez de ("falsamente") "al día".
        val url = URL(GITHUB_API_URL)
        val connection = url.openConnection().apply {
            setRequestProperty("User-Agent", "OmniServ/Android")
            connectTimeout = 10000
            readTimeout = 10000
        }

        val jsonString = connection.getInputStream().bufferedReader().use { it.readText() }
        val json = JSONObject(jsonString)

        val tagName = json.getString("tag_name")
        val versionName = tagName.removePrefix("v")
        val body = json.getString("body")

        val assets = json.getJSONArray("assets")
        var apkUrl: String? = null
        var apkSize: Long = 0

        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.getString("name")
            if (name.endsWith(".apk")) {
                apkUrl = asset.getString("browser_download_url")
                apkSize = asset.getLong("size")
                break
            }
        }

        val currentVersion = getCurrentVersion()
        val isNewer = compareVersions(versionName, currentVersion) > 0

        return if (isNewer && apkUrl != null) {
            UpdateInfo(
                versionName = versionName,
                tagName = tagName,
                releaseNotes = body,
                apkUrl = apkUrl,
                apkSize = apkSize
            )
        } else {
            null
        }
    }

    private fun getCurrentVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.versionName ?: "1.0.0"
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionName ?: "1.0.0"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }

    private fun compareVersions(version1: String, version2: String): Int {
        val parts1 = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = version2.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLength) {
            val v1 = parts1.getOrElse(i) { 0 }
            val v2 = parts2.getOrElse(i) { 0 }

            if (v1 > v2) return 1
            if (v1 < v2) return -1
        }

        return 0
    }
}

data class UpdateInfo(
    val versionName: String,
    val tagName: String,
    val releaseNotes: String,
    val apkUrl: String,
    val apkSize: Long
)
