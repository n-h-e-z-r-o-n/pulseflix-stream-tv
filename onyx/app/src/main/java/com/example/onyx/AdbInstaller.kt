package com.example.onyx

import android.content.Context
import android.util.Base64
import com.tananaev.adblib.AdbConnection
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdbInstaller(private val context: Context, private val connection: AdbConnection) {

    suspend fun pushAndInstallApk(onProgress: (String) -> Unit): Boolean = withContext(Dispatchers.IO) {
        try {
            onProgress("Checking for latest version on GitHub...")
            val versionJsonUrl = BuildConfig.APPV_J
            val connection = (java.net.URL(versionJsonUrl).openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                connect()
            }

            if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                onProgress("Failed to fetch version info. HTTP ${connection.responseCode}")
                return@withContext false
            }

            val reader = connection.inputStream.reader()
            val updateInfo = com.google.gson.Gson().fromJson(reader, com.example.onyx.OnyxObjects.AppUpdater.UpdateInfo::class.java)
            val downloadUrlString = updateInfo.downloadUrl

            onProgress("Downloading latest APK from GitHub...")
            val apkConnection = (java.net.URL(downloadUrlString).openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 60000 // 60 seconds read timeout for large files
                connect()
            }

            if (apkConnection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                onProgress("Failed to download APK. HTTP ${apkConnection.responseCode}")
                return@withContext false
            }

            val downloadsDir = File(context.getExternalFilesDir(null), "TVUpdates")
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val apkFile = File(downloadsDir, "tv_update.apk")
            if (apkFile.exists()) apkFile.delete()

            val fileLength = apkConnection.contentLength
            val input = apkConnection.inputStream
            val output = java.io.FileOutputStream(apkFile)
            val data = ByteArray(4096)
            var count: Int
            var totalDownload = 0L
            var lastDownloadProgress = -1
            
            while (input.read(data).also { count = it } != -1) {
                totalDownload += count
                output.write(data, 0, count)
                if (fileLength > 0) {
                    val progress = (totalDownload * 100 / fileLength).toInt()
                    if (progress != lastDownloadProgress) {
                        lastDownloadProgress = progress
                        onProgress("Downloading update... $progress%")
                    }
                }
            }
            output.flush()
            output.close()
            input.close()

            val remoteApkPath = "/data/local/tmp/onyx_update.apk"

            // 1. Push APK using official ADB SYNC protocol
            onProgress("Pushing downloaded APK to TV... 0%")
            pushApkSync(apkFile, remoteApkPath, onProgress)

            // 2. Install the APK
            onProgress("Installing APK on TV... (Please wait, this can take a minute)")
            val installSuccess = installApk(remoteApkPath)
            
            // 3. Cleanup
            cleanup(remoteApkPath)

            if (installSuccess) {
                onProgress("Success! App installed on TV.")
                return@withContext true
            } else {
                onProgress("Failed to install APK. See logs.")
                return@withContext false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            onProgress("Error during install: ${e.message}")
            return@withContext false
        }
    }

    private fun pushApkSync(apkFile: File, remotePath: String, onProgress: (String) -> Unit) {
        val stream = connection.open("sync:")
        
        // 1. Send SEND request
        val pathAndMode = "$remotePath,33206".toByteArray(Charsets.UTF_8)
        val req = java.nio.ByteBuffer.allocate(8 + pathAndMode.size).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        req.put("SEND".toByteArray(Charsets.UTF_8))
        req.putInt(pathAndMode.size)
        req.put(pathAndMode)
        stream.write(req.array())
        
        // 2. Send DATA chunks
        // CRITICAL: The max ADB payload (maxData) for older Android TVs is strictly 4096 bytes. 
        // If we send a WRTE packet larger than this, the TV silently drops the connection and hangs!
        // We use a 2048 byte chunk + 8 byte header = 2056 bytes (well under the 4096 limit).
        val buffer = ByteArray(2048)
        val fileLength = apkFile.length()
        var totalPush = 0L
        var lastPushProgress = -1
        
        FileInputStream(apkFile).use { fis ->
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                // Combine DATA header and actual chunk to send as a single payload block
                val payload = java.nio.ByteBuffer.allocate(8 + bytesRead).order(java.nio.ByteOrder.LITTLE_ENDIAN)
                payload.put("DATA".toByteArray(Charsets.UTF_8))
                payload.putInt(bytesRead)
                payload.put(buffer, 0, bytesRead)
                
                stream.write(payload.array())
                
                totalPush += bytesRead
                if (fileLength > 0) {
                    val progress = (totalPush * 100 / fileLength).toInt()
                    if (progress != lastPushProgress) {
                        lastPushProgress = progress
                        // Update UI on every percentage point change
                        onProgress("Pushing downloaded APK to TV... $progress%")
                    }
                }
            }
        }
        
        // 3. Send DONE
        val done = java.nio.ByteBuffer.allocate(8).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        done.put("DONE".toByteArray(Charsets.UTF_8))
        done.putInt((System.currentTimeMillis() / 1000).toInt())
        stream.write(done.array())
        
        // 4. Read response
        val response = stream.read()
        if (response != null && response.size >= 4) {
            val status = String(response, 0, 4)
            if (status != "OKAY") {
                stream.close()
                val errorMsg = if (response.size > 8) String(response, 8, response.size - 8) else ""
                throw Exception("Sync failed with status: $status $errorMsg")
            }
        }
        
        stream.close()
    }

    private fun installApk(remotePath: String): Boolean {
        // Run pm install
        val stream = connection.open("shell:pm install -r $remotePath")
        
        var isSuccess = false
        var output = ""
        try {
            while (!stream.isClosed) {
                val responseBytes = stream.read()
                if (responseBytes == null || responseBytes.isEmpty()) break
                val responseChunk = String(responseBytes)
                output += responseChunk
                if (output.contains("Success")) {
                    isSuccess = true
                    break
                }
                if (output.contains("Failure")) {
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            stream.close()
        }
        
        return isSuccess
    }

    private fun cleanup(remotePath: String) {
        try {
            val stream = connection.open("shell:rm $remotePath")
            stream.read()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
