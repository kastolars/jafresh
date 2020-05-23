package com.kastolars.expirationreminderproject.ocrreader.ui

import android.graphics.*
import com.kastolars.expirationreminderproject.ocrreader.ui.camera.GraphicOverlay

class CaptureBox(overlay: GraphicOverlay<*>, private val box: Rect?) :
    GraphicOverlay.Graphic(overlay) {

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private var rectPaint: Paint? = null
    }

    init {
        if (rectPaint == null) {
            rectPaint = Paint()
            rectPaint!!.color = TEXT_COLOR
            rectPaint!!.style = Paint.Style.STROKE
            rectPaint!!.strokeWidth = 4.0f
        }
        postInvalidate()
    }

    override fun draw(canvas: Canvas) {
        if (box == null) {
            return
        }
        var rect = RectF(box)
//        rect = translateRect(rect)
        canvas.drawRect(rect, rectPaint!!)
    }

    override fun contains(x: Float, y: Float): Boolean {
        if (box == null) {
            return false
        }
        var rect = RectF(box)
        rect = translateRect(rect)
        return rect.contains(x,y)
    }
}