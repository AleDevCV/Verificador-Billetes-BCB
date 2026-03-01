package com.alecv.verificadorbilletes

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultadosActivity : AppCompatActivity() {

    // Copia aquí nuevamente tus tres listas de rangos (rangos10, rangos20, rangos50)
    // (Asegúrate de poner las listas completas como las tienes en el MainActivity)
    private val rangos10 = listOf(
        77100001L..77550000L,
        78000001L..78450000L,
        78900001L..96350000L,
        96350001L..96800000L,
        96800001L..97250000L,
        98150001L..98600000L,
        104900001L..105350000L,
        105350001L..105800000L,
        106700001L..107150000L,
        107600001L..108050000L,
        108050001L..108500000L,
        109400001L..109850000L
    )
    private val rangos20 = listOf(
        87280145L..91646549L,
        96650001L..97100000L,
        99800001L..100250000L,
        100250001L..100700000L,
        109250001L..109700000L,
        110600001L..111050000L,
        111050001L..111500000L,
        111950001L..112400000L,
        112400001L..112850000L,
        112850001L..113300000L,
        114200001L..114650000L,
        114650001L..115100000L,
        115100001L..115550000L,
        118700001L..119150000L,
        119150001L..119600000L,
        120500001L..120950000L
    )
    private val rangos50 = listOf(
        67250001L..67700000L,
        69050001L..69500000L,
        69500001L..69950000L,
        69950001L..70400000L,
        70400001L..70850000L,
        70850001L..71300000L,
        76310012L..85139995L,
        86400001L..86850000L,
        90900001L..91350000L,
        91800001L..92250000L
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultados)

        val tablaResultados = findViewById<TableLayout>(R.id.tablaResultados)

        // Recibir los datos enviados desde MainActivity
        val textoRecibido = intent.getStringExtra("SERIES_A_VERIFICAR") ?: ""
        val seriesBrutas = textoRecibido.split(Regex("[\\s,]+")).filter { it.isNotEmpty() }

        for (serieBruta in seriesBrutas) {
            // Autocorrección
            var serieLimpia = serieBruta.uppercase().replace('O', '0').replace('I', '1').replace('L', '1').replace('S', '5')
            if ((serieLimpia.length == 9 || serieLimpia.length == 10) && serieLimpia.endsWith("8")) {
                serieLimpia = serieLimpia.dropLast(1) + "B"
            }

            // Crear una nueva fila para la tabla
            val fila = TableRow(this)
            val marginParams = TableLayout.LayoutParams()
            marginParams.setMargins(0, 1, 0, 1) // Pequeño margen para simular el borde de Excel
            fila.layoutParams = marginParams

            // Si el formato es inválido, mostramos una advertencia en toda la fila
            if (!serieLimpia.matches(Regex("^[0-9]{8,9}B$"))) {
                fila.addView(crearCelda(serieLimpia, colorFondo = "#FFE699"))
                val celdaError = crearCelda("⚠️ Formato Inválido", colorFondo = "#EEEEEE", colorTexto = "#FF0000")
                val params = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                params.span = 3 // Ocupa las 3 columnas
                celdaError.layoutParams = params
                fila.addView(celdaError)
            } else {
                // Validación matemática real
                val numero = serieLimpia.dropLast(1).toLong()
                val inhabilitado10 = rangos10.any { numero in it }
                val inhabilitado20 = rangos20.any { numero in it }
                val inhabilitado50 = rangos50.any { numero in it }

                // Llenar las 4 celdas imitando tu Excel
                fila.addView(crearCelda(serieLimpia, colorFondo = "#FFE699", negrita = true))
                fila.addView(crearCeldaValidacion(inhabilitado10))
                fila.addView(crearCeldaValidacion(inhabilitado20))
                fila.addView(crearCeldaValidacion(inhabilitado50))
            }

            tablaResultados.addView(fila)
        }
    }

    // Función auxiliar para construir las celdas de texto normales
    private fun crearCelda(texto: String, colorFondo: String, colorTexto: String = "#000000", negrita: Boolean = false): TextView {
        val tv = TextView(this)
        tv.text = texto
        tv.setPadding(16, 16, 16, 16)
        tv.gravity = Gravity.CENTER
        tv.setBackgroundColor(Color.parseColor(colorFondo))
        tv.setTextColor(Color.parseColor(colorTexto))
        if (negrita) tv.setTypeface(null, Typeface.BOLD)

        // Parámetros para que ocupe el espacio correcto y deje borde
        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT)
        params.setMargins(1, 1, 1, 1)
        tv.layoutParams = params
        return tv
    }

    // Función auxiliar para construir las celdas de "SEGURO" o "INHABILITADO"
    private fun crearCeldaValidacion(isInhabilitado: Boolean): TextView {
        if (isInhabilitado) {
            return crearCelda("❌ INHABILITADO", colorFondo = "#EA9999", colorTexto = "#CC0000", negrita = true) // Rojo
        } else {
            return crearCelda("✅ SEGURO ✅", colorFondo = "#B6D7A8", colorTexto = "#38761D", negrita = true) // Verde
        }
    }
}