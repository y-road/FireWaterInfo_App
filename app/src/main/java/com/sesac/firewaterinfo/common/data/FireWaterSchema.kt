package com.sesac.firewaterinfo.common.data

const val DB_NAME = "fire_water_schema.db" // 방수개시 앱의 메인 스키마
const val CURRENT_DB_VERSION = 1

// 수정 이력 테이블------------------------------------------------------------
const val TABLE_EDIT_HISTORY = "edit_history_table" // 수정 이력 테이블

const val CREATE_EDIT_HISTORY_TABLE = // 수정 이력 테이블 생성
    """CREATE TABLE $TABLE_EDIT_HISTORY (
        _id INTEGER PRIMARY KEY AUTOINCREMENT, 
        fw_name TEXT, 
        edited_type TEXT, 
        edited_Date TEXT);"""

data class EditedHistory(  // 수정 이력 테이블
    var eHPK: Int = 0, // 프라이머리 키
    var fwName: String = "", // 소화전 이름
    var editedType: String = "", // 수정된 타입 : text, photo
    var editedDate: String = "", // 수정된 날짜 : 0000-00-00
)
// --------------------------------------------------------------------------

//// 나의 즐겨찾기 테이블--------------------------------------------------------
//const val TABLE_MY_FAVORITE = "my_favorite_table" // 나의 즐겨찾기 테이블
//
//const val CREATE_MY_FAVORITE_TABLE =
//    """CREATE TABLE $TABLE_MY_FAVORITE (
//        _id INTEGER PRIMARY KEY AUTOINCREMENT,
//        fw_serial INTEGER
//    """
//
//data class MyFavorite(  // 나의 즐겨찾기 테이블
//    var mFPK: Int = 0, // 프라이머리 키
//    var mFSerial: Int = 0, // 시리얼 넘버
////    var mFName: String = "", // 소화전 이름
////    var mFAddress: String = "", // 소화전 주소
////    var mFDetail: String = "", // 소화전 설명
////    var mFDate: String = "", // 수정된 날짜: 0000-00-00
////    var mFAvailable: Boolean = true // 사용 가능 여부
//)
//// --------------------------------------------------------------------------
