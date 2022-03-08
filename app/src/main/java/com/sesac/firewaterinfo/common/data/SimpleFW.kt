package com.sesac.firewaterinfo.common.data

data class SimpleFW(
    var number: Int,
    var name: String,
    var latitude: Double,
    var longitude: Double,
    var available: Boolean
)

data class AllFW(
    var number: Int,
    var name: String,
    var type: String,
    var province: String,
    var city: String,
    var address: String,
    var latitude: Double,
    var longitude: Double,
    var detail: String,
    var center: String,
    var protect_case: Boolean,
    var available: Boolean,
    var installation_year: Int,
    var pipe_depth: Double,
    var out_pressure: Double,
    var pipe_diameter: Int,
    var fire_station: String,
    var phone: String,
    var update_date: String,
    var manager: Long
)
