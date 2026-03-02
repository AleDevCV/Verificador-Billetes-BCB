package com.alecv.verificadorbilletes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTiempoReal = findViewById<Button>(R.id.btnTiempoReal)
        val inputSerie = findViewById<EditText>(R.id.inputSerie)
        val btnBuscar = findViewById<Button>(R.id.btnBuscar)
        val btnRangos = findViewById<Button>(R.id.btnRangos)
        val btnGithub = findViewById<ImageButton>(R.id.btnGithub)

        // 1. Botón de Cámara (Tiempo Real)
        btnTiempoReal.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            intent.putExtra("MODO", "TIEMPO_REAL")
            startActivity(intent)
        }

        // 2. Botón de Búsqueda Manual
        btnBuscar.setOnClickListener {
            val textoIngresado = inputSerie.text.toString().trim()

            // Validar que no esté vacío y tenga la longitud correcta (8 o 9 dígitos)
            if (textoIngresado.length < 8 || textoIngresado.length > 9) {
                Toast.makeText(this, "Por favor, ingresa 8 o 9 dígitos válidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Abrimos la pantalla de resultados y LE AGREGAMOS LA "B" AUTOMÁTICAMENTE
            val intent = Intent(this, ResultadosActivity::class.java)
            intent.putExtra("SERIES_A_VERIFICAR", "${textoIngresado}B")
            startActivity(intent)

            // Limpiamos la caja de texto para que esté vacía si el usuario regresa
            inputSerie.text.clear()
        }

        // 3. Botón de Rangos Oficiales
        btnRangos.setOnClickListener {
            val intent = Intent(this, InfoRangosActivity::class.java)
            startActivity(intent)
        }

        // 4. Botón de GitHub
        btnGithub.setOnClickListener {
            val url = "https://github.com/AlejandroCV2014"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}