package com.alecv.verificadorbilletes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Lanzador para recibir los datos del ScannerActivity en Modo Lote
    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val seriesEscaneadas = result.data?.getStringExtra("SERIES_ESCANEDAS")
            if (!seriesEscaneadas.isNullOrEmpty()) {
                val inputSerie = findViewById<EditText>(R.id.inputSerie)
                val textoActual = inputSerie.text.toString()

                // Si la caja ya tenía texto escrito, añade los nuevos abajo. Si no, los pone directo.
                if (textoActual.isBlank()) {
                    inputSerie.setText(seriesEscaneadas)
                } else {
                    inputSerie.setText("$textoActual\n$seriesEscaneadas")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputSerie = findViewById<EditText>(R.id.inputSerie)
        val btnBuscar = findViewById<Button>(R.id.btnBuscar)
        val btnCamara = findViewById<Button>(R.id.btnCamara)
        val btnTiempoReal = findViewById<Button>(R.id.btnTiempoReal)

        val btnRangos = findViewById<Button>(R.id.btnRangos)
        val btnGithub = findViewById<android.widget.ImageButton>(R.id.btnGithub)

        // Acción para abrir tu perfil de GitHub
        btnGithub.setOnClickListener {
            // ¡OJO! Cambia la URL por el enlace real de tu perfil
            val url = "https://github.com/AleDevCV"
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse(url)
            startActivity(intent)
        }

        // Acción para abrir la pantalla de las tablas del BCB
        btnRangos.setOnClickListener {
            val intent = android.content.Intent(this, InfoRangosActivity::class.java)
            startActivity(intent)
        }

        // Si aprietan "Abrir Cámara" (Lote)
        btnCamara.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            intent.putExtra("MODO", "LOTE")
            scannerLauncher.launch(intent)
        }

        // Si aprietan "Verificador en Tiempo Real"
        btnTiempoReal.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            intent.putExtra("MODO", "TIEMPO_REAL")
            startActivity(intent)
        }

        // 3. Qué hacer cuando se presiona el botón "Buscar"
        btnBuscar.setOnClickListener {
            val textoIngresado = inputSerie.text.toString()

            if (textoIngresado.isBlank()) {
                Toast.makeText(this, "Por favor, ingresa una serie", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Abrimos la nueva pantalla y le enviamos el texto
            val intent = Intent(this, ResultadosActivity::class.java)
            intent.putExtra("SERIES_A_VERIFICAR", textoIngresado)
            startActivity(intent)
        }
    }
}