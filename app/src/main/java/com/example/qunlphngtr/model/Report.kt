package com.example.qunlphngtr.model

data class Report(
    val id: Int,
    val title: String,
    val content: String?,
    val date: String?,
    val status: String?,
    val tenant_id: Int?
)

