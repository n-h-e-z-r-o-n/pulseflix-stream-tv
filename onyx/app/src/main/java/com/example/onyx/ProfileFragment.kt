package com.example.onyx

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.onyx.Database.AppDatabase
import com.example.onyx.Database.SessionManger
import com.example.onyx.OnyxObjects.GlobalUtils
import com.example.onyx.databinding.FragmentProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

import com.example.onyx.OnyxObjects.AppUpdater

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase
    private lateinit var sm: SessionManger

    private var themeDialog: AlertDialog? = null
    private var restartDialog: AlertDialog? = null
    private var logoutDialog: AlertDialog? = null

    private lateinit var appUpdater: AppUpdater

    private var lastFocusedViewId: Int = R.id.themeSetting
    private var currentUserId: Int = -1

    private data class ProfileUiState(
        val username: String,
        val memberLabel: String,
        val moviesWatched: Int,
        val episodesWatched: Int,
        val animeFavorites: Int,
        val subscriptionDaysLeft: Long,
        val subscriptionType: String,
        val subscriptionActive: Boolean
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        db = AppDatabase(requireActivity())
        sm = SessionManger(requireActivity())
        currentUserId = sm.getUserId()
        appUpdater = AppUpdater(requireActivity(), lifecycleScope)

        initializeStaticUi()
        loadProfileImage()
        loadSettings()
        setupClickListeners()
        setupFocusHandling()
        loadProfileData()
    }

    override fun onResume() {
        super.onResume()
        binding.root.post {
            val target = binding.root.findViewById<View>(lastFocusedViewId) ?: binding.themeSetting
            if (target.isShown && target.isFocusable) {
                target.requestFocus()
            } else {
                binding.themeSetting.requestFocus()
            }
        }
    }

    private fun initializeStaticUi() {
        binding.appVersion.text = GlobalUtils.getAppVersion(requireActivity())
    }

    private fun loadProfileImage() {
        val assetPath = "file:///android_asset/${sm.getUserAvatar()}"
        Glide.with(this)
            .load(assetPath)
            .placeholder(R.drawable.ic_person)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.profileImage)
    }

    private fun loadSettings() {
        val currentTheme = GlobalUtils.getAppTheme(requireActivity())
        binding.themeValue.text = GlobalUtils.getThemeDisplayName(currentTheme)

        val isDynamicColor = sm.isDynamicColorEnabled()
        binding.dynamicColorValue.text = if (isDynamicColor) "On" else "Off"
        
        val isLowQuality = GlobalUtils.isLowQualityImagesEnabled(requireContext())
        binding.lowQualityValue.text = if (isLowQuality) "On" else "Off"
    }

    private fun setupClickListeners() {
        binding.logoutBtn.setOnClickListener {
            showLogoutDialog()
        }

        binding.themeSetting.setOnClickListener {
            showThemeDialog()
        }

        binding.dynamicColorSetting.setOnClickListener {
            val newState = !sm.isDynamicColorEnabled()
            sm.setDynamicColorEnabled(newState)
            binding.dynamicColorValue.text = if (newState) "On" else "Off"
            Toast.makeText(
                requireActivity(),
                "Dynamic Color ${if (newState) "Enabled" else "Disabled"}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.lowQualitySetting.setOnClickListener {
            val newState = !GlobalUtils.isLowQualityImagesEnabled(requireContext())
            GlobalUtils.setLowQualityImagesEnabled(requireContext(), newState)
            binding.lowQualityValue.text = if (newState) "On" else "Off"
            Toast.makeText(
                requireActivity(),
                "Low Quality Images ${if (newState) "Enabled" else "Disabled"}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.editProfileSetting.setOnClickListener { showEditProfileDialog() }
        binding.clearWatchHistorySetting.setOnClickListener { showClearHistoryDialog() }
        binding.clearFavoritesSetting.setOnClickListener { showClearFavoritesDialog() }
        binding.deleteAccountSetting.setOnClickListener { showDeleteAccountDialog() }

        binding.clearCache.setOnClickListener {
            val cleared = GlobalUtils.clearAppCache(requireActivity())
            Toast.makeText(
                requireActivity(),
                if (cleared) "Cache cleared successfully" else "Failed to clear cache",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.versionInfo.setOnClickListener {
            Toast.makeText(
                requireActivity(),
                "Installed version ${binding.appVersion.text}",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.checkUpdates.setOnClickListener {
            appUpdater.checkForUpdates()
        }

        binding.restartApp.setOnClickListener {
            showRestartDialog()
        }

        binding.termsAndConditions.setOnClickListener {
            startActivity(Intent(requireActivity(), TermsAndConditionsActivity::class.java))
        }
    }

    private fun setupFocusHandling() {
        val focusableViews = listOf(
            binding.logoutBtn,
            binding.editProfileSetting,
            binding.clearWatchHistorySetting,
            binding.clearFavoritesSetting,
            binding.deleteAccountSetting,
            binding.themeSetting,
            binding.dynamicColorSetting,
            binding.lowQualitySetting,
            binding.versionInfo,
            binding.clearCache,
            binding.checkUpdates,
            binding.restartApp,
            binding.termsAndConditions
        )

        focusableViews.forEach { itemView ->
            itemView.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    lastFocusedViewId = view.id
                }
                view.animate()
                    .scaleX(if (hasFocus) 1.03f else 1f)
                    .scaleY(if (hasFocus) 1.03f else 1f)
                    .setDuration(140)
                    .start()
            }
        }
    }

    private fun loadProfileData() {
        lifecycleScope.launch(Dispatchers.Main) {
            val uiState = withContext(Dispatchers.IO) {
                db.resetExpiredSubscription()

                val username = db.getUsernameById(currentUserId)
                    ?.takeIf { it.isNotBlank() }
                    ?: "Profile $currentUserId"
                val subscriptionType = db.getSubscriptionType()
                val subscriptionActive = db.isSubscriptionActive()
                val subscriptionDaysLeft = db.getSubscriptionDaysLeft()

                ProfileUiState(
                    username = username,
                    memberLabel = buildMemberLabel(currentUserId, subscriptionType, subscriptionActive),
                    moviesWatched = GlobalUtils.getMoviesWatched(requireActivity()),
                    episodesWatched = GlobalUtils.getSeriesWatched(requireActivity()),
                    animeFavorites = db.getFavoriteAnimeCount(currentUserId),
                    subscriptionDaysLeft = subscriptionDaysLeft,
                    subscriptionType = subscriptionType,
                    subscriptionActive = subscriptionActive
                )
            }

            if (!isAdded || _binding == null) return@launch
            renderProfileData(uiState)
        }
    }

    private fun renderProfileData(uiState: ProfileUiState) {
        binding.profileName.text = uiState.username
        binding.profileMeta.text = uiState.memberLabel
        binding.moviesWatched.text = uiState.moviesWatched.toString()
        binding.seriesWatched.text = uiState.episodesWatched.toString()
        binding.animeWatched.text = uiState.animeFavorites.toString()

        if (uiState.subscriptionActive) {
            binding.subscriptionLeft.text = uiState.subscriptionDaysLeft.toString()
            binding.subscriptionLeft.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_green_light)
            )
            binding.subscriptionStatus.text = "${formatSubscriptionType(uiState.subscriptionType)} active"
        } else {
            binding.subscriptionLeft.text = "0"
            binding.subscriptionLeft.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
            )
            binding.subscriptionStatus.text = "Inactive"
        }
    }

    private fun buildMemberLabel(userId: Int, subscriptionType: String, subscriptionActive: Boolean): String {
        val planLabel = if (subscriptionActive) {
            formatSubscriptionType(subscriptionType)
        } else {
            "Free plan"
        }
        return "Member #$userId • $planLabel"
    }

    private fun formatSubscriptionType(subscriptionType: String): String {
        return when (subscriptionType.uppercase()) {
            "MONTHLY" -> "Monthly plan"
            "3MONTH" -> "3-month plan"
            "YEARLY" -> "Yearly plan"
            else -> "Free plan"
        }
    }

    private fun showLogoutDialog() {
        logoutDialog = AlertDialog.Builder(requireActivity(), R.style.CustomDialogTheme)
            .setTitle("Logout")
            .setMessage("Sign out of this profile and return to profile selection?")
            .setPositiveButton("Logout") { _, _ ->
                sm.clearSession()
                val intent = Intent(requireActivity(), Login_Page::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .create()

        logoutDialog?.show()
    }

    private fun showThemeDialog() {
        val themes = GlobalUtils.getAvailableThemes()
        val currentTheme = GlobalUtils.getAppTheme(requireActivity())
        val currentIndex = themes.indexOf(currentTheme)
        val themeNames = themes.map { GlobalUtils.getThemeDisplayName(it) }.toTypedArray()

        themeDialog = AlertDialog.Builder(requireActivity(), R.style.CustomDialogTheme)
            .setTitle("Select App Theme")
            .setSingleChoiceItems(themeNames, currentIndex) { dialog, which ->
                val selectedTheme = themes[which]
                val previousTheme = GlobalUtils.getAppTheme(requireActivity())

                if (selectedTheme == previousTheme) {
                    dialog.dismiss()
                    return@setSingleChoiceItems
                }

                GlobalUtils.setAppTheme(requireActivity(), selectedTheme)
                binding.themeValue.text = GlobalUtils.getThemeDisplayName(selectedTheme)
                dialog.dismiss()
                requireActivity().recreate()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        themeDialog?.setOnShowListener {
            val alertDialog = it as AlertDialog
            val listView = alertDialog.listView ?: return@setOnShowListener

            val fgValue = TypedValue()
            requireActivity().theme.resolveAttribute(R.attr.FG_color, fgValue, true)
            val fgColor = fgValue.data

            val accentValue = TypedValue()
            requireActivity().theme.resolveAttribute(R.attr.AccentColor, accentValue, true)
            val accentColor = accentValue.data

            listView.selector = ColorDrawable(accentColor)
            listView.choiceMode = ListView.CHOICE_MODE_SINGLE

            for (index in 0 until listView.childCount) {
                val child = listView.getChildAt(index)
                if (child is TextView) {
                    child.setTextColor(fgColor)
                }
            }

            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(fgColor)
        }

        themeDialog?.show()
    }

    private fun showRestartDialog() {
        restartDialog = AlertDialog.Builder(requireActivity(), R.style.CustomDialogTheme)
            .setTitle("Restart App")
            .setMessage("Are you sure you want to restart the application? This will close all current activities and restart the app.")
            .setPositiveButton("Restart") { _, _ ->
                Toast.makeText(requireActivity(), "Restarting app...", Toast.LENGTH_SHORT).show()
                GlobalUtils.restartApp(requireActivity())
            }
            .setNegativeButton("Cancel", null)
            .create()

        restartDialog?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appUpdater.dismissDialogs()
        themeDialog?.dismiss()
        restartDialog?.dismiss()
        logoutDialog?.dismiss()
        _binding = null
    }
    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val editUsername = dialogView.findViewById<android.widget.EditText>(R.id.editUsername)
        val editPin = dialogView.findViewById<android.widget.EditText>(R.id.editPin)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)

        editUsername.setText(binding.profileName.text)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val newName = editUsername.text.toString().trim()
            val newPin = editPin.text.toString().trim()

            if (newName.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Name cannot be empty", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var finalPin = newPin
            if (finalPin.isEmpty()) {
                val cursor = db.readableDatabase.rawQuery("SELECT pin FROM users WHERE id=?", arrayOf(currentUserId.toString()))
                if (cursor.moveToFirst()) {
                    finalPin = cursor.getString(0)
                }
                cursor.close()
            }

            val success = db.updateUser(currentUserId, newName, finalPin)
            if (success) {
                android.widget.Toast.makeText(requireContext(), "Profile updated", android.widget.Toast.LENGTH_SHORT).show()
                binding.profileName.text = newName
                dialog.dismiss()
            } else {
                android.widget.Toast.makeText(requireContext(), "Failed to update", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showClearHistoryDialog() {
        android.app.AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Clear Watch History")
            .setMessage("Are you sure you want to clear your watch history? This cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                val success = db.clearWatchHistory(currentUserId)
                if (success) {
                    android.widget.Toast.makeText(requireContext(), "Watch history cleared", android.widget.Toast.LENGTH_SHORT).show()
                    loadProfileData()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearFavoritesDialog() {
        android.app.AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Clear Favorites")
            .setMessage("Are you sure you want to remove all saved favorites? This cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                val success = db.clearFavorites(currentUserId)
                if (success) {
                    android.widget.Toast.makeText(requireContext(), "Favorites cleared", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_user, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val dialogTitle = dialogView.findViewById<android.widget.TextView>(R.id.dialogTitle)
        val editPin = dialogView.findViewById<android.widget.EditText>(R.id.editPin)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<android.widget.Button>(R.id.btnDelete)

        dialogTitle.text = "Delete ${binding.profileName.text}"

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnDelete.setOnClickListener {
            val enteredPin = editPin.text.toString().trim()

            val cursor = db.validateUser(binding.profileName.text.toString(), enteredPin)
            if (cursor.moveToFirst()) {
                cursor.close()
                val success = db.deleteUser(currentUserId)
                if (success) {
                    android.widget.Toast.makeText(requireContext(), "Profile deleted", android.widget.Toast.LENGTH_SHORT).show()
                    sm.clearSession()
                    val intent = android.content.Intent(requireActivity(), Login_Page::class.java)
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    requireActivity().finish()
                    dialog.dismiss()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Failed to delete", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                cursor.close()
                android.widget. Toast.makeText(requireContext(), "Incorrect PIN", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

}
