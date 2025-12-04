package com.example.onyx.Database

import android.content.Context


class SessionManger(context: Context) {
    private val pref = context.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)

    fun saveUserId(userId: Int) {
        pref.edit().putInt("LOGGED_USER_ID", userId).apply()
    }

    fun getUserId(): Int {
        return pref.getInt("LOGGED_USER_ID", -1) // -1 means no user logged in
    }

    fun saveAvatar(avatar: String) {
        pref.edit().putString("LOGGED_AVATAR", avatar).apply()
    }

    fun getUserAvatar(): String? {
        return pref.getString("LOGGED_AVATAR", "-") // -1 means no user logged in
    }

    fun clearSession() {
        pref.edit().clear().apply()
    }
}