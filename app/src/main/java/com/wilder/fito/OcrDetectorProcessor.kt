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
package com.wilder.fito

import android.util.Log
import android.util.SparseArray

import com.wilder.fito.camera.GraphicOverlay
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 * TODO: Make this implement Detector.Processor<TextBlock> and add text to the GraphicOverlay
</TextBlock> */
class OcrDetectorProcessor internal constructor(private val mGraphicOverlay: GraphicOverlay<OcrGraphic>) : Detector.Processor<TextBlock> {

    override fun release() {
        mGraphicOverlay.clear()
    }

    override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
        mGraphicOverlay.clear()

        val items = detections.detectedItems

        for (i in 0..items.size() - 1) {
            val item = items.valueAt(i)
            if (item != null && item.value != null) {
                Log.d("Processor", "Text detected! " + item.value)
            }
            val graphic = OcrGraphic(mGraphicOverlay, item)
            mGraphicOverlay.add(graphic)
        }
    }

}
