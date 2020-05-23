/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kastolars.expirationreminderproject.ocrreader

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.kastolars.expirationreminderproject.ocrreader.ui.camera.GraphicOverlay
import com.kastolars.expirationreminderproject.ocrreader.ui.camera.GraphicOverlay.Graphic
import com.google.android.gms.vision.text.TextBlock

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
class OcrGraphic internal constructor(overlay: GraphicOverlay<*>, private val textBlock: TextBlock?) : Graphic(overlay) {
    var id = 0

    /**
     * Checks whether a point is within the bounding box of this graphic.
     * The provided point should be relative to this graphic's containing overlay.
     * @param x An x parameter in the relative context of the canvas.
     * @param y A y parameter in the relative context of the canvas.
     * @return True if the provided point is contained within this graphic's bounding box.
     */
    override fun contains(x: Float, y: Float): Boolean {
        if (textBlock == null) {
            return false
        }
        var rect: RectF? = RectF(textBlock.boundingBox)
        rect = translateRect(rect!!)
        return rect.contains(x, y)
    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private var rectPaint: Paint? = null
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    override fun draw(canvas: Canvas) {
        if (textBlock == null) {
            return
        }

        // Draws the bounding box around the TextBlock.
        var rect: RectF? = RectF(textBlock.boundingBox)
        rect = translateRect(rect!!)
        canvas.drawRect(rect, rectPaint!!)
    }



    init {
        if (rectPaint == null) {
            rectPaint = Paint()
            rectPaint!!.color = TEXT_COLOR
            rectPaint!!.style = Paint.Style.STROKE
            rectPaint!!.strokeWidth = 4.0f
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate()
    }
}