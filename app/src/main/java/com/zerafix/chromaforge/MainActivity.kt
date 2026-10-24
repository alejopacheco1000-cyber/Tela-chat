package com.zerafix.chromaforge

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
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
        listOf("ORIGINAL", "VÍVIDO", "CINEMÁTICO", "FRÍO", "CÁLIDO", "OSCURO").chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEach { style ->
                row.addView(actionButton(style) {
                    selectedStyle = style
                    sendFilterUpdate()
                    Toast.makeText(this, "Perfil $style seleccionado", Toast.LENGTH_SHORT).show()
                    buildUiRefreshStyle()
                }, LinearLayout.LayoutParams(0, dp(48), 1f).apply { setMargins(dp(3), dp(3), dp(3), dp(3)) })
            }
            root.addView(row)
        }
        space(18)
        cardTitle("SATURACIÓN")
        saturationValue = label("100%", 18, Color.WHITE, true)
        slider(0, 200, 100) { saturation = it; saturationValue.text = "$it%"; sendFilterUpdate() }
        cardTitle("CONTRASTE")
        contrastValue = label("100%", 18, Color.WHITE, true)
        slider(50, 150, 100) { contrast = it; contrastValue.text = "$it%"; sendFilterUpdate() }

        space(18)
        cardTitle("FILTRO SOBRE OTROS JUEGOS")
        label("Activa una capa de color sobre otras aplicaciones. Android te pedirá permiso para mostrarla encima de los juegos. Los efectos son tintes aproximados; no alteran los píxeles reales ni garantizan cambios de saturación/contraste.", 14, Color.LTGRAY, false)
        space(8)
        root.addView(actionButton("ACTIVAR FILTRO EN JUEGO") { activateOverlay() }, matchWrap())
        root.addView(actionButton("DESACTIVAR FILTRO") {
            stopService(Intent(this, FilterOverlayService::class.java))
            Toast.makeText(this, "Filtro desactivado", Toast.LENGTH_SHORT).show()
        }, matchWrap())
        space(12)
        cardTitle("JUEGOS")
        label("Abre el juego normalmente después de activar la capa. La biblioteca de juegos aún no está implementada.", 14, Color.LTGRAY, false)
        root.addView(actionButton("RESTABLECER AJUSTES") {
            saturation = 100; contrast = 100; selectedStyle = "ORIGINAL"
            saturationValue.text = "100%"; contrastValue.text = "100%"
            sendFilterUpdate()
            Toast.makeText(this, "Ajustes restablecidos", Toast.LENGTH_SHORT).show()
        }, matchWrap())
        space(22)
        label("ChromaForge 0.2 • Capa visual experimental", 11, Color.GRAY, false)
    }

    private fun buildUiRefreshStyle() {
        // Keep the selected profile in the service; no activity recreation is needed.
    }

    private fun activateOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            Toast.makeText(this, "Activa 'Mostrar encima' y vuelve a ChromaForge para pulsar ACTIVAR.", Toast.LENGTH_LONG).show()
            return
        }
        sendFilterUpdate()
        startService(Intent(this, FilterOverlayService::class.java).apply {
            action = FilterOverlayService.ACTION_START
            putExtra(FilterOverlayService.EXTRA_STYLE, selectedStyle)
            putExtra(FilterOverlayService.EXTRA_SATURATION, saturation)
            putExtra(FilterOverlayService.EXTRA_CONTRAST, contrast)
        })
        Toast.makeText(this, "Capa visual activada. Puedes volver al juego.", Toast.LENGTH_LONG).show()
    }

    private fun sendFilterUpdate() {
        if (!Settings.canDrawOverlays(this)) return
        startService(Intent(this, FilterOverlayService::class.java).apply {
            action = FilterOverlayService.ACTION_UPDATE
            putExtra(FilterOverlayService.EXTRA_STYLE, selectedStyle)
            putExtra(FilterOverlayService.EXTRA_SATURATION, saturation)
            putExtra(FilterOverlayService.EXTRA_CONTRAST, contrast)
        })
    }

    private fun label(text: String, size: Int, color: Int, bold: Boolean): TextView {
        val view = TextView(this).apply {
            this.text = text; textSize = size.toFloat(); setTextColor(color)
            if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(4))
        }
        root.addView(view, matchWrap())
        return view
    }

    private fun cardTitle(text: String) {
        root.addView(TextView(this).apply {
            this.text = text; textSize = 13f; setTextColor(red)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(dp(12), dp(10), dp(12), dp(10)); setBackgroundColor(panel)
        }, matchWrap())
        space(7)
    }

    private fun slider(min: Int, max: Int, initial: Int, changed: (Int) -> Unit) {
        root.addView(SeekBar(this).apply {
            this.max = max - min; progress = initial - min
            progressTintList = android.content.res.ColorStateList.valueOf(red)
            thumbTintList = android.content.res.ColorStateList.valueOf(red)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { changed(progress + min) }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }, matchWrap())
        space(10)
    }

    private fun actionButton(text: String, click: () -> Unit): Button =
        Button(this).apply {
            this.text = text; isAllCaps = false; setTextColor(Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(if (text == selectedStyle) red else red))
            setOnClickListener { click() }
        }

    private fun space(height: Int) { root.addView(View(this), LinearLayout.LayoutParams(1, dp(height))) }
    private fun matchWrap() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
