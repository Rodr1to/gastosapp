package com.rodrigovalverde.gastosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.abs

class GraficoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recibimos los datos enviados desde el MainActivity
        val descripciones = intent.getStringArrayListExtra("descripciones") ?: arrayListOf()
        val montos = intent.getDoubleArrayExtra("montos") ?: doubleArrayOf()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    GraficoBarrasHorizontal(descripciones, montos)
                }
            }
        }
    }
}

@Composable
fun GraficoBarrasHorizontal(descripciones: ArrayList<String>, montos: DoubleArray) {
    Canvas(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        // Dimensiones del Canvas
        val anchoTotal = size.width
        val altoTotal = size.height

        // Configuración de las barras
        val altoBarra = 80f
        val espacioEntreBarras = 40f
        val margenIzquierdo = 300f // Espacio para las etiquetas (nombres)
        val margenSuperior = 100f

        // Buscamos el valor absoluto máximo para calcular la escala
        // (Usamos abs() porque los gastos son negativos)
        val maxValor = montos.maxOfOrNull { abs(it) }?.toFloat() ?: 1f

        // Ancho máximo disponible para dibujar
        val anchoDisponible = anchoTotal - margenIzquierdo - 100f

        // Pinturas para texto (Android nativo)
        val paintTextoEtiqueta = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 40f
            textAlign = android.graphics.Paint.Align.RIGHT
        }

        val paintTextoMonto = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 40f
            textAlign = android.graphics.Paint.Align.LEFT
        }

        // Dibujamos una barra por cada dato
        montos.forEachIndexed { index, monto ->
            val valorAbsoluto = abs(monto).toFloat()

            // Calculamos el ancho de esta barra según la escala
            val anchoBarra = (valorAbsoluto / maxValor) * anchoDisponible

            val posicionY = margenSuperior + index * (altoBarra + espacioEntreBarras)

            // Color: Verde si es Ingreso (>=0), Rojo si es Gasto (<0)
            val colorBarra = if (monto >= 0) Color(0xFF4CAF50) else Color(0xFFE53935)

            // 1. DIBUJAR ETIQUETA (A la izquierda)
            drawContext.canvas.nativeCanvas.drawText(
                descripciones.getOrElse(index) { "" },
                margenIzquierdo - 20f, // Un poco antes de la barra
                posicionY + altoBarra / 1.5f, // Centrado verticalmente
                paintTextoEtiqueta
            )

            // 2. DIBUJAR BARRA (Rectángulo Horizontal)
            drawRect(
                color = colorBarra,
                topLeft = Offset(x = margenIzquierdo, y = posicionY),
                size = Size(width = anchoBarra, height = altoBarra)
            )

            // 3. DIBUJAR MONTO (A la derecha de la barra)
            drawContext.canvas.nativeCanvas.drawText(
                "S/ ${String.format("%.2f", valorAbsoluto)}",
                margenIzquierdo + anchoBarra + 20f,
                posicionY + altoBarra / 1.5f,
                paintTextoMonto
            )
        }
    }
}