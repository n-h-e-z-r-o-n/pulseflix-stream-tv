package com.example.onyx

import android.Manifest
import android.view.LayoutInflater
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

import com.example.onyx.Database.AppDatabase
import com.example.onyx.Database.SessionManger
import com.example.onyx.OnyxObjects.GlobalUtils
import com.example.onyx.OnyxObjects.NavAction


class Profile_Page : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var  sm: SessionManger

    private lateinit var moviesWatchedText: TextView
    private lateinit var seriesWatchedText: TextView
    private lateinit var qualityValueText: TextView
    private lateinit var themeValueText: TextView
    private lateinit var appVersionText: TextView
    
    // APK Update related properties
    // APK Update related properties
    private var updateDialog: androidx.appcompat.app.AlertDialog? = null
    // installPermissionLauncher removed as it's not applicable for REQUEST_INSTALL_PACKAGES (requires Intent)
    
    // GitHub raw URL for the APK file - replace with your actual URL
    // TODO: USER - Update this link to point to your version.json file
    private val versionJsonUrl = "https://raw.githubusercontent.com/n-h-e-z-r-o-n/tv-APP/main/onyx/app/release/version.json"
    
    // Data class for update info
    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val changelog: String,
        val downloadUrl: String
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalUtils.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_page)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        db = AppDatabase(this)         // Initialize database
        sm = SessionManger(this)         // Initialize session manager

        NavAction.setupSidebar(this)

        ////////////////////////////////////////////////////////////////////////////////////////////
        val profileImage = findViewById<ImageView>(R.id.ProfileImg)
        val assetPath = "file:///android_asset/${sm.getUserAvatar()}"
        Glide.with(this)
            .load(assetPath)
            .placeholder(R.drawable.ic_person)
            .into(profileImage)

        val logoutBtn = findViewById<LinearLayout>(R.id.logoutBtn)
        logoutBtn.setOnClickListener {
            sm.clearSession()
            val intent = Intent(this, Login_Page::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        ////////////////////////////////////////////////////////////////////////////////////////////

        // Initialize views
        initializeViews()
        
        // Load saved settings
        loadSettings()
        
        // Setup click listeners
        setupClickListeners()
        
        // Load statistics
        loadStatistics()
        
        // Setup focus handling for TV remote
        setupFocusHandling()

        getRemainingDays()
    }
    
    private fun initializeViews() {
        moviesWatchedText = findViewById(R.id.moviesWatched)
        seriesWatchedText = findViewById(R.id.seriesWatched)
        themeValueText = findViewById(R.id.themeValue)
        appVersionText = findViewById(R.id.appVersion)
        
        // Set app version using GlobalUtils
        appVersionText.text = GlobalUtils.getAppVersion(this)
    }
    
    private fun loadSettings() {


        // Load theme setting using GlobalUtils
        val currentTheme = GlobalUtils.getAppTheme(this)
        themeValueText.text = currentTheme.replaceFirstChar { it.uppercase() }
    }
    
    private fun setupClickListeners() {


        // Theme setting click
        val themeSetting = findViewById<LinearLayout>(R.id.themeSetting)
        themeSetting.setOnClickListener {
            showThemeDialog()
        }
        
        // Clear cache click
        val clearCache = findViewById<LinearLayout>(R.id.clearCache)
        clearCache.setOnClickListener {
            if (GlobalUtils.clearAppCache(this)) {
                Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to clear cache", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Version info click
        val versionInfo = findViewById<LinearLayout>(R.id.versionInfo)
        versionInfo.setOnClickListener {
            Toast.makeText(this, "Onyx TV App v${appVersionText.text}", Toast.LENGTH_LONG).show()
        }
        
        // Check for updates click
        val checkUpdates = findViewById<LinearLayout>(R.id.checkUpdates)
        checkUpdates.setOnClickListener {
            checkForUpdates()
        }
        
        // Restart app click
        val restartApp = findViewById<LinearLayout>(R.id.restartApp)
        restartApp.setOnClickListener {
            showRestartDialog()
        }
        
        // Terms and Conditions click
        val termsAndConditions = findViewById<LinearLayout>(R.id.termsAndConditions)
        termsAndConditions.setOnClickListener {
            startActivity(android.content.Intent(this, TermsAndConditionsActivity::class.java))
        }
    }
    
    private fun loadStatistics() {
        // Load watched movies count using GlobalUtils
        moviesWatchedText.text = GlobalUtils.getMoviesWatched(this).toString()
        
        // Load watched series count using GlobalUtils
        seriesWatchedText.text = GlobalUtils.getSeriesWatched(this).toString()
    }
    
    private fun showQualityDialog() {
        val qualities = arrayOf("720p", "1080p", "4K")
        val currentQuality = qualityValueText.text.toString()
        val currentIndex = qualities.indexOf(currentQuality)
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle("Select Video Quality")
            .setSingleChoiceItems(qualities, currentIndex) { dialog, which ->
                val selectedQuality = qualities[which]
                qualityValueText.text = selectedQuality
                GlobalUtils.setVideoQuality(this, selectedQuality)
                Toast.makeText(this, "Video quality set to $selectedQuality", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showThemeDialog() {
        val themes = GlobalUtils.getAvailableThemes()
        val currentTheme = GlobalUtils.getAppTheme(this)
        val currentIndex = themes.indexOf(currentTheme)
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle("Select App Theme")
            .setSingleChoiceItems(themes.map { it.replaceFirstChar { char -> char.uppercase() } }.toTypedArray(), currentIndex) { dialog, which ->
                val selectedTheme = themes[which]
                GlobalUtils.setAppTheme(this, selectedTheme)
                themeValueText.text = selectedTheme.replaceFirstChar { it.uppercase() }
                dialog.dismiss()
                
                // Show restart suggestion dialog
                showThemeChangeDialog(selectedTheme)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun checkForUpdates() {
        Toast.makeText(this, "Checking for updates...", Toast.LENGTH_SHORT).show()

        // Check if install permission is granted (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                // Request install permission
                showInstallPermissionDialog()
                return
            }
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(versionJsonUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = java.io.InputStreamReader(inputStream)
                    val updateInfo = com.google.gson.Gson().fromJson(reader, UpdateInfo::class.java)
                    
                    withContext(Dispatchers.Main) {
                        val currentVersionCode = BuildConfig.VERSION_CODE
                        
                        if (updateInfo.versionCode > currentVersionCode) {
                            showUpdateConfirmation(updateInfo)
                        } else {
                            Toast.makeText(this@Profile_Page, "App is up to date", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                         Toast.makeText(this@Profile_Page, "Failed to check for updates: Server Error", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                    Toast.makeText(this@Profile_Page, "Failed to check for updates: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showUpdateConfirmation(updateInfo: UpdateInfo) {
        androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("Update Available: v${updateInfo.versionName}")
            .setMessage("Changelog:\n${updateInfo.changelog}\n\nWould you like to update now?")
            .setPositiveButton("Update Now") { _, _ ->
                downloadAndInstallApk(updateInfo.downloadUrl)
            }
            .setNegativeButton("Later", null)
            .show()
    }
    
    private fun downloadAndInstallApk(downloadUrlString: String) {
        // Setup custom progress dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_progress, null)
        val progressBar = dialogView.findViewById<android.widget.ProgressBar>(R.id.updateProgressBar)
        val progressText = dialogView.findViewById<TextView>(R.id.updateProgressText)
        val sizeText = dialogView.findViewById<TextView>(R.id.updateSizeText)
        
        updateDialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        updateDialog?.show()
        
        // Make the dialog background transparent to show the CardView corners
        updateDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(downloadUrlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                }
                
                val fileLength = connection.contentLength
                val input: InputStream = connection.inputStream
                
                // Create downloads directory if it doesn't exist - using app-specific directory which needs no permissions
                val downloadsDir = File(getExternalFilesDir(null), "OnyxUpdates")
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val apkFile = File(downloadsDir, "onyx-update.apk")
                // Delete old update if exists
                if (apkFile.exists()) {
                    apkFile.delete()
                }
                
                val output = FileOutputStream(apkFile)
                
                val data = ByteArray(4096) // Increased buffer size
                var total: Long = 0
                var count: Int
                
                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    output.write(data, 0, count)
                    
                    // Update progress
                    if (fileLength > 0) {
                        // Calculate percentage
                        val progress = (total * 100 / fileLength).toInt()
                        
                        withContext(Dispatchers.Main) {
                            progressBar.progress = progress
                            progressText.text = "$progress%"
                            
                            // Format bytes to MB
                            val totalMb = String.format("%.1f", total / (1024f * 1024f))
                            val maxMb = String.format("%.1f", fileLength / (1024f * 1024f))
                            sizeText.text = "$totalMb MB / $maxMb MB"
                        }
                    }
                }
                
                output.flush()
                output.close()
                input.close()
                
                withContext(Dispatchers.Main) {
                    updateDialog?.dismiss()
                    installApk(apkFile)
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateDialog?.dismiss()
                    e.printStackTrace()
                    Toast.makeText(this@Profile_Page, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun installApk(apkFile: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(this, "${packageName}.fileprovider", apkFile)
            } else {
                Uri.fromFile(apkFile)
            }
            
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(intent)
            Toast.makeText(this, "Installation started", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Installation failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showInstallPermissionDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle("Install Permission Required")
            .setMessage("This app needs permission to install APK files. Please enable 'Install unknown apps' permission in settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showThemeChangeDialog(selectedTheme: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle("Theme Changed")
            .setMessage("Theme changed to ${selectedTheme.replaceFirstChar { it.uppercase() }}. Would you like to restart the app now to see the full effect?")
            .setPositiveButton("Restart Now") { _, _ ->
                Toast.makeText(this, "Restarting app...", Toast.LENGTH_SHORT).show()
                GlobalUtils.restartApp(this)
            }
            .setNegativeButton("Later") { _, _ ->
                Toast.makeText(this, "Theme will be applied after restart", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
    
    private fun showRestartDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle("Restart App")
            .setMessage("Are you sure you want to restart the application? This will close all current activities and restart the app.")
            .setPositiveButton("Restart") { _, _ ->
                Toast.makeText(this, "Restarting app...", Toast.LENGTH_SHORT).show()
                GlobalUtils.restartApp(this)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    
    private fun setupFocusHandling() {
        // Setup focus handling for TV remote navigation
        val focusableViews = listOf(
            findViewById<LinearLayout>(R.id.themeSetting),
            findViewById<LinearLayout>(R.id.versionInfo),
            findViewById<LinearLayout>(R.id.clearCache),
            findViewById<LinearLayout>(R.id.checkUpdates),
            findViewById<LinearLayout>(R.id.restartApp),
            findViewById<LinearLayout>(R.id.termsAndConditions)
        )
        
        focusableViews.forEach { view ->
            view.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    v.background = getDrawable(R.drawable.setting_item_background)
                    v.scaleX = 1.0f
                    v.scaleY = 1.05f
                } else {
                    v.background = getDrawable(R.drawable.setting_item_background)
                    v.scaleX = 1.0f
                    v.scaleY = 1.0f
                }
            }
        }

    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            1001 -> {
                // Storage permission request result - No longer needed/used
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        updateDialog?.dismiss()
    }


    private fun getRemainingDays() {
        val subscriptionWidget = findViewById<TextView>(R.id.SubscriptionLeft)

        val remainingDays = db.getSubscriptionDaysLeft()  // <-- NEW DB function

        val displayText = when {
            remainingDays <= 0 -> "expired"
            else -> remainingDays.toString()
        }

        subscriptionWidget.text = displayText
    }




}