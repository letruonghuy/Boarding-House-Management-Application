package com.example.qunlphngtr.model
data class Bill(
    val id: Int = 0,
    val month: String,
    val electric: Double,
    val water: Double,
    val roomFee: Double,
    val internet: Double,
    val total: Double,
    val roomId: Int,
    val tenantId: Int,
    val roomName: String = "",
    val status: String = "unpaid"
)