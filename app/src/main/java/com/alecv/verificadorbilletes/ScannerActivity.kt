package com.alecv.verificadorbilletes

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView

    // Variables para el modo LOTE
    private val seriesEscaneadas = mutableSetOf<String>()
    private lateinit var txtContador: TextView
    private lateinit var iconoCheck: TextView
    private lateinit var btnTerminar: Button

    // Variables para el modo TIEMPO REAL
    private lateinit var panelResultadoVivo: LinearLayout
    private lateinit var txtSerieDetectadaEnVivo: TextView // Nueva
    private lateinit var txtStatus10Vivo: TextView
    private lateinit var txtLabel10Vivo: TextView
    private lateinit var txtStatus20Vivo: TextView
    private lateinit var txtLabel20Vivo: TextView
    private lateinit var txtStatus50Vivo: TextView
    private lateinit var txtLabel50Vivo: TextView

    private var modoEscaneo = "LOTE" // Lote por defecto

    // Copiamos los rangos aquí para la validación rápida en Tiempo Real

    // Copiamos los rangos aquí para la validación rápida en Tiempo Real
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

    // Solicitud de permisos de cámara
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            iniciarCamara()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            finish() // Cierra la pantalla si no hay permiso
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. PRIMERO SIEMPRE SE CREA LA VISTA
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        // 2. LUEGO SE BUSCAN LOS ELEMENTOS
        viewFinder = findViewById(R.id.viewFinder)
        txtContador = findViewById(R.id.txtContador)
        iconoCheck = findViewById(R.id.iconoCheck)
        btnTerminar = findViewById(R.id.btnTerminar)
        panelResultadoVivo = findViewById(R.id.panelResultadoVivo)

        // Elementos nuevos de las 3 columnas
        txtSerieDetectadaEnVivo = findViewById(R.id.txtSerieDetectadaEnVivo)
        txtStatus10Vivo = findViewById(R.id.txtStatus10Vivo)
        txtLabel10Vivo = findViewById(R.id.txtLabel10Vivo)
        txtStatus20Vivo = findViewById(R.id.txtStatus20Vivo)
        txtLabel20Vivo = findViewById(R.id.txtLabel20Vivo)
        txtStatus50Vivo = findViewById(R.id.txtStatus50Vivo)
        txtLabel50Vivo = findViewById(R.id.txtLabel50Vivo)

        // Leer la orden (Intento) de MainActivity
        modoEscaneo = intent.getStringExtra("MODO") ?: "LOTE"
        configurarInterfazPorModo()

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Pedir permiso e iniciar
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        btnTerminar.setOnClickListener {
            val resultadoFinal = seriesEscaneadas.joinToString("\n")
            val intentResultado = Intent()
            intentResultado.putExtra("SERIES_ESCANEDAS", resultadoFinal)
            setResult(RESULT_OK, intentResultado)
            finish()
        }
    }

    private fun configurarInterfazPorModo() {
        if (modoEscaneo == "LOTE") {
            txtContador.visibility = View.VISIBLE
            btnTerminar.visibility = View.VISIBLE
            // iconoCheck se queda GONE hasta que detecte algo
        } else {
            panelResultadoVivo.visibility = View.VISIBLE
        }
    }

    private fun iniciarCamara() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Configurar la vista previa (Preview)
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            // Configurar el analizador de imágenes (Inteligencia Artificial)
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { imageProxy ->
                        procesarImagen(imageProxy)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e("ScannerActivity", "Error al vincular la cámara", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun procesarImagen(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Expresión Regular: Busca palabras que parezcan series (8-9 caracteres alfanuméricos terminados en B u 8)
                    val regex = Regex("\\b[A-Z0-9]{8,10}\\b")

                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val textoDetectado = line.text.replace(" ", "").uppercase()

                            if (regex.containsMatchIn(textoDetectado)) {
                                val serieExtraida = sanitizarSerie(textoDetectado)

                                // Verificamos si cumple el formato estricto tras limpiar
                                if (serieExtraida.matches(Regex("^[0-9]{8,9}B$"))) {
                                    manejarSerieDetectada(serieExtraida)
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ScannerActivity", "Error en OCR", e)
                }
                .addOnCompleteListener {
                    imageProxy.close() // ¡MUY IMPORTANTE! Libera la memoria para el siguiente fotograma
                }
        } else {
            imageProxy.close()
        }
    }

    private fun sanitizarSerie(bruta: String): String {
        var limpia = bruta.replace('O', '0').replace('I', '1').replace('L', '1').replace('S', '5')
        if ((limpia.length == 9 || limpia.length == 10) && limpia.endsWith("8")) {
            limpia = limpia.dropLast(1) + "B"
        }
        return limpia
    }

    private fun manejarSerieDetectada(serie: String) {
        if (modoEscaneo == "LOTE") {
            if (!seriesEscaneadas.contains(serie)) {
                seriesEscaneadas.add(serie)
                vibrarTelefono()

                // Actualizar interfaz (siempre en el hilo principal)
                runOnUiThread {
                    txtContador.text = "Escaneados: ${seriesEscaneadas.size}"
                    mostrarCheckTemporal()
                }
            }
        } else {
            // Modo TIEMPO REAL
            val numero = serie.dropLast(1).toLong()
            val esInhabilitado10 = rangos10.any { numero in it }
            val esInhabilitado20 = rangos20.any { numero in it }
            val esInhabilitado50 = rangos50.any { numero in it }

            // Nueva Lógica de Tiempo Real para 3 Columnas Simultáneas
            runOnUiThread {
                // 1. Mostrar el número de serie grande arriba
                txtSerieDetectadaEnVivo.text = serie

                // 2. Actualizar cada columna de forma independiente usando nuestra función de ayuda
                actualizarColumnaResultado(txtStatus10Vivo, txtLabel10Vivo, esInhabilitado10, "Bs. 10")
                actualizarColumnaResultado(txtStatus20Vivo, txtLabel20Vivo, esInhabilitado20, "Bs. 20")
                actualizarColumnaResultado(txtStatus50Vivo, txtLabel50Vivo, esInhabilitado50, "Bs. 50")

                // 3. Vibrar SOLO si al menos uno es inhabilitado (para que sirva como alerta)
                if (esInhabilitado10 || esInhabilitado20 || esInhabilitado50) {
                    vibrarTelefono()
                }
            }
        }
    }
    // Función de ayuda para limpiar y colorear cada columna según tu mockup
    private fun actualizarColumnaResultado(textViewStatus: TextView, textViewLabel: TextView, isInhabilitado: Boolean, labelBase: String) {
        if (isInhabilitado) {
            // Estilo INHABILITADO (Rojo)
            textViewStatus.text = "❌ INHABILITADO"
            textViewStatus.setTextColor(android.graphics.Color.RED)
            // Agrega las X grandes a los lados del texto principal
            textViewLabel.text = "❌ $labelBase ❌"
            textViewLabel.setTextColor(android.graphics.Color.RED)
        } else {
            // Estilo SEGURO (Verde)
            textViewStatus.text = "✅ Seguro / Válido"
            textViewStatus.setTextColor(android.graphics.Color.GREEN)
            // Agrega los checks grandes a los lados del texto principal
            textViewLabel.text = "✅ $labelBase ✅"
            textViewLabel.setTextColor(android.graphics.Color.GREEN)
        }
    }

    private fun mostrarCheckTemporal() {
        iconoCheck.visibility = View.VISIBLE
        iconoCheck.postDelayed({
            iconoCheck.visibility = View.GONE
        }, 1000) // Se oculta después de 1 segundo (1000 ms)
    }

    private fun vibrarTelefono() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}