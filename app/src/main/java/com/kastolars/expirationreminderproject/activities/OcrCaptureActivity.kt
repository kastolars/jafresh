package com.kastolars.expirationreminderproject.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.util.valueIterator
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.kastolars.expirationreminderproject.R
import java.io.IOException
import java.util.*


class OcrCaptureActivity : AppCompatActivity() {

    lateinit var textRecognizer: TextRecognizer
    private val requestCameraPermissionId = 1001
    lateinit var surfaceView: SurfaceView
    private lateinit var cameraSource: CameraSource
    private val tag = "exprem" + OcrCaptureActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(tag, "onCreate called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr_capture)

        surfaceView = findViewById(R.id.surface_view)

        // Initialize text recognizer
        textRecognizer = TextRecognizer.Builder(applicationContext).build()
        if (!textRecognizer.isOperational) {
            Log.w(tag, "Detector dependencies are not yet available")
        } else {
            // Build cameraSource
            cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1980, 1080)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build()

            surfaceView.holder.addCallback(SurfaceCallback())
            textRecognizer.setProcessor(OcrProcessor())
        }
    }

    // OCR Processor for extracting text
    inner class OcrProcessor :
        Detector.Processor<TextBlock> {


        private val pattern = "([0-9]{1,2}|[a-zA-Z]{3,})\\W{0,2}[0-9]{1,2}\\W{0,2}[0-9]{2,4}"
        private val captureRegex = Regex(".*${pattern}.*")
        private val cleanRegex = Regex(pattern)

        private var isTakingPicture = false

        override fun release() {
            Log.v(tag, "release called")
        }

        // Extract the date after detecting a matching result
        inner class PictureCallback(val item: TextBlock) : CameraSource.PictureCallback {
            override fun onPictureTaken(p0: ByteArray?) {
                isTakingPicture = false
                var extractedText: MatchResult
                try {
                    extractedText = extractClean(item.value)!!
                } catch (e: NullPointerException) {
                    Log.e(tag, "Failed to extract text: $e")
                    return
                }
                val date = toDate(extractedText.value)
                if (date != null) {
                    // Build the intent and start the new item activity
                    val intent =
                        Intent(applicationContext, ItemActivity::class.java)
                    val cal = Calendar.getInstance(TimeZone.getDefault())
                    cal.time = date
                    intent.putExtra("year", cal.get(Calendar.YEAR))
                    intent.putExtra("month", cal.get(Calendar.MONTH))
                    intent.putExtra("dayOfMonth", cal.get(Calendar.DATE))
                    Log.v(
                        tag,
                        "${cal.get(Calendar.MONTH)} - ${cal.get(Calendar.DATE)} - ${cal.get(
                            Calendar.YEAR
                        )}"
                    )
                    startActivityForResult(intent, 1)
                    val handler = Handler(Looper.getMainLooper())
                    handler.post {
                        cameraSource.release()
                    }
                }
            }
        }

        // Filters all text detections fo the ones that match the regex
        override fun receiveDetections(detections: Detector.Detections<TextBlock>?) {
            val items = detections?.detectedItems!!
            if (items.size() != 0) {
                for (item: TextBlock in items.valueIterator()) {
                    if (!isTakingPicture && captureRegex.matches(item.value)) {
                        cameraSource.takePicture(null, PictureCallback(item))
                        isTakingPicture = true
                    }
                }
            }
        }

        // Removes all unrelated characters
        private fun extractClean(capturedText: String): MatchResult? {
            return cleanRegex.find(capturedText)
        }

        /**
         * Converts the string into a date object
         * Note that this is not robust and there are many formats that need to be
         * accounted for. Unit tests can be used to improve robustness for
         * cases that fail to be detected/converted.
         */
        private fun toDate(s: String): Date? {
            Log.v(tag, "toDate called")
            val months = arrayOf(
                "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug",
                "sep", "oct", "nov", "dec"
            )
            val cal = Calendar.getInstance(TimeZone.getDefault())
            var year: Int? = null
            var month: Int? = null
            var dayOfTheMonth: Int? = null
            // Split the string into date elements
            val splits = s.split(Regex("\\W{1,2}"))
            for (i in splits.indices) {
                if (splits[i].matches(Regex("[a-zA-z]+"))) {
                    for (j in 0..11) {
                        if (months[j].matches(Regex(splits[i], RegexOption.IGNORE_CASE))) {
                            month = j
                            break
                        }
                    }
                } else {
                    try {
                        val num = splits[i].toInt()
                        if (num > 31) {
                            year = num
                        } else {
                            when {
                                month == null -> {
                                    month = num - 1
                                }
                                dayOfTheMonth == null -> {
                                    dayOfTheMonth = num
                                }
                                year == null -> {
                                    year = 2000 + num
                                }
                            }
                        }
                    } catch (e: NumberFormatException) {
                        continue
                    }
                }
            }
            return try {
                cal.set(year!!, month!!, dayOfTheMonth!!)
                cal.time
            } catch (e: NullPointerException) {
                null
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.v(tag, "onActivityResult called")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data)
            cameraSource.stop()
            finish()
        } else {
            cameraSource.stop()
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    // Start the camera if permissions were granted
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.v(tag, "onRequestPermissionsResult called")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            this.requestCameraPermissionId -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                try {
                    cameraSource.start(surfaceView.holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            Log.v(tag, "surfaceChanged called")
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            Log.v(tag, "surfaceDestroyed called")
            cameraSource.stop()
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
            Log.v(tag, "surfaceCreated called")
            try {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@OcrCaptureActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        requestCameraPermissionId
                    )
                    return
                }
                cameraSource.start(surfaceView.holder)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        Log.v(tag, "onDestroy called")
        super.onDestroy()
        cameraSource.stop()
    }
}

