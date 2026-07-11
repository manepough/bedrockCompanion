package com.example.bedrockcompanion

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout

/**
 * Draws 9 floating buttons over the screen. Tapping one simulates a real
 * touch event at the corresponding hotbar slot position -- this is
 * functionally identical to the user tapping their own hotbar, just via
 * a bigger, better-placed button. No packets are sent, no game memory
 * or process is touched, and Android requires the user to explicitly
 * grant Accessibility access before this can run at all.
 */
class QuickSlotOverlayService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showOverlay()
    }

    private fun showOverlay() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        // Approximate hotbar slot spacing -- in a real build you'd let the
        // user calibrate this once by dragging a marker over their actual
        // in-game hotbar, since HUD position varies by device/aspect ratio.
        val hotbarY = (screenHeight * 0.93).toInt()
        val slotSpacing = (screenWidth * 0.08).toInt()
        val hotbarStartX = (screenWidth * 0.15).toInt()

        for (i in 1..9) {
            val button = Button(this).apply {
                text = i.toString()
                alpha = 0.7f
                setOnClickListener {
                    val x = hotbarStartX + (i - 1) * slotSpacing
                    simulateTap(x, hotbarY)
                }
            }
            layout.addView(button)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 100
        }

        overlayView = layout
        windowManager.addView(layout, params)
    }

    // Dispatches a real, single touch-down-then-up gesture at (x, y) --
    // exactly what the Android accessibility APIs are designed for
    // (this is the same mechanism screen readers and switch-access
    // tools use to interact with apps on a user's behalf).
    private fun simulateTap(x: Int, y: Int) {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager.removeView(it) }
    }
}
