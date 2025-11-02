package com.example.qunlphngtr.model

data class Tenant(
    val id: Int = 0,      // thêm id
    val name: String,
    val gender: String?,
    val phone: String?,
    val imageUri: String? = null
)
