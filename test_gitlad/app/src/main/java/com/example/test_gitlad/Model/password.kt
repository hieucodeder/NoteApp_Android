package com.example.test_gitlad.Model

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.example.test_gitlad.DataBase.DatabaseHelper

class PasswordDAO(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insert(password: String): Long {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_PASSWORD, password)
        }

        return db.insert(DatabaseHelper.TABLE_PASSWORD, null, values)
    }

    fun getPassword(): String? {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(DatabaseHelper.COLUMN_PASSWORD)
        var password: String? = null

        db.query(
            DatabaseHelper.TABLE_PASSWORD,
            projection,
            null,
            null,
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                password =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD))
            }
        }

        return password
    }
    data class password(
        var password: String // Thêm thuộc tính này cho ảnh
    )
    @SuppressLint("Range")
    fun getPasswordFromDatabase(context: Context): String? {
        val dbHelper = DatabaseHelper(context)
        val db: SQLiteDatabase
        db = try {
            dbHelper.readableDatabase
        } catch (e: SQLiteException) {
            dbHelper.writableDatabase
        }

        val projection = arrayOf(DatabaseHelper.COLUMN_PASSWORD)
        var password: String? = null

        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_PASSWORD,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            password = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD))
        }

        cursor.close()
        db.close()

        return password
    }
}