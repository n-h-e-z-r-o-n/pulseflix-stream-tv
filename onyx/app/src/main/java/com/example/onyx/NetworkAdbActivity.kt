package com.example.onyx

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.onyx.OnyxObjects.GlobalUtils
import kotlinx.coroutines.withContext

class    NetworkAdbActivity : AppCompatActivity() {

    private lateinit var etIpAddress: EditText
    private lateinit var btnConnect: Button
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_network_adb)
        supportActionBar?.hide()
        GlobalUtils.hideSystemUI(this)
        etIpAddress = findViewById(R.id.etIpAddress)
        btnConnect = findViewById(R.id.btnConnect)
        tvStatus = findViewById(R.id.tvStatus)

        btnConnect.setOnClickListener {
            val ip = etIpAddress.text.toString().trim()
            if (ip.isEmpty()) {
                Toast.makeText(this, "Please enter an IP address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startAdbConnection(ip)
        }

        if (!GlobalUtils.isTvDevice(this)) {
            GlobalUtils.enableImmersiveMode(this)
        }
    }

    private fun startAdbConnection(ip: String) {
        btnConnect.isEnabled = false
        tvStatus.text = "Connecting to $ip:5555..."
        
        lifecycleScope.launch(Dispatchers.IO) {
            val adbManager = AdbManager(this@NetworkAdbActivity)
            try {
                // Connect to ADB
                val connection = adbManager.connect(ip)
                
                // Update UI
                withContext(Dispatchers.Main) {
                    tvStatus.text = "Connected! Preparing to push APK..."
                }
                
                // Push and install
                val installer = AdbInstaller(this@NetworkAdbActivity, connection)
                installer.pushAndInstallApk { progressMsg ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        tvStatus.text = progressMsg
                    }
                }
                
            } catch (e: java.net.ConnectException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    tvStatus.text = "Connection Refused. Is Network Debugging enabled on the TV?"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    tvStatus.text = "Error: ${e.message}"
                }
            } finally {
                adbManager.disconnect()
                withContext(Dispatchers.Main) {
                    btnConnect.isEnabled = true
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            GlobalUtils.hideSystemUI(this)
        }
    }
}
