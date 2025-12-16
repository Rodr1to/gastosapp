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
        val anchoTotal = size.width
        val altoTotal = size.height

        val altoBarra = 60f
        val espacioEntreBarras = 50f

        val margenIzquierdo = anchoTotal * 0.25f

        val espacioParaMonto = anchoTotal * 0.20f

        val margenSuperior = 100f

        val maxValor = montos.maxOfOrNull { abs(it) }?.toFloat() ?: 1f

        // ancho disponible para la barra es: Total - Izquierda - EspacioDerecho
        val anchoDisponible = anchoTotal - margenIzquierdo - espacioParaMonto

        val paintTextoEtiqueta = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 35f
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }

        val paintTextoMonto = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 35f
            textAlign = android.graphics.Paint.Align.LEFT
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        montos.forEachIndexed { index, monto ->
            val valorAbsoluto = abs(monto).toFloat()

            // ancho proporcional
            val anchoBarra = (valorAbsoluto / maxValor) * anchoDisponible

            val posicionY = margenSuperior + index * (altoBarra + espacioEntreBarras)

            val colorBarra = if (monto >= 0) Color(0xFF4CAF50) else Color(0xFFE53935)

            val etiqueta = descripciones.getOrElse(index) { "" }
            val etiquetaCortada = if (etiqueta.length > 15) etiqueta.take(15) + "..." else etiqueta

            drawContext.canvas.nativeCanvas.drawText(
                etiquetaCortada,
                margenIzquierdo - 20f,
                posicionY + altoBarra / 1.5f,
                paintTextoEtiqueta
            )

            val anchoVisual = if (anchoBarra < 5f) 5f else anchoBarra

            drawRect(
                color = colorBarra,
                topLeft = Offset(x = margenIzquierdo, y = posicionY),
                size = Size(width = anchoVisual, height = altoBarra)
            )

            drawContext.canvas.nativeCanvas.drawText(
                "S/ ${String.format("%.2f", valorAbsoluto)}",
                margenIzquierdo + anchoVisual + 20f,
                posicionY + altoBarra / 1.5f,
                paintTextoMonto
            )
        }
    }
}