package com.example.onyx.Database

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.onyx.Database.AppDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import java.io.FileOutputStream
import com.google.android.gms.common.api.Scope


class GoogleDriveSyncManager(private val context: Context) {

    private val DATABASE_NAME = "app_data.db"
    private val BACKUP_FILE_NAME = "app_data_backup.db"

    private var driveService: Drive? = null




    fun isAccountLinked(): GoogleSignInAccount? { // 1. Check if user is signed in
        return GoogleSignIn.getLastSignedInAccount(context)
    }


    fun signInIfNeeded(launcher: ActivityResultLauncher<Intent>): GoogleSignInAccount? {

        // Check existing signed-in account
        val existingAccount = GoogleSignIn.getLastSignedInAccount(context)

        if (existingAccount != null) {

            Log.d("DriveSync", "Already signed in")

            // Initialize Drive immediately
            initDrive(existingAccount)

            return existingAccount
        }


        // Not signed in → request sign-in
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

        val client = GoogleSignIn.getClient(context, options)

        launcher.launch(client.signInIntent)

        return null
    }

    fun signOut() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

        val client = GoogleSignIn.getClient(context, options)
        client.signOut().addOnCompleteListener {
            Log.d("DriveSync", "Signed out from Google")
        }
    }





    //////////////////////////////////////////////////////
    // INIT
    //////////////////////////////////////////////////////

    fun initDrive(account: GoogleSignInAccount): Boolean {

        return try {

            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(DriveScopes.DRIVE_APPDATA)
            )

            credential.selectedAccount = account.account

            driveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential
            )
                .setApplicationName("Onyx Streaming")
                .build()

            true

        } catch (e: Exception) {

            Log.e("DriveSync", "Init failed", e)

            false
        }
    }


    //////////////////////////////////////////////////////
    // BACKUP DATABASE
    //////////////////////////////////////////////////////

    fun backup(): Boolean {

        try {

            val drive = driveService ?: return false


            val dbFile = context.getDatabasePath(DATABASE_NAME)

            if (!dbFile.exists()) {

                Log.e("DriveSync", "Database not found")
                return false
            }


            val fileId = getBackupFileId()


            val metadata = File().apply {

                name = BACKUP_FILE_NAME
                parents = listOf("appDataFolder")
            }


            val media = FileContent(
                "application/octet-stream",
                dbFile
            )


            if (fileId == null) {

                drive.files()
                    .create(metadata, media)
                    .execute()

                Log.d("DriveSync", "Backup CREATED")

            } else {

                drive.files()
                    .update(fileId, metadata, media)
                    .execute()

                Log.d("DriveSync", "Backup UPDATED")
            }


            return true

        } catch (e: Exception) {

            Log.e("DriveSync", "Backup failed", e)

            return false
        }

    }


    //////////////////////////////////////////////////////
    // RESTORE DATABASE
    //////////////////////////////////////////////////////

    fun restore(): Boolean {

        try {

            val drive = driveService ?: return false


            val fileId = getBackupFileId()
                ?: return false


            closeDatabase()


            val dbFile = context.getDatabasePath(DATABASE_NAME)


            val output = FileOutputStream(dbFile)


            drive.files()
                .get(fileId)
                .executeMediaAndDownloadTo(output)


            output.flush()
            output.close()


            Log.d("DriveSync", "Restore SUCCESS")


            return true


        } catch (e: Exception) {

            Log.e("DriveSync", "Restore failed", e)

            return false
        }

    }



    //////////////////////////////////////////////////////
    // CHECK IF BACKUP EXISTS
    //////////////////////////////////////////////////////

    fun backupExists(): Boolean {

        return getBackupFileId() != null
    }



    //////////////////////////////////////////////////////
    // GET BACKUP FILE ID
    //////////////////////////////////////////////////////

    private fun getBackupFileId(): String? {

        try {

            val drive = driveService ?: return null


            val result = drive.files()
                .list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .execute()


            result.files.forEach {

                if (it.name == BACKUP_FILE_NAME)
                    return it.id
            }


        } catch (e: Exception) {

            Log.e("DriveSync", "Get file failed", e)
        }


        return null
    }



    //////////////////////////////////////////////////////
    // CLOSE DATABASE BEFORE RESTORE
    //////////////////////////////////////////////////////

    private fun closeDatabase() {

        try {

            val dbHelper = AppDatabase(context)

            dbHelper.close()

        } catch (e: Exception) {

            Log.e("DriveSync", "Close DB failed", e)
        }

    }



    //////////////////////////////////////////////////////
    // AUTO SYNC
    //////////////////////////////////////////////////////

    fun autoSync(): Boolean {

        return try {

            if (backupExists()) {

                restore()

            } else {

                backup()

            }

        } catch (e: Exception) {

            false
        }

    }


}