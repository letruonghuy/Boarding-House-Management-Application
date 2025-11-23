package com.example.qunlphngtr.model

data class Contract(
    val id: Int,
    val tenantId: Int,
    val roomId: Int,
    val startDate: String,
    val endDate: String,
    val rentPrice: Double,
    val depositAmount: Double,
    val contractPdfUri: String? // Đường dẫn đến file PDF, có thể là null
)
