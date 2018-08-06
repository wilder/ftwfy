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
package com.wilderpereira.ftwfy

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.text.TextRecognizer
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wilderpereira.ftwfy.camera.CameraSource
import com.wilderpereira.ftwfy.camera.CameraSourcePreview
import com.wilderpereira.ftwfy.camera.GraphicOverlay
import java.io.IOException

/**
 * Activity for the Ocr Detecting app.  This app detects text and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 */
class CameraActivity : AppCompatActivity() {

    private var mCameraSource: CameraSource? = null
    private var processor: DetectorProcessor? = null
    private lateinit var mPreview: CameraSourcePreview
    private lateinit var mGraphicOverlay: GraphicOverlay<OcrGraphic>

    // Helper objects for detecting taps and pinches.
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null

    //text to find
    private var textToFind: String = "var"

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.ocr_capture)

        mPreview = findViewById(R.id.preview) as CameraSourcePreview
        mGraphicOverlay = findViewById(R.id.graphicOverlay) as GraphicOverlay<OcrGraphic>

        // Set good defaults for capturing text.
        val autoFocus = true
        val useFlash = false

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash, textToFind)
        } else {
            requestCameraPermission()
        }

        gestureDetector = GestureDetector(this, GestureDetector.SimpleOnGestureListener())
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        val regexCheckbox = findViewById<CheckBox>(R.id.regexCheckbox)
        val etTextToFind = findViewById<EditText>(R.id.textToFind)

        RxTextView.textChanges(etTextToFind).subscribe {
            textToFind = etTextToFind.text.toString()
            processor?.textToFind = textToFind
        }

        regexCheckbox.setOnClickListener {
            processor?.regexEnabled = regexCheckbox.isChecked
        }

    }


    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private fun requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission")

        val permissions = arrayOf(Manifest.permission.CAMERA)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }

        val thisActivity = this

        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(thisActivity, permissions,
                    RC_HANDLE_CAMERA_PERM)
        }

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val b = scaleGestureDetector!!.onTouchEvent(e)

        val c = gestureDetector!!.onTouchEvent(e)

        return b || c || super.onTouchEvent(e)
    }


    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.

     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private fun createCameraSource(autoFocus: Boolean, useFlash: Boolean, textToFind: String) {
        val context = applicationContext

        // Create the TextRecognizer
        val textRecognizer = TextRecognizer.Builder(context).build()

        processor = DetectorProcessor(mGraphicOverlay, textToFind)

        // Set the TextRecognizer's Processor.
        textRecognizer.setProcessor(processor!!)

        // Check if the TextRecognizer is operational.
        if (!textRecognizer.isOperational) {
            Log.w(TAG, "Detector dependencies are not yet available.")

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            val lowstorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = registerReceiver(null, lowstorageFilter) != null

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show()
                Log.w(TAG, getString(R.string.low_storage_error))
            }
        }

        // Create the mCameraSource using the TextRecognizer.
        mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(15.0f)
                .setFlashMode(if (useFlash) Camera.Parameters.FLASH_MODE_TORCH else null)
                .setFocusMode(if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE else null)
                .build()

    }

    /**
     * Restarts the camera.
     */
    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    /**
     * Stops the camera.
     */
    override fun onPause() {
        super.onPause()
        mPreview.stop()
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    override fun onDestroy() {
        super.onDestroy()
        mPreview.release()
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on [.requestPermissions].
     *
     *
     * **Note:** It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     *

     * @param requestCode  The request code passed in [.requestPermissions].
     * *
     * @param permissions  The requested permissions. Never null.
     * *
     * @param grantResults The grant results for the corresponding permissions
     * *                     which is either [PackageManager.PERMISSION_GRANTED]
     * *                     or [PackageManager.PERMISSION_DENIED]. Never null.
     * *
     * @see .requestPermissions
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: $requestCode")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source")
            // We have permission, so create the camerasource
            val autoFocus = intent.getBooleanExtra(AutoFocus, false)
            val useFlash = intent.getBooleanExtra(UseFlash, false)
            createCameraSource(autoFocus, useFlash, textToFind)
            return
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.size +
                " Result code = " + if (grantResults.isNotEmpty()) grantResults[0] else "(empty)")

        val listener = DialogInterface.OnClickListener { _, _ -> finish() }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ftwfy")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show()
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                applicationContext)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource!!, mGraphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }

        }
    }

    private inner class ScaleListener : ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.

         * @param detector The detector reporting the event - use this to
         * *                 retrieve extended info about event state.
         * *
         * @return Whether or not the detector should consider this event
         * * as handled. If an event was not handled, the detector
         * * will continue to accumulate movement until an event is
         * * handled. This can be useful if an application, for example,
         * * only wants to update scaling factors if the change is
         * * greater than 0.01.
         */
        override fun onScale(detector: ScaleGestureDetector): Boolean = false

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.

         * @param detector The detector reporting the event - use this to
         * *                 retrieve extended info about event state.
         * *
         * @return Whether or not the detector should continue recognizing
         * * this gesture. For example, if a gesture is beginning
         * * with a focal point outside of a region where it makes
         * * sense, onScaleBegin() may return false to ignore the
         * * rest of the gesture.
         */
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         *
         *
         * Once a scale has ended, [ScaleGestureDetector.getFocusX]
         * and [ScaleGestureDetector.getFocusY] will return focal point
         * of the pointers remaining on the screen.

         * @param detector The detector reporting the event - use this to
         * *                 retrieve extended info about event state.
         */
        override fun onScaleEnd(detector: ScaleGestureDetector) {
            if (mCameraSource != null) {
                mCameraSource!!.doZoom(detector.scaleFactor)
            }
        }
    }

    companion object {
        private val TAG = "CameraActivity"

        // Intent request code to handle updating play services if needed.
        private val RC_HANDLE_GMS = 9001

        // Permission request codes need to be < 256
        private val RC_HANDLE_CAMERA_PERM = 2

        // Constants used to pass extra data in the intent
        val AutoFocus = "AutoFocus"
        val UseFlash = "UseFlash"
    }
}
