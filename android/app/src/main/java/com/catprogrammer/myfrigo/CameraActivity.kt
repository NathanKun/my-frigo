package com.catprogrammer.myfrigo

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.util.BarcodeUtil
import com.catprogrammer.myfrigo.util.OcrUtil
import com.catprogrammer.myfrigo.util.PermissionsDelegate
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.selector.*
import kotlinx.android.synthetic.main.activity_camera.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.File
import java.io.IOException


class CameraActivity : AppCompatActivity() {

    private val tag = "camera"

    private val permissionsDelegate = PermissionsDelegate(this)

    private var permissionsGranted: Boolean = false
    private var activeCamera: Camera = Camera.Back

    private lateinit var fotoapparat: Fotoapparat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        permissionsGranted = permissionsDelegate.hasCameraPermission()

        if (permissionsGranted) {
            camera_view.visibility = View.VISIBLE
        } else {
            permissionsDelegate.requestPermissions()
        }

        fotoapparat = Fotoapparat(
                context = this,
                view = camera_view,
                logger = logcat(),
                lensPosition = activeCamera.lensPosition,
                cameraConfiguration = activeCamera.configuration,
                cameraErrorCallback = { Log.e(LOGGING_TAG, "Camera error: ", it) }
        )

        cam_btn_capture.setOnClickListener { takePicture() }

    }

    private fun takePicture() {
        val photoResult = fotoapparat
                .autoFocus()
                .takePicture()

        photoResult
                .saveToFile(File(
                        getExternalFilesDir("photos"),
                        "photo.jpg"
                ))

        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val currentDateandTime = sdf.format(Calendar.getInstance().time)
        val path = "${Environment.getExternalStorageDirectory()}/Pictures/$currentDateandTime.jpeg"
        //val path = "${getExternalFilesDir("photos")}/Pictures/${Calendar.getInstance().time}.jpeg"
        Log.d(tag, path)
        photoResult.saveToFile(File(path))

        val barcodeUtil = BarcodeUtil()
        val ocrUtil = OcrUtil()
        var food: Food

        // callback for http request
        val httpCallback: Callback =  object: Callback{
            override fun onFailure(call: Call?, e: IOException?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onResponse(call: Call?, response: Response?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }

        // callback for running after detect
        fun detectCallback(f: Food) {
            if (f.isBarcodeDetected && f.isOcrDetected) {
                f.create(httpCallback)
            }
        }

        photoResult.toBitmap().whenAvailable { bitmapPhoto ->
            food = Food(bitmapPhoto!!.bitmap)
            barcodeUtil.detect(food, ::detectCallback)
            ocrUtil.detect(food, ::detectCallback)
        }
    }

    /*private fun toggleFlash(): (CompoundButton, Boolean) -> Unit = { _, isChecked ->
        fotoapparat.updateConfiguration(
                UpdateConfiguration(
                        flashMode = if (isChecked) {
                            firstAvailable(
                                    torch(),
                                    off()
                            )
                        } else {
                            off()
                        }
                )
        )

        Log.i(LOGGING_TAG, "Flash is now ${if (isChecked) "on" else "off"}")
    }*/

    override fun onStart() {
        super.onStart()
        if (permissionsGranted) {
            fotoapparat.start()
        }
    }

    override fun onStop() {
        super.onStop()
        if (permissionsGranted) {
            fotoapparat.stop()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            permissionsGranted = true
            fotoapparat.start()
            camera_view.visibility = View.VISIBLE
        }
    }

}

private const val LOGGING_TAG = "Fotoapparat Example"

private sealed class Camera(
        val lensPosition: LensPositionSelector,
        val configuration: CameraConfiguration
) {

    object Back : Camera(
            lensPosition = back(),
            configuration = CameraConfiguration(
                    previewResolution = firstAvailable(
                            wideRatio(highestResolution()),
                            standardRatio(highestResolution())
                    ),
                    previewFpsRange = highestFps(),
                    flashMode = off(),
                    focusMode = firstAvailable(
                            continuousFocusPicture(),
                            autoFocus()
                    ),
                    frameProcessor = {
                        // Do something with the preview frame
                    }
            )
    )

    /*object Front : Camera(
            lensPosition = front(),
            configuration = CameraConfiguration(
                    previewResolution = firstAvailable(
                            wideRatio(highestResolution()),
                            standardRatio(highestResolution())
                    ),
                    previewFpsRange = highestFps(),
                    flashMode = off(),
                    focusMode = firstAvailable(
                            fixed(),
                            autoFocus()
                    )
            )
    )*/
}