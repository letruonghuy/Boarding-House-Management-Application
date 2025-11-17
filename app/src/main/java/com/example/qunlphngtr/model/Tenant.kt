package com.example.qunlphngtr.model

// Data class này PHẢI khớp với các cột trong DatabaseHelper
data class Tenant(
    val id: Int,
    val name: String,
    val gender: String?,
    val phone: String?,
    val imageUri: String?,
    val identity_number: String?,
    val room_id: Int?,
    val start_date: String?,
    val end_date: String?,
    val user_id: Int?
)