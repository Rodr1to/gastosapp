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

    val listaGastos: StateFlow<List<Gasto>> = dao.obtenerGastos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalGastos: StateFlow<Double> = dao.obtenerTotalGastos()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    fun agregarGasto(descripcion: String, montoString: String) {
        val monto = montoString.toDoubleOrNull()
        if (descripcion.isNotBlank() && monto != null) {
            viewModelScope.launch {
                val nuevoGasto = Gasto(
                    descripcion = descripcion,
                    monto = monto,
                    fecha = Date()
                )
                dao.insertar(nuevoGasto)
            }
        }
    }

    fun eliminarGasto(gasto: Gasto) {
        viewModelScope.launch {
            dao.eliminar(gasto)
        }
    }
}