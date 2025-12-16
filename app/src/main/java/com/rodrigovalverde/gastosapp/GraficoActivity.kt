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

        val altoBarra = 60f // Un poco más delgadas para que se vean elegantes
        val espacioEntreBarras = 50f
        val margenIzquierdo = 350f // Aumentamos espacio para nombres largos a la izquierda
        val margenSuperior = 100f

        val maxValor = montos.maxOfOrNull { abs(it) }?.toFloat() ?: 1f

        // --- CORRECCIÓN AQUÍ ---
        // Antes restábamos 100f, era muy poco. Ahora restamos 400f.
        // Esto asegura que la barra más larga termine mucho antes del borde derecho,
        // dejando espacio suficiente para escribir
        val anchoDisponible = anchoTotal - margenIzquierdo - 400f

        val paintTextoEtiqueta = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 45f // Texto un poco más pequeño para que quepa mejor
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }

        val paintTextoMonto = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 45f
            textAlign = android.graphics.Paint.Align.LEFT
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD // Negrita para el monto
        }

        montos.forEachIndexed { index, monto ->
            val valorAbsoluto = abs(monto).toFloat()

            // Calculamos el ancho proporcional
            val anchoBarra = (valorAbsoluto / maxValor) * anchoDisponible

            val posicionY = margenSuperior + index * (altoBarra + espacioEntreBarras)

            val colorBarra = if (monto >= 0) Color(0xFF4CAF50) else Color(0xFFE53935)

            // 1. DIBUJAR ETIQUETA (Izquierda)
            // Cortamos el texto si es muy largo para que no se superponga
            val etiqueta = descripciones.getOrElse(index) { "" }
            val etiquetaCortada = if (etiqueta.length > 15) etiqueta.take(15) + "..." else etiqueta

            drawContext.canvas.nativeCanvas.drawText(
                etiquetaCortada,
                margenIzquierdo - 20f,
                posicionY + altoBarra / 1.5f,
                paintTextoEtiqueta
            )

            // 2. DIBUJAR BARRA
            // Aseguramos que tenga al menos 5px de ancho para que se vea algo si el valor es muy bajo
            val anchoVisual = if (anchoBarra < 5f) 5f else anchoBarra

            drawRect(
                color = colorBarra,
                topLeft = Offset(x = margenIzquierdo, y = posicionY),
                size = Size(width = anchoVisual, height = altoBarra)
            )

            // 3. DIBUJAR MONTO (Derecha)
            // Ahora sí hay espacio porque 'anchoVisual' nunca llegará al borde de la pantalla
            drawContext.canvas.nativeCanvas.drawText(
                "S/ ${String.format("%.2f", valorAbsoluto)}",
                margenIzquierdo + anchoVisual + 20f,
                posicionY + altoBarra / 1.5f,
                paintTextoMonto
            )
        }
    }
}