package com.moondark.realfoodapp.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items ORDER BY addedAt DESC")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT SUM(productPrice * quantity) FROM cart_items")
    fun getTotalAmount(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM cart_items")
    fun getCartItemCount(): Flow<Int>

    @Query("SELECT SUM(quantity) FROM cart_items")
    fun getTotalItemsQuantity(): Flow<Int?>

    // UPSERT: Inserta o actualiza si ya existe
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCartItem(item: CartItemEntity)

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getCartItemByProductId(productId: Int): CartItemEntity?

    @Delete
    suspend fun removeFromCart(item: CartItemEntity)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun removeByProductId(productId: Int)
}
