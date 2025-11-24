package com.moondark.realfoodapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey  // ← CAMBIO: Ahora productId es la clave primaria
    val productId: Int,
    val productName: String,
    val productPrice: Double,
    val quantity: Int,
    val addedAt: Long = System.currentTimeMillis()
)
