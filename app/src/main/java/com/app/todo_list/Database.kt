package com.app.todo_list

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "app_database.db"
        private const val DATABASE_VERSION = 2

        private const val NOTES_TABLE_NAME = "notes"
        private const val NOTES_COLUMN_ID = "id"
        private const val NOTES_COLUMN_TITLE = "title"
        private const val NOTES_COLUMN_CONTENT = "content"

        private const val TASKS_TABLE_NAME = "tasks"
        private const val TASKS_COLUMN_ID = "id"
        private const val TASKS_COLUMN_START_DATE = "start_date"
        private const val TASKS_COLUMN_END_DATE = "end_date"
        private const val TASKS_COLUMN_TITLE = "title"
        private const val TASKS_COLUMN_IS_CHECKED = "is_checked"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createNotesTable = ("CREATE TABLE $NOTES_TABLE_NAME ("
                + "$NOTES_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$NOTES_COLUMN_TITLE TEXT,"
                + "$NOTES_COLUMN_CONTENT TEXT)")
        db.execSQL(createNotesTable)

        val createTasksTable = ("CREATE TABLE $TASKS_TABLE_NAME ("
                + "$TASKS_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$TASKS_COLUMN_START_DATE TEXT,"
                + "$TASKS_COLUMN_END_DATE TEXT,"
                + "$TASKS_COLUMN_TITLE TEXT,"
                + "$TASKS_COLUMN_IS_CHECKED INTEGER DEFAULT 0)")
        db.execSQL(createTasksTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TASKS_TABLE_NAME ADD COLUMN $TASKS_COLUMN_IS_CHECKED INTEGER DEFAULT 0")
        }
    }

    fun addNote(title: String, content: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(NOTES_COLUMN_TITLE, title)
            put(NOTES_COLUMN_CONTENT, content)
        }
        return db.insert(NOTES_TABLE_NAME, null, contentValues)
    }

    fun getAllNotes(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $NOTES_TABLE_NAME", null)
    }

    fun updateNote(id: Int, title: String, content: String): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(NOTES_COLUMN_TITLE, title)
            put(NOTES_COLUMN_CONTENT, content)
        }
        return db.update(NOTES_TABLE_NAME, contentValues, "$NOTES_COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteNote(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(NOTES_TABLE_NAME, "$NOTES_COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun addTask(startDate: String, endDate: String, title: String, isChecked: Boolean): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(TASKS_COLUMN_START_DATE, startDate)
            put(TASKS_COLUMN_END_DATE, endDate)
            put(TASKS_COLUMN_TITLE, title)
            put(TASKS_COLUMN_IS_CHECKED, if (isChecked) 1 else 0)
        }
        return db.insert(TASKS_TABLE_NAME, null, contentValues)
    }

    fun getAllTasks(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TASKS_TABLE_NAME", null)
    }

    fun updateTask(id: Int, startDate: String, endDate: String, title: String, isChecked: Boolean): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(TASKS_COLUMN_START_DATE, startDate)
            put(TASKS_COLUMN_END_DATE, endDate)
            put(TASKS_COLUMN_TITLE, title)
            put(TASKS_COLUMN_IS_CHECKED, if (isChecked) 1 else 0)
        }
        return db.update(TASKS_TABLE_NAME, contentValues, "$TASKS_COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteTask(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(TASKS_TABLE_NAME, "$TASKS_COLUMN_ID = ?", arrayOf(id.toString()))
    }
}
