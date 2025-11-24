package com.moondark.realfoodapp.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    // LEER el perfil del usuario (siempre ID = 1)
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    // GUARDAR o ACTUALIZAR el perfil
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    // ACTUALIZAR solo las coordenadas de entrega
    @Query("UPDATE user_profile SET deliveryLatitude = :lat, deliveryLongitude = :lon WHERE id = 1")
    suspend fun updateDeliveryLocation(lat: Double, lon: Double)

    // ELIMINAR el perfil (por ejemplo, cerrar sesión)
    @Query("DELETE FROM user_profile")
    suspend fun deleteProfile()
}
