package com.example.yeohangjissokssok.models

class PlaceData (
    val id: Long,
    val name: String,
    val region: String,
    val address: String?,
    val photoUrl: String,
    val pos: Double,
    val totalNum: Int,
    val keywordText: String = "없음",
    val keywordNum: Int = -1
)