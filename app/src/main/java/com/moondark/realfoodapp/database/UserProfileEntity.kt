package com.moondark.realfoodapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,  // Siempre ID = 1 (solo un perfil por usuario)
    val name: String,
    val email: String,
    val phone: String,
    val birthday: String,
    val deliveryAddress: String,
    val deliveryCity: String,
    val deliveryLatitude: Double,
    val deliveryLongitude: Double
)
