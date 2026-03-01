package com.example.inventorytracker

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                $COL_USERNAME TEXT PRIMARY KEY,
                $COL_PASSWORD TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_INVENTORY (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ITEM_NAME TEXT NOT NULL,
                $COL_QTY INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INVENTORY")
        onCreate(db)
    }

    // ---------- Users ----------
    fun userExists(username: String): Boolean {
        val db = readableDatabase
        db.rawQuery(
            "SELECT $COL_USERNAME FROM $TABLE_USERS WHERE $COL_USERNAME = ?",
            arrayOf(username)
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    fun createUser(username: String, password: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_USERNAME, username)
            put(COL_PASSWORD, password)
        }
        return db.insert(TABLE_USERS, null, cv) != -1L
    }

    fun validateLogin(username: String, password: String): Boolean {
        val db = readableDatabase
        db.rawQuery(
            "SELECT $COL_USERNAME FROM $TABLE_USERS WHERE $COL_USERNAME = ? AND $COL_PASSWORD = ?",
            arrayOf(username, password)
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    // ---------- Inventory CRUD ----------
    fun addItem(name: String, qty: Int): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_ITEM_NAME, name)
            put(COL_QTY, qty)
        }
        return db.insert(TABLE_INVENTORY, null, cv)
    }

    fun getAllItems(): List<InventoryItem> {
        val items = mutableListOf<InventoryItem>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT $COL_ID, $COL_ITEM_NAME, $COL_QTY FROM $TABLE_INVENTORY ORDER BY $COL_ITEM_NAME ASC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    InventoryItem(
                        id = it.getLong(0),
                        name = it.getString(1),
                        qty = it.getInt(2)
                    )
                )
            }
        }
        return items
    }

    fun updateQty(id: Long, newQty: Int): Int {
        val db = writableDatabase
        val cv = ContentValues().apply { put(COL_QTY, newQty) }
        return db.update(TABLE_INVENTORY, cv, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun deleteItem(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_INVENTORY, "$COL_ID = ?", arrayOf(id.toString()))
    }

    companion object {
        private const val DB_NAME = "inventorytracker.db"
        private const val DB_VERSION = 1

        private const val TABLE_USERS = "users"
        private const val TABLE_INVENTORY = "inventory"

        private const val COL_USERNAME = "username"
        private const val COL_PASSWORD = "password"

        private const val COL_ID = "_id"
        private const val COL_ITEM_NAME = "name"
        private const val COL_QTY = "qty"
    }
}