package com.catprogrammer.myfrigo

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.media.ExifInterface
import android.util.Log
import android.view.View
import android.widget.Toast
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.model.GeneralCallback
import com.catprogrammer.myfrigo.util.BarcodeUtil
import com.catprogrammer.myfrigo.util.OcrUtil
import com.catprogrammer.myfrigo.util.PermissionsDelegate
import com.google.gson.JsonObject
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.configuration.UpdateConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.selector.*
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread


class CameraActivity : Activity() {

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
                focusView = focus_view,
                logger = logcat(),
                lensPosition = activeCamera.lensPosition,
                cameraConfiguration = activeCamera.configuration,
                cameraErrorCallback = { Log.e(LOGGING_TAG, "Camera error: ", it) }
        )

        cam_btn_capture.setOnClickListener { takePicture() }

    }

    private fun takePicture() {
        // create a white screen flash effect to notice photo was taken
        camera_white_screen.visibility = View.VISIBLE
        (Handler()).postDelayed({ camera_white_screen.visibility = View.GONE }, 200)

        // callback for http request
        val httpCallback: GeneralCallback = object : GeneralCallback() {
            override fun onSuccess(data: JsonObject) {
                runOnUiThread { Toast.makeText(this@CameraActivity, "上传成功", Toast.LENGTH_LONG).show() }
            }

            override fun onFailure() {
                runOnUiThread { Toast.makeText(this@CameraActivity, "上传失败", Toast.LENGTH_LONG).show() }
            }
        }

        // callback for running after detect
        fun detectCallback(f: Food) {
            if (f.isBarcodeDetected && f.isOcr1Detected && f.isOcr2Detected && f.isOcr3Detected) {
                f.create(httpCallback)
            }
        }

        val photoResult = fotoapparat
                .autoFocus()
                .takePicture()

        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val currentDateandTime = sdf.format(Calendar.getInstance().time)

        val dirPath = "${Environment.getExternalStorageDirectory()}/Pictures/MyFrigo"
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdir()

        val path = "$dirPath/$currentDateandTime.jpeg"
        Log.d(tag, path)

        val barcodeUtil = BarcodeUtil()
        val ocrUtil = OcrUtil()

        photoResult.saveToFile(File(path)).whenAvailable {
            fotoapparat.updateConfiguration(
                    UpdateConfiguration(
                            focusMode = firstAvailable(
                                    continuousFocusPicture(),
                                    autoFocus()
                            )
                    )
            )

            thread(start = true) {

                // rotate
                val exif = ExifInterface(path)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                val matrix = Matrix()
                when (orientation) {
                    3 -> matrix.postRotate(180F)
                    6 -> matrix.postRotate(90F)
                    8 -> matrix.postRotate(-90F)
                    else -> matrix.postRotate(0F)
                }

                val originalBitmap = BitmapFactory.decodeFile(path)
                val rotatedBitmap = Bitmap.createBitmap(
                        originalBitmap, 0, 0,
                        originalBitmap.width, originalBitmap.height,
                        matrix, true)
                originalBitmap.recycle()
                try {
                    //save the rotated file to disk cache
                    Log.d("compressBitmap", "cacheDir: " + this.cacheDir)
                    val bmpFile = FileOutputStream("${this.cacheDir}${currentDateandTime}_rotated.jpeg")
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bmpFile)
                    bmpFile.flush()
                    bmpFile.close()
                } catch (e: Exception) {
                    Log.e("compressBitmap", "Error on saving file")
                }

                // compress rotated file
                val compressedBitmap = BitmapFactory.decodeFile(
                        resizeAndCompressImageBeforeSend(this,
                                "${this.cacheDir}${currentDateandTime}_rotated.jpeg",
                                "${currentDateandTime}_rotated_compressed.jpeg"))

                // create Food instance
                val food = Food(rotatedBitmap, compressedBitmap, path)

                // detect end upload
                barcodeUtil.detect(food, ::detectCallback)
                ocrUtil.detect(food, ::detectCallback)
            }
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


    private fun resizeAndCompressImageBeforeSend(context: Context, filePath: String, fileName: String): String {
        val maxImageSize = 1024 * 1024 // max final file size

        // First decode with inJustDecodeBounds=true to check dimensions of image
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)

        // Calculate inSampleSize(First we are going to resize the image to 800x800 image, in order to not have a big but very low quality image.
        //resizing the image will already reduce the file size, but after resizing we will check the file size and start to compress image
        options.inSampleSize = calculateInSampleSize(options, 1024, 1024)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888

        val bmpPic = BitmapFactory.decodeFile(filePath, options)


        var compressQuality = 100 // quality decreasing by 5 every loop.
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            Log.d("compressBitmap", "Quality: $compressQuality")
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
            Log.d("compressBitmap", "Size: " + streamLength / 1024 + " kb")
        } while (streamLength >= maxImageSize)

        try {
            //save the resized and compressed file to disk cache
            Log.d("compressBitmap", "cacheDir: " + context.cacheDir)
            val bmpFile = FileOutputStream("${context.cacheDir}$fileName")
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpFile)
            bmpFile.flush()
            bmpFile.close()
        } catch (e: Exception) {
            Log.e("compressBitmap", "Error on saving file")
        }

        //return the path of resized and compressed file
        return "${context.cacheDir}$fileName"
    }


    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val debugTag = "MemoryInformation"
        // Image nin islenmeden onceki genislik ve yuksekligi
        val height = options.outHeight
        val width = options.outWidth
        Log.d(debugTag, "image height: $height---image width: $width")
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        Log.d(debugTag, "inSampleSize: $inSampleSize")
        return inSampleSize
    }

}

private const val LOGGING_TAG = "Fotoapparat"

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