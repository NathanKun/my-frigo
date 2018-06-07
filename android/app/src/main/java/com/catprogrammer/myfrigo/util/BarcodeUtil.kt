package com.catprogrammer.myfrigo.util


import android.util.Log
import com.catprogrammer.myfrigo.model.Food
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlin.reflect.KFunction


class BarcodeUtil {

    private var options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(
                    FirebaseVisionBarcode.FORMAT_EAN_13)
            .build()

    private var detector: FirebaseVisionBarcodeDetector = FirebaseVision.getInstance()
            .getVisionBarcodeDetector(options)

    fun detect(food: Food, cb: (f: Food) -> Unit) {

        val image = FirebaseVisionImage.fromBitmap(food.photoBitmap!!)

        detector.detectInImage(image)
                .addOnSuccessListener { barcodes: List<FirebaseVisionBarcode> ->
                    // Task completed successfully
                    if(barcodes.isNotEmpty()) {
                        food.barcode = barcodes[0].rawValue
                        Log.d("barcode detect", food.barcode)
                    } else {
                        Log.d("barcode detect", "no barcode found")
                    }
                    food.isBarcodeDetected = true
                    cb(food)
                }
                .addOnFailureListener { e: Exception ->
                    // Task failed with an exception
                    Log.d("barcode detect", "Barcode detect error")
                    e.printStackTrace()
                    food.isBarcodeDetected = true
                    cb(food)
                }
    }

}