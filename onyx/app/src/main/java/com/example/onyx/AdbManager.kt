package com.example.onyx

import android.content.Context
import android.util.Base64
import com.tananaev.adblib.AdbBase64
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import java.io.File
import java.net.Socket

class AdbManager(private val context: Context) {

    private var adbConnection: AdbConnection? = null
    
    // AdbBase64 implementation for Android
    private val adbBase64 = AdbBase64 { data -> Base64.encodeToString(data, Base64.NO_WRAP) }

    fun connect(ipAddress: String): AdbConnection {
        val crypto = getAdbCrypto()
        
        // Open raw TCP socket to the TV
        val socket = Socket(ipAddress, 5555)
        
        // Create the ADB connection wrapper
        val connection = AdbConnection.create(socket, crypto)
        
        // Start the connection and handshake
        connection.connect()
        adbConnection = connection
        return connection
    }

    private fun getAdbCrypto(): AdbCrypto {
        val prefsDir = context.filesDir
        val pubKeyFile = File(prefsDir, "adbkey.pub")
        val privKeyFile = File(prefsDir, "adbkey")

        return if (pubKeyFile.exists() && privKeyFile.exists()) {
            AdbCrypto.loadAdbKeyPair(adbBase64, privKeyFile, pubKeyFile)
        } else {
            val crypto = AdbCrypto.generateAdbKeyPair(adbBase64)
            crypto.saveAdbKeyPair(privKeyFile, pubKeyFile)
            crypto
        }
    }

    fun disconnect() {
        try {
            adbConnection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
