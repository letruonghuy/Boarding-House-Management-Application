package com.example.qunlphngtr.model

data class Room(
    val id: Int = 0,
    val name: String,
    val price: Double,
    val area: Double,
    var status: String,
    val description: String,
    val imageUri: String? = null,
    var tenantId: Int? = null // Thêm trường tenantId có thể null
)
