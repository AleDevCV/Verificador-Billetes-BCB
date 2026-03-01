package com.alecv.verificadorbilletes

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InfoRangosActivity : AppCompatActivity() {

    // Las bases de datos oficiales
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
        setContentView(R.layout.activity_info_rangos)

        val tabla10 = findViewById<TableLayout>(R.id.tabla10)
        val tabla20 = findViewById<TableLayout>(R.id.tabla20)
        val tabla50 = findViewById<TableLayout>(R.id.tabla50)

        // Dibujar las tablas dinámicamente con los colores pastel de tu Excel
        llenarTabla(tabla10, rangos10, "#E0F2F1") // Fondo Celeste claro
        llenarTabla(tabla20, rangos20, "#FFF3E0") // Fondo Naranja claro
        llenarTabla(tabla50, rangos50, "#EDE7F6") // Fondo Morado claro
    }

    private fun llenarTabla(tabla: TableLayout, rangos: List<LongRange>, colorFondo: String) {
        // Crear la fila de Cabecera (Desde - Hasta)
        val filaCabecera = TableRow(this)
        filaCabecera.addView(crearCelda("Desde", colorFondo, true))
        filaCabecera.addView(crearCelda("Hasta", colorFondo, true))
        tabla.addView(filaCabecera)

        // Rellenar con los datos exactos del BCB
        for (rango in rangos) {
            val fila = TableRow(this)
            fila.addView(crearCelda(rango.first.toString(), colorFondo, false))
            fila.addView(crearCelda(rango.last.toString(), colorFondo, false))
            tabla.addView(fila)
        }
    }

    private fun crearCelda(texto: String, color: String, esCabecera: Boolean): TextView {
        val tv = TextView(this)
        tv.text = texto
        tv.setPadding(16, 24, 16, 24) // Celdas altas y espaciosas
        tv.gravity = Gravity.CENTER
        tv.setTextColor(Color.BLACK)
        tv.setBackgroundColor(Color.parseColor(color))
        tv.textSize = 16f

        if (esCabecera) {
            tv.setTypeface(null, Typeface.BOLD)
        }

        // Simular el borde de la tabla (1 pixel de margen negro)
        val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT)
        params.setMargins(1, 1, 1, 1)
        tv.layoutParams = params

        return tv
    }
}