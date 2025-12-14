package com.rodrigovalverde.gastosapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    fun obtenerGastos(): Flow<List<Gasto>>

    @Query("SELECT SUM(monto) FROM gastos")
    fun obtenerTotalGastos(): Flow<Double?>

    @Insert
    suspend fun insertar(gasto: Gasto)

    @Delete
    suspend fun eliminar(gasto: Gasto)
}