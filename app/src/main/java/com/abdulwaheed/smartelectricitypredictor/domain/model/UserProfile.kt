package com.abdulwaheed.smartelectricitypredictor.domain.model

data class UserProfile(
    val uid: String,
    val fullName: String,
    val email: String,
    val age: Int,
    val gender: String,
    val cellNumber: String,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
