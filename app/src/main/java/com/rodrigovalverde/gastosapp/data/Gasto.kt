package com.rodrigovalverde.gastosapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "gastos")
data class Gasto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    val fecha: Date
)