package com.example.onyx.Database


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor

class AppDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        // 1. User table
        // 1. Users Table
        db.execSQL(
            """CREATE TABLE users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE,
        gender TEXT,
        pin TEXT,
        avatar TEXT
    )"""
        )


// 2. Watchlist
        db.execSQL(
            """CREATE TABLE watchlist (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER,
        item_id TEXT,
        item_type TEXT,
        title TEXT,
        poster TEXT,
        backdrop TEXT,
        FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
    )"""
        )


// 3. Favorites Movie
        db.execSQL(
            """CREATE TABLE favorites_movies (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER,
        movie_id TEXT,
        title TEXT,
        poster TEXT,
        backdrop TEXT,
        overview TEXT,
        date TEXT,
        duration TEXT,
        rating TEXT,
        UNIQUE(user_id, movie_id),
        FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
    )"""
        )


// 4. Favorites Series
        db.execSQL(
            """CREATE TABLE favorites_series (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER,
        series_id TEXT,
        title TEXT,
        poster TEXT,
        backdrop TEXT,
        overview TEXT,
        date TEXT,
        duration TEXT,
        rating TEXT,
        UNIQUE(user_id, series_id),
        FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
    )"""
        )


// 5. Favorites Anime
        db.execSQL(
            """
                CREATE TABLE favorites_anime (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    anime_id TEXT,
                    name TEXT,
                    type TEXT,
                    anilistId TEXT,
                    malId TEXT,
                    description TEXT,
                    rating TEXT,
                    quality TEXT,
                    duration TEXT,
                    poster TEXT,
                    sub TEXT,
                    dub TEXT,
                    aired TEXT,
                    genre TEXT,
                    seasons TEXT,
                    UNIQUE(user_id, anime_id),
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )

            """.trimIndent()
        )


// 6. Downloads
        db.execSQL(
            """CREATE TABLE downloads (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER,
        item_id TEXT,
        title TEXT,
        file_path TEXT,
        size INTEGER,
        progress INTEGER,
        status TEXT,
        FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
    )"""
        )


// 7. Continue Watching
        db.execSQL(
            """CREATE TABLE continue_watching (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER,
        item_id TEXT,
        title TEXT,
        poster TEXT,
        last_position INTEGER,
        duration INTEGER,
        updated_at INTEGER,
        UNIQUE(user_id, item_id),
        FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
    )"""
        )

    // 8. App Settings (General App Info – NOT linked to users)
    db.execSQL(
        """CREATE TABLE app_settings (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            is_paid INTEGER DEFAULT 0,
            subscription_type TEXT,
            subscription_expiry INTEGER,
            payment_reference TEXT,
            license_key TEXT
        )"""
    )

    db.execSQL(
        """INSERT INTO app_settings (is_paid, subscription_type, subscription_expiry, payment_reference, license_key)
           VALUES (0, 'NONE', 0, '', '')"""
    )

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // If you upgrade schema later
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS watchlist")
        db.execSQL("DROP TABLE IF EXISTS favorites_movies")
        db.execSQL("DROP TABLE IF EXISTS favorites_series")
        db.execSQL("DROP TABLE IF EXISTS favorites_anime")
        db.execSQL("DROP TABLE IF EXISTS downloads")
        db.execSQL("DROP TABLE IF EXISTS continue_watching")
        db.execSQL("DROP TABLE IF EXISTS app_settings")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "app_data.db"
        private const val DATABASE_VERSION = 1
    }
    // ========== USERS TABLE OPERATIONS ==========

    fun addUser(username: String, gender: String, pin: String, avatar: String = ""): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("gender", gender)
            put("pin", pin)
            put("avatar", avatar)
        }

        // Insert and get the row ID
        val userId = db.insert("users", null, values)

        // userId = the AUTO_INCREMENT primary key (id)
        return userId   // returns -1 if insert failed
    }

    fun getUsers(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM users", null)
    }

    fun deleteUser(id: Int): Boolean {
        val db = writableDatabase
        return db.delete("users", "id=?", arrayOf(id.toString())) > 0
    }

    fun updateUserAvatar(id: Int, avatar: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply { put("avatar", avatar) }
        return db.update("users", cv, "id=?", arrayOf(id.toString())) > 0
    }

    fun validateUser(username: String, pin: String): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT * FROM users WHERE username=? AND pin=?",
            arrayOf(username, pin)
        )
    }

    //////////////////////////////// FAVORITES ANIME FUNCTIONS ///////////////////////////////////////

    fun addFavoriteAnime(
        userId: Int,
        animeId: String,
        name: String,
        type: String,
        anilistId: String,
        malId: String,
        description: String,
        rating: String,
        quality: String,
        duration: String,
        poster: String,
        sub: String,
        dub: String,
        aired: String,
        genre: String,
        seasons: String
    ): Boolean {
        val db = writableDatabase
        val cv = ContentValues()

        cv.put("user_id", userId)
        cv.put("anime_id", animeId)
        cv.put("name", name)
        cv.put("type", type)
        cv.put("anilistId", anilistId)
        cv.put("malId", malId)
        cv.put("description", description)
        cv.put("rating", rating)
        cv.put("quality", quality)
        cv.put("duration", duration)
        cv.put("poster", poster)
        cv.put("sub", sub)
        cv.put("dub", dub)
        cv.put("aired", aired)
        cv.put("genre", genre)
        cv.put("seasons", seasons)

        return try {
            db.insertOrThrow("favorites_anime", null, cv) > 0
        } catch (e: Exception) {
            false
        }
    }

    fun removeFavoriteAnime(userId: Int, animeId: String): Boolean {
        val db = writableDatabase
        return db.delete(
            "favorites_anime", "user_id=? AND anime_id=?",
            arrayOf(userId.toString(), animeId)
        ) > 0
    }

    fun isFavoriteAnime(userId: Int, animeId: String): Boolean {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT 1 FROM favorites_anime WHERE user_id=? AND anime_id=? LIMIT 1",
            arrayOf(userId.toString(), animeId)
        )

        val exists = cursor.moveToFirst()
        cursor.close()

        return exists
    }

    fun getFavoriteAnime(userId: Int): List<FavoriteAnimeModel> {
        val db = readableDatabase
        val list = mutableListOf<FavoriteAnimeModel>()

        val cursor = db.rawQuery(
            "SELECT * FROM favorites_anime WHERE user_id=?",
            arrayOf(userId.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val seasonsString = cursor.getString(cursor.getColumnIndexOrThrow("seasons"))
                val seasonsArray = seasonsString.split(",")  // Convert back to list

                list.add(
                    FavoriteAnimeModel(
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                        animeId = cursor.getString(cursor.getColumnIndexOrThrow("anime_id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        anilistId = cursor.getString(cursor.getColumnIndexOrThrow("anilistId")),
                        malId = cursor.getString(cursor.getColumnIndexOrThrow("malId")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        rating = cursor.getString(cursor.getColumnIndexOrThrow("rating")),
                        quality = cursor.getString(cursor.getColumnIndexOrThrow("quality")),
                        duration = cursor.getString(cursor.getColumnIndexOrThrow("duration")),
                        poster = cursor.getString(cursor.getColumnIndexOrThrow("poster")),
                        sub = cursor.getString(cursor.getColumnIndexOrThrow("sub")),
                        dub = cursor.getString(cursor.getColumnIndexOrThrow("dub")),
                        aired = cursor.getString(cursor.getColumnIndexOrThrow("aired")),
                        genre = cursor.getString(cursor.getColumnIndexOrThrow("genre")),
                        seasons = seasonsArray      // <-- ARRAY restored
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    data class FavoriteAnimeModel(
        val userId: Int,
        val animeId: String,
        val name: String,
        val type: String,
        val anilistId: String,
        val malId: String,
        val description: String,
        val rating: String,
        val quality: String,
        val duration: String,
        val poster: String,
        val sub: String,
        val dub: String,
        val aired: String,
        val genre: String,
        val seasons: List<String> // SEASONS as ARRAY
    )

    ////////////////////////////////////// WATCHLIST FUNCTIONS //////////////////////////////////////

    fun resetDatabase() {
        val db = writableDatabase

        // Drop all tables
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS watchlist")
        db.execSQL("DROP TABLE IF EXISTS favorites_movies")
        db.execSQL("DROP TABLE IF EXISTS favorites_series")
        db.execSQL("DROP TABLE IF EXISTS favorites_anime")
        db.execSQL("DROP TABLE IF EXISTS downloads")
        db.execSQL("DROP TABLE IF EXISTS continue_watching")
        db.execSQL("DROP TABLE IF EXISTS app_settings")

        // Recreate database schema
        onCreate(db)
    }



}
