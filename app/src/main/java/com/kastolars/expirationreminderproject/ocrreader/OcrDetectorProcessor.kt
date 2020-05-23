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

import android.graphics.Rect
import android.util.Log
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.text.TextBlock
import com.kastolars.expirationreminderproject.ocrreader.ui.CaptureBox
import com.kastolars.expirationreminderproject.ocrreader.ui.camera.GraphicOverlay

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
class OcrDetectorProcessor internal constructor(private val graphicOverlay: GraphicOverlay<GraphicOverlay.Graphic>) :
    Detector.Processor<TextBlock?> {


    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    override fun receiveDetections(detections: Detections<TextBlock?>) {
        graphicOverlay.clear()
        val left = 0
        val top = (graphicOverlay.height / 2) - 200
        val right = graphicOverlay.width
        val bottom = top + 200
        val captureRect = Rect(left, top, right, bottom)
        val captureBox = CaptureBox(graphicOverlay, captureRect)
        graphicOverlay.add(captureBox)
        val items = detections.detectedItems
        for (i in 0 until items.size()) {
            val item = items.valueAt(i)
            if (item != null && item.value != null) {
                Log.d("OcrDetectorProcessor", "Text detected! " + item.value)
                val graphic = OcrGraphic(graphicOverlay, item)
                graphicOverlay.add(graphic)
            }
        }
    }

    override fun release() {
        graphicOverlay.clear()
    }
}