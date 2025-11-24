package com.moondark.realfoodapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderNumber: String,         // "ORD-20251122-001"
    val items: String,               // JSON: '{"Pizza":2,"Coca":1}'
    val totalAmount: Double,         // 25.50
    val deliveryAddress: String,     // "Calle Lenina, 123"
    val deliveryLatitude: Double,    // 56.0153
    val deliveryLongitude: Double,   // 92.8932
    val status: String,              // "pending", "delivered", etc.
    val createdAt: Long = System.currentTimeMillis()
)
