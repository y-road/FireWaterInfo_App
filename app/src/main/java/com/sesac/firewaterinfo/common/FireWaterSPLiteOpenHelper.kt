package com.sesac.firewaterinfo.common

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.sesac.firewaterinfo.MY_DEBUG_TAG
import com.sesac.firewaterinfo.common.data.*

class FireWaterSPLiteOpenHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, CURRENT_DB_VERSION) {
    companion object {
        private lateinit var dbInstance: FireWaterSPLiteOpenHelper

        fun getDBInstance(context: Context): FireWaterSPLiteOpenHelper {
            if (!this::dbInstance.isInitialized) {
                dbInstance = FireWaterSPLiteOpenHelper(context)
            }
            return dbInstance
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.let {
            it.execSQL(CREATE_EDIT_HISTORY_TABLE)
//            it.execSQL(CREATE_MY_FAVORITE_TABLE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.let {
            it.execSQL("DROP TABLE IF EXISTS $TABLE_EDIT_HISTORY")
//            it.execSQL("DROP TABLE IF EXISTS $TABLE_MY_FAVORITE")
        }
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun insertEditedHistory(values: ContentValues, tableName: String): Int {
        val dbConnection = writableDatabase
        dbConnection.beginTransaction()

        var resultPK = -1
        try {
            resultPK = dbConnection.insert(tableName, null, values).toInt()
            dbConnection.setTransactionSuccessful()
        } catch (sql: SQLiteException) {
            Log.e("SQL_ERROR", "$sql")
        } finally {
            with(dbConnection) {
                endTransaction()
                close()
            }
        }
        return resultPK
    }

    fun selectAllEditedHistory(): MutableList<EditedHistory> {
        val dbConnection = readableDatabase
        var histroy = mutableListOf<EditedHistory>()

        try {
            val sqlStr = "SELECT * FROM $TABLE_EDIT_HISTORY"

            val cursor = dbConnection.rawQuery(sqlStr, null)
            histroy = navigate(cursor)
        } catch (sql: SQLiteException) {
            Log.e(MY_DEBUG_TAG, "selectAllEditedHistory() -> $sql")
        } finally {
            dbConnection.close()
        }
        return histroy
    }

    fun navigate(cursor: Cursor): MutableList<EditedHistory> {
        val items = mutableListOf<EditedHistory>()

        cursor.use {
            while (!it.isClosed && it.moveToNext()) {
                var columnIndex = 0
                with(EditedHistory()) {
                    eHPK = it.getInt(columnIndex++)
                    fwName = it.getString(columnIndex++)
                    editedType = it.getString(columnIndex++)
                    editedDate = it.getString(columnIndex++)

                    items.add(this)
                }
            }
        }
        return items
    }

//    fun insertMyFavorite(values: ContentValues, tableName: String): Int {
//        val dbConnection = writableDatabase
//        dbConnection.beginTransaction()
//
//        var resultPK = -1
//        try {
//            resultPK = dbConnection.insert(tableName, null, values).toInt()
//            dbConnection.setTransactionSuccessful()
//        } catch (sql: SQLiteException) {
//            Log.e("SQL_ERROR", "$sql")
//        } finally {
//            with(dbConnection) {
//                endTransaction()
//                close()
//            }
//        }
//        return resultPK
//    }
//
//    fun selectAllMyFavorite(): MutableList<MyFavorite> {
//        val dbConnection = readableDatabase
//        var myFavorite = mutableListOf<MyFavorite>()
//        try {
//            val sqlStr = "SELECT * FROM $TABLE_MY_FAVORITE"
//
//            val cursor = dbConnection.rawQuery(sqlStr, null)
//            myFavorite = navigate2(cursor)
//        } catch (sql: SQLiteException) {
//            Log.e(MY_DEBUG_TAG, "selectAllMyFavorite() -> $sql")
//        } finally {
//            dbConnection.close()
//        }
//        return myFavorite
//    }
//
//    fun navigate2(cursor: Cursor): MutableList<MyFavorite> {
//        val items = mutableListOf<MyFavorite>()
//
//        cursor.use {
//            while (!it.isClosed && it.moveToNext()) {
//                var columnIndex = 0
//                with(MyFavorite()) {
//                    mFPK = it.getInt(columnIndex++)
//                    mFSerial = it.getInt(columnIndex++)
//
//                    items.add(this)
//                }
//            }
//        }
//        return items
//    }
}





















