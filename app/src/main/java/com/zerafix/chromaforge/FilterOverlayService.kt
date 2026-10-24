package com.zerafix.chromaforge

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class FilterOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlay: TextView? = null
    private var style = "VÍVIDO"
    private var saturation = 100
    private var contrast = 100

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                style = intent.getStringExtra(EXTRA_STYLE) ?: style
                saturation = intent.getIntExtra(EXTRA_SATURATION, saturation)
                contrast = intent.getIntExtra(EXTRA_CONTRAST, contrast)
                showOverlay()
            }
            ACTION_UPDATE -> {
                style = intent.getStringExtra(EXTRA_STYLE) ?: style
                saturation = intent.getIntExtra(EXTRA_SATURATION, saturation)
                contrast = intent.getIntExtra(EXTRA_CONTRAST, contrast)
                updateOverlay()
            }
        }
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        if (overlay == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            overlay = TextView(this).apply {
                text = " "
                setBackgroundColor(Color.TRANSPARENT)
            }
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP or Gravity.START }
            try { windowManager?.addView(overlay, params) } catch (_: Exception) { stopSelf(); return }
        }
        updateOverlay()
    }

    private fun updateOverlay() {
        val view = overlay ?: return
        val base = when (style) {
            "VÍVIDO" -> Color.rgb(255, 35, 65)
            "CINEMÁTICO" -> Color.rgb(110, 60, 145)
            "FRÍO" -> Color.rgb(40, 120, 255)
            "CÁLIDO" -> Color.rgb(255, 130, 35)
            "OSCURO" -> Color.BLACK
            else -> Color.TRANSPARENT
        }
        // A screen-wide translucent tint is an approximation, not a per-pixel saturation/contrast filter.
        val styleAlpha = when (style) {
            "ORIGINAL" -> 0
            "OSCURO" -> 72
            "VÍVIDO" -> 18
            "CINEMÁTICO" -> 22
            "FRÍO", "CÁLIDO" -> 24
            else -> 0
        }
        val contrastAlpha = when {
            contrast < 90 -> 10
            contrast > 115 -> 18
            else -> 0
        }
        val satAlpha = if (saturation < 70) 8 else if (saturation > 140) 6 else 0
        val alpha = (styleAlpha + contrastAlpha + satAlpha).coerceIn(0, 110)
        view.setBackgroundColor(Color.argb(alpha, Color.red(base), Color.green(base), Color.blue(base)))
        view.invalidate()
    }

    override fun onDestroy() {
        try { if (overlay != null) windowManager?.removeView(overlay) } catch (_: Exception) {}
        overlay = null
        windowManager = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.zerafix.chromaforge.START_FILTER"
        const val ACTION_UPDATE = "com.zerafix.chromaforge.UPDATE_FILTER"
        const val EXTRA_STYLE = "style"
        const val EXTRA_SATURATION = "saturation"
        const val EXTRA_CONTRAST = "contrast"
    }
}
