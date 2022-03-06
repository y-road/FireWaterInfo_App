package com.sesac.firewaterinfo.common

const val DB_NAME = "fire_water_schema.db"
const val CURRENT_DB_VERSION = 1

const val TABLE_LATLNG = "latlng_tbl"

const val CREATE_LATLNG_TABLE = """CREATE TABLE $TABLE_LATLNG (
    |-id INTGER PRIMARY KEY AUTOINCREMENT,
    |fw_name TEXT,
    |fw_lati DOUBLE,
    |fw_long DOUBLE
    |);"""

data class FireWaterSimpleInfo(
    var fwPK: Int,
    var fwName: String,
    var fwLati: Double,
    var fwLong: Double
)