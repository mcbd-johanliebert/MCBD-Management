package com.mcbdone.app.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progressPercent: Int, val downloadedBytes: Long, val totalBytes: Long) : DownloadState()
    data class DownloadCompleted(val apkFile: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class UpdateManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun downloadApk(url: String): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading(0, 0, 0))
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "MCBD-Glass-Updater/1.0 (Android; com.mcbdone.app)")
                .build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                emit(DownloadState.Error("Server returned code ${response.code}"))
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit(DownloadState.Error("Empty response from server"))
                return@flow
            }

            val totalBytes = body.contentLength()
            val updatesDir = File(context.cacheDir, "updates")
            if (!updatesDir.exists()) updatesDir.mkdirs()

            val targetFile = File(updatesDir, "MCBD-Glass-Update.apk")
            if (targetFile.exists()) targetFile.delete()

            body.byteStream().use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead: Long = 0
                    var lastPercent = 0

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        val percent = if (totalBytes > 0) {
                            ((totalRead * 100) / totalBytes).toInt().coerceIn(0, 100)
                        } else {
                            50 // Indeterminate progress fallback
                        }

                        if (percent != lastPercent || totalRead == totalBytes) {
                            lastPercent = percent
                            emit(DownloadState.Downloading(percent, totalRead, totalBytes))
                        }
                    }
                    outputStream.flush()
                }
            }

            emit(DownloadState.DownloadCompleted(targetFile))
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error downloading APK", e)
            emit(DownloadState.Error(e.message ?: "Failed to download update"))
        }
    }.flowOn(Dispatchers.IO)

    fun installApk(apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists() || apkFile.length() == 0L) {
                return Result.failure(Exception("APK file not found or empty"))
            }

            // Android O+ Unknown Source Permission check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Failed to launch installer", e)
            Result.failure(e)
        }
    }
}
