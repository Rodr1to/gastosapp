package com.rodrigovalverde.gastosapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigovalverde.gastosapp.data.AppDatabase
import com.rodrigovalverde.gastosapp.data.Gasto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class GastosViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).gastoDao()

    val listaMovimientos: StateFlow<List<Gasto>> = dao.obtenerGastos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val saldoFinal: StateFlow<Double> = dao.obtenerSaldoTotal()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalIngresos: StateFlow<Double> = dao.obtenerTotalIngresos()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalGastos: StateFlow<Double> = dao.obtenerTotalGastos()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Función modificada para recibir si es ingreso o gasto
    fun registrarMovimiento(descripcion: String, montoString: String, esIngreso: Boolean) {
        val monto = montoString.toDoubleOrNull()
        if (descripcion.isNotBlank() && monto != null && monto > 0) {
            viewModelScope.launch {
                // Si es ingreso se guarda positivo, si es gasto negativo
                val montoFinal = if (esIngreso) monto else -monto

                val nuevoMovimiento = Gasto(
                    descripcion = descripcion,
                    monto = montoFinal,
                    fecha = Date()
                )
                dao.insertar(nuevoMovimiento)
            }
        }
    }

    fun eliminarMovimiento(gasto: Gasto) {
        viewModelScope.launch {
            dao.eliminar(gasto)
        }
    }
}