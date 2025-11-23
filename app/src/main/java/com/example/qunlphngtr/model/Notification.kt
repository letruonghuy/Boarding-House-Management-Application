package com.example.qunlphngtr.model

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val type: String?,
    val date: String?,
    val isRead: Boolean,
    val tenantId: Int?
)