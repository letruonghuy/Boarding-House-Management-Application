package com.example.qunlphngtr.model

data class Bill(
    val id: Int = 0,
    val month: String,
    // Chỉ số
    val oldElectricReading: Int = 0,
    val newElectricReading: Int = 0,
    val oldWaterReading: Int = 0,
    val newWaterReading: Int = 0,
    // Tiền (sẽ được tính toán từ chỉ số)
    val electric: Double,
    val water: Double,
    val roomFee: Double,
    val internet: Double,
    val total: Double,
    // Thanh toán
    val paidAmount: Double = 0.0,
    val lastPaidDate: String? = null,
    // Thông tin phòng và người thuê
    val roomId: Int,
    val tenantId: Int,
    val roomName: String = "",
    val status: String = "unpaid",
    // Ảnh
    val oldElectricImageUri: String? = null,
    val newElectricImageUri: String? = null,
    val oldWaterImageUri: String? = null,
    val newWaterImageUri: String? = null,
    val paymentProofUri: String? = null // Bằng chứng thanh toán
)
