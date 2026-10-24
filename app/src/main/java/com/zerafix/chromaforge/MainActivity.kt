package com.zerafix.chromaforge

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val red = Color.rgb(255, 45, 65)
    private val dark = Color.rgb(13, 13, 16)
    private val panel = Color.rgb(27, 27, 33)
    private val projectionRequest = 711
    private lateinit var root: LinearLayout
    private lateinit var saturationValue: TextView
    private lateinit var contrastValue: TextView
    private var saturation = 100
    private var contrast = 100
    private var selectedStyle = "VÍVIDO"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = dark
        window.navigationBarColor = dark
        window.decorView.systemUiVisibility = 0
        buildUi()
    }

    private fun buildUi() {
        val scroll = ScrollView(this).apply { setBackgroundColor(dark) }
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
            setBackgroundColor(dark)
        }
        scroll.addView(root)
        setContentView(scroll)

        label("CHROMA", 14, red, true)
        label("FORGE", 32, Color.WHITE, true)
        label("FILTROS VISUALES • BETA", 12, Color.LTGRAY, false)
        space(22)
        cardTitle("PERFIL GRÁFICO")
        val styles = listOf("ORIGINAL", "VÍVIDO", "CINEMÁTICO", "FRÍO", "CÁLIDO", "OSCURO")
        styles.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEach { style ->
                val button = Button(this).apply {
                    text = style
                    isAllCaps = false
                    setTextColor(if (style == selectedStyle) Color.WHITE else Color.LTGRAY)
                    setBackgroundTintList(android.content.res.ColorStateList.valueOf(if (style == selectedStyle) red else panel))
                    setOnClickListener {
                        selectedStyle = style
                        applyPreviewStyle()
                        Toast.makeText(this@MainActivity, "Perfil $style seleccionado", Toast.LENGTH_SHORT).show()
                    }
                }
                row.addView(button, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                    setMargins(dp(3), dp(3), dp(3), dp(3))
                })
            }
            root.addView(row)
        }
        space(18)
        cardTitle("SATURACIÓN")
        saturationValue = label("100%", 18, Color.WHITE, true)
        slider(0, 200, 100) {
            saturation = it
            saturationValue.text = "$it%"
        }
        cardTitle("CONTRASTE")
        contrastValue = label("100%", 18, Color.WHITE, true)
        slider(50, 150, 100) {
            contrast = it
            contrastValue.text = "$it%"
        }
        space(18)
        cardTitle("JUEGOS")
        label("La biblioteca de juegos y la aplicación de perfiles están en desarrollo.", 14, Color.LTGRAY, false)
        val addGame = actionButton("＋  AGREGAR JUEGO") {
            Toast.makeText(this, "La selección de juegos instalados aún no está implementada.", Toast.LENGTH_LONG).show()
        }
        root.addView(addGame, matchWrap())
        space(10)
        cardTitle("CAPTURA DE PANTALLA")
        label("Solicita permiso del sistema para capturar la pantalla. Esta versión aún no procesa el vídeo en tiempo real ni aplica filtros sobre otros juegos.", 13, Color.LTGRAY, false)
        space(8)
        root.addView(actionButton("SOLICITAR PERMISO DE CAPTURA") { requestCapturePermission() }, matchWrap())
        space(12)
        root.addView(actionButton("RESTABLECER AJUSTES") {
            saturation = 100
            contrast = 100
            saturationValue.text = "100%"
            contrastValue.text = "100%"
            selectedStyle = "ORIGINAL"
            Toast.makeText(this, "Ajustes restablecidos", Toast.LENGTH_SHORT).show()
        }, matchWrap())
        space(22)
        label("ChromaForge 0.1 • Sin modificar archivos de juegos", 11, Color.GRAY, false)
    }

    private fun applyPreviewStyle() {
        val tint = when (selectedStyle) {
            "VÍVIDO" -> Color.rgb(255, 45, 65)
            "CINEMÁTICO" -> Color.rgb(160, 90, 190)
            "FRÍO" -> Color.rgb(70, 150, 255)
            "CÁLIDO" -> Color.rgb(255, 160, 55)
            "OSCURO" -> Color.rgb(90, 90, 100)
            else -> red
        }
        root.findViewsWithText(ArrayList<View>(), selectedStyle, View.FIND_VIEWS_WITH_TEXT)
        Toast.makeText(this, "Estilo $selectedStyle: vista de configuración; filtro de juego aún no activo.", Toast.LENGTH_SHORT).show()
    }

    private fun requestCapturePermission() {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(manager.createScreenCaptureIntent(), projectionRequest)
    }

    @Deprecated("Deprecated by Android, retained for compatibility with Android 10+")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == projectionRequest && resultCode == RESULT_OK && data != null) {
            Toast.makeText(this, "Permiso concedido. El procesamiento de captura todavía no está implementado.", Toast.LENGTH_LONG).show()
        } else if (requestCode == projectionRequest) {
            Toast.makeText(this, "Permiso de captura cancelado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun label(text: String, size: Int, color: Int, bold: Boolean): TextView {
        val view = TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(4))
        }
        root.addView(view, matchWrap())
        return view
    }

    private fun cardTitle(text: String) {
        val view = TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(red)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(panel)
        }
        root.addView(view, matchWrap())
        space(7)
    }

    private fun slider(min: Int, max: Int, initial: Int, changed: (Int) -> Unit) {
        val seek = SeekBar(this).apply {
            this.max = max - min
            progress = initial - min
            progressTintList = android.content.res.ColorStateList.valueOf(red)
            thumbTintList = android.content.res.ColorStateList.valueOf(red)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    changed(progress + min)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        root.addView(seek, matchWrap())
        space(10)
    }

    private fun actionButton(text: String, click: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(red))
            setOnClickListener { click() }
        }

    private fun space(height: Int) {
        root.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
