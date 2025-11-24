package com.moondark.realfoodapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,              // "Pizza Margarita"
    val description: String,        // "Tomate, mozzarella y albahaca"
    val price: Double,              // 8.50
    val category: String,           // "Pizza", "Burgers", "Drinks"
    val imageUrl: String = "",      // URL de la imagen (opcional)
    val available: Boolean = true   // Si está disponible o no
)
