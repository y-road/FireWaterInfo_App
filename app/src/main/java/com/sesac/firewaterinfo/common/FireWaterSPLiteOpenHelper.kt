package com.sesac.firewaterinfo.common

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

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
            it.execSQL(CREATE_LATLNG_TABLE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.let {
            it.execSQL("DROP TABLE IF EXISTS $TABLE_LATLNG")
        }
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun insetLoadingFireWater(values: ContentValues, tableName: String): Int {
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




}





















