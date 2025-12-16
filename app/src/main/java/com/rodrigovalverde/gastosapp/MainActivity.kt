package com.rodrigovalverde.gastosapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodrigovalverde.gastosapp.data.Gasto
import com.rodrigovalverde.gastosapp.viewmodel.GastosViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GastosScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastosScreen(viewModel: GastosViewModel = viewModel()) {
    val context = LocalContext.current

    // Observamos datos del ViewModel
    val lista by viewModel.listaMovimientos.collectAsState()
    val totalIngresos by viewModel.totalIngresos.collectAsState()
    val totalGastos by viewModel.totalGastos.collectAsState() // Será negativo
    val saldoFinal by viewModel.saldoFinal.collectAsState()

    var descripcion by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var esIngreso by remember { mutableStateOf(false) } // Switch: false=Gasto, true=Ingreso

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finanzas Personales") },
                actions = {
                    // Botón para ir al Gráfico (Punto 4)
                    IconButton(onClick = {
                        val intent = Intent(context, GraficoActivity::class.java)
                        // Enviamos los datos al gráfico (Tomamos los últimos 10 para que quepan)
                        val datosGrafico = lista.take(10)
                        intent.putStringArrayListExtra("descripciones", ArrayList(datosGrafico.map { it.descripcion }))
                        intent.putExtra("montos", datosGrafico.map { it.monto }.toDoubleArray())
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Ver Gráfico")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {

            // 1. TARJETA DE RESUMEN (Ingresos, Gastos, Saldo)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoCaja("Ingresos", totalIngresos, Color(0xFF2E7D32)) // Verde
                    InfoCaja("Gastos", totalGastos, Color(0xFFC62828))     // Rojo
                    InfoCaja("Saldo", saldoFinal, Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. FORMULARIO DE REGISTRO
            Card(elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (esIngreso) "Registrar INGRESO" else "Registrar GASTO",
                            fontWeight = FontWeight.Bold,
                            color = if (esIngreso) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                        Switch(checked = esIngreso, onCheckedChange = { esIngreso = it })
                    }

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = monto,
                        onValueChange = { monto = it },
                        label = { Text("Monto") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.registrarMovimiento(descripcion, monto, esIngreso)
                            descripcion = ""
                            monto = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (esIngreso) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    ) {
                        Text(if (esIngreso) "Guardar Ingreso" else "Guardar Gasto")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Últimos Movimientos:", style = MaterialTheme.typography.titleMedium)

            // 3. LISTA DE MOVIMIENTOS
            LazyColumn {
                items(lista) { item ->
                    ItemMovimiento(item, onDelete = { viewModel.eliminarMovimiento(item) })
                }
            }
        }
    }
}

@Composable
fun InfoCaja(titulo: String, valor: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(titulo, fontSize = 12.sp)
        Text(
            text = "S/ %.2f".format(abs(valor)), // Valor absoluto para mostrar
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ItemMovimiento(gasto: Gasto, onDelete: () -> Unit) {
    // Lógica visual: Rojo si es negativo (Gasto), Negro si es positivo (Ingreso)
    val esGasto = gasto.monto < 0
    val colorTexto = if (esGasto) Color(0xFFC62828) else Color.Black
    val signo = if (esGasto) "-" else "+"

    Card(
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // Descripción en Rojo o Negro según sea Gasto o Ingreso
                Text(gasto.descripcion, fontWeight = FontWeight.Bold, color = colorTexto)

                val fechaFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(gasto.fecha)
                Text(fechaFormat, fontSize = 12.sp, color = Color.Gray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$signo S/ %.2f".format(abs(gasto.monto)),
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
                }
            }
        }
    }
}