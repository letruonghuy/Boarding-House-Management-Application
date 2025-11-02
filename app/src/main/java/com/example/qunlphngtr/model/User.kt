package com.example.qunlphngtr.model

data class User(
    val id: Int,
    val username: String,
    val password: String,
    val role: String // Sẽ là "landlord" hoặc "tenant"
)