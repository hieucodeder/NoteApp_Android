package com.example.test_gitlad.DataBase

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "notes.db"
        private const val DATABASE_VERSION = 2 // Incremented version to trigger upgrade

        // Table and columns
        const val TABLE_NAME = "notes"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_DATE = "date"
        const val COLUMN_IMAGE_URI = "image_Uri"
        const val COLUMN_ALARM_TIME = "alarm_time"
        const val COLUMN_COLOR = "color"
        const val COLUMN_PINNED = "pinned"
        const val COLUMN_AUDIO_PATH = "audio_path" // New column for audio path
        const val COLUMN_DRAWING = "drawing"

        // Table and columns for password
        const val TABLE_PASSWORD = "password_table"
        const val COLUMN_PASSWORD = "password"

        // SQL queries to create tables
        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$COLUMN_TITLE TEXT," +
                    "$COLUMN_CONTENT TEXT," +
                    "$COLUMN_DATE TEXT," +
                    "$COLUMN_IMAGE_URI TEXT," +
                    "$COLUMN_ALARM_TIME INTEGER," +
                    "$COLUMN_COLOR INTEGER," +
                    "$COLUMN_PINNED INTEGER DEFAULT 0," +
                    "$COLUMN_AUDIO_PATH TEXT," +
                    "$COLUMN_DRAWING TEXT)"

        private const val SQL_CREATE_ENTRIESPW =
            "CREATE TABLE $TABLE_PASSWORD (" + "$COLUMN_PASSWORD TEXT)"

        // SQL queries to delete tables
        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"

        private const val SQL_DELETE_ENTRIESPW = "DROP TABLE IF EXISTS $TABLE_PASSWORD"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        db.execSQL(SQL_CREATE_ENTRIESPW)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(SQL_DELETE_ENTRIES)
            db.execSQL(SQL_CREATE_ENTRIES)
        }
        db.execSQL(SQL_DELETE_ENTRIESPW)
        db.execSQL(SQL_CREATE_ENTRIESPW)
        onCreate(db)
    }
}
