package com.example.onyx.OnyxObjects

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.onyx.BuildConfig
import com.example.onyx.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class AppUpdater(private val activity: Activity, private val coroutineScope: CoroutineScope) {
    
    private val versionJsonUrl = BuildConfig.APPV_J
    private var updateDialog: AlertDialog? = null

    data class UpdateInfo(
        @com.google.gson.annotations.SerializedName("versionCode")
        val versionCode: Int,
        @com.google.gson.annotations.SerializedName("versionName")
        val versionName: String,
        @com.google.gson.annotations.SerializedName("changelog")
        val changelog: String,
        @com.google.gson.annotations.SerializedName("downloadUrl")
        val downloadUrl: String
    )

    fun checkForUpdates(showToastOnUpToDate: Boolean = true) {
        if (showToastOnUpToDate) {
            Toast.makeText(activity, "Checking for updates...", Toast.LENGTH_SHORT).show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !activity.packageManager.canRequestPackageInstalls()
        ) {
            showInstallPermissionDialog()
            return
        }

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val connection = (URL(versionJsonUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                    connect()
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = connection.inputStream.reader()
                    val updateInfo = com.google.gson.Gson().fromJson(reader, UpdateInfo::class.java)

                    withContext(Dispatchers.Main) {
                        if (activity.isFinishing || activity.isDestroyed) return@withContext
                        val installedVersionCode = getInstalledVersionCode()
                        Log.d(
                            "UpdateCheck",
                            "Installed versionCode: $installedVersionCode, Remote versionCode: ${updateInfo.versionCode}"
                        )

                        if (updateInfo.versionCode > installedVersionCode) {
                            showUpdateConfirmation(updateInfo)
                        } else {
                            if (showToastOnUpToDate) {
                                Toast.makeText(activity, "App is up to date", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        if (activity.isFinishing || activity.isDestroyed) return@withContext
                        Toast.makeText(
                            activity,
                            "Failed to check for updates: Server Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    if (activity.isFinishing || activity.isDestroyed) return@withContext
                    error.printStackTrace()
                    Toast.makeText(
                        activity,
                        "Failed to check for updates: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getInstalledVersionCode(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity
                    .packageManager
                    .getPackageInfo(
                        activity.packageName,
                        PackageManager.PackageInfoFlags.of(0)
                    )
                    .longVersionCode
                    .toInt()
            } else {
                @Suppress("DEPRECATION")
                activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode
            }
        } catch (error: Exception) {
            BuildConfig.VERSION_CODE
        }
    }

    private fun showUpdateConfirmation(updateInfo: UpdateInfo) {
        AlertDialog.Builder(activity, R.style.CustomDialogTheme)
            .setTitle("Update Available: v${updateInfo.versionName}")
            .setMessage("Changelog:\n${updateInfo.changelog}\n\nWould you like to update now?")
            .setPositiveButton("Update Now") { _, _ ->
                downloadAndInstallApk(updateInfo.downloadUrl)
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun downloadAndInstallApk(downloadUrlString: String) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_update_progress, null)
        val progressBar = dialogView.findViewById<android.widget.ProgressBar>(R.id.updateProgressBar)
        val progressText = dialogView.findViewById<TextView>(R.id.updateProgressText)
        val sizeText = dialogView.findViewById<TextView>(R.id.updateSizeText)

        updateDialog = AlertDialog.Builder(activity, R.style.CustomDialogTheme)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        updateDialog?.show()
        updateDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val connection = (URL(downloadUrlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                    connect()
                }

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                }

                val fileLength = connection.contentLength
                val input: InputStream = connection.inputStream
                val downloadsDir = File(activity.getExternalFilesDir(null), "OnyxUpdates")
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val apkFile = File(downloadsDir, "onyx-update.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                val output = FileOutputStream(apkFile)
                val data = ByteArray(4096)
                var total = 0L
                var count: Int
                var lastProgress = 0

                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    output.write(data, 0, count)

                    if (fileLength > 0) {
                        val progress = (total * 100 / fileLength).toInt()
                        if (progress > lastProgress) {
                            lastProgress = progress
                            withContext(Dispatchers.Main) {
                                if (activity.isFinishing || activity.isDestroyed) return@withContext
                                progressBar.progress = progress
                                progressText.text = "$progress%"

                                val totalMb = String.format("%.1f", total / (1024f * 1024f))
                                val maxMb = String.format("%.1f", fileLength / (1024f * 1024f))
                                sizeText.text = "$totalMb MB / $maxMb MB"
                            }
                        }
                    }
                }

                output.flush()
                output.close()
                input.close()

                withContext(Dispatchers.Main) {
                    if (activity.isFinishing || activity.isDestroyed) return@withContext
                    updateDialog?.dismiss()
                    installApk(apkFile)
                }
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    if (activity.isFinishing || activity.isDestroyed) return@withContext
                    updateDialog?.dismiss()
                    error.printStackTrace()
                    Toast.makeText(activity, "Download failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun installApk(apkFile: File) {
        try {
            val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            activity.startActivity(intent)
            Toast.makeText(activity, "Installation started", Toast.LENGTH_SHORT).show()
        } catch (error: Exception) {
            Toast.makeText(activity, "Installation failed: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showInstallPermissionDialog() {
        AlertDialog.Builder(activity, R.style.CustomDialogTheme)
            .setTitle("Install Permission Required")
            .setMessage("This app needs permission to install APK files. Please enable 'Install unknown apps' permission in settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    fun dismissDialogs() {
        updateDialog?.dismiss()
    }
}
