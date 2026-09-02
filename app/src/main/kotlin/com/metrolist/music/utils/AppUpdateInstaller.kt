package com.metrolist.music.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object AppUpdateInstaller {
    suspend fun downloadAndInstall(context: Context, url: String, fileName: String) =
        withContext(Dispatchers.IO) {
            val appContext = context.applicationContext
            val updatesDir = File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "updates")
            updatesDir.mkdirs()
            val apk = File(updatesDir, fileName)
            if (apk.exists()) apk.delete()

            val manager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Downloading Musie update")
                .setDescription(fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(apk))
                .setMimeType(APK_MIME_TYPE)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
            val downloadId = manager.enqueue(request)

            while (true) {
                val cursor = manager.query(DownloadManager.Query().setFilterById(downloadId))
                cursor.use {
                    if (!it.moveToFirst()) error("Update download was cancelled")
                    when (it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> return@withContext installIntent(appContext, apk)
                        DownloadManager.STATUS_FAILED -> {
                            val reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                            error("Update download failed ($reason)")
                        }
                    }
                }
                delay(750)
            }
        }

    private fun installIntent(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.FileProvider", apk)
        context.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, APK_MIME_TYPE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
}
