package com.moondark.realfoodapp.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    // LEER todos los pedidos (más recientes primero)
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    // LEER un pedido específico por ID
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Int): OrderEntity?

    // LEER pedidos por estado (pending, delivering, delivered)
    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>>

    // CREAR un nuevo pedido (retorna el ID generado)
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    // ACTUALIZAR un pedido completo
    @Update
    suspend fun updateOrder(order: OrderEntity)

    // CAMBIAR solo el estado de un pedido
    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String)

    // ELIMINAR un pedido
    @Delete
    suspend fun deleteOrder(order: OrderEntity)
}
