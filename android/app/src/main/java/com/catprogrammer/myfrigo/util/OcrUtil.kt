package com.catprogrammer.myfrigo.util

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.catprogrammer.myfrigo.model.Food
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import java.time.DateTimeException
import java.time.LocalDate
import java.util.*


class OcrUtil {

    private var detector = FirebaseVision.getInstance()
            .visionTextDetector


    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    fun detect(food: Food, detectCallback: (f: Food) -> Unit) {

        val image1 = FirebaseVisionImage.fromBitmap(food.photoBitmap!!)
        val image2 = FirebaseVisionImage.fromBitmap(food.photoBitmap!!.rotate(90f))
        val image3 = FirebaseVisionImage.fromBitmap(food.photoBitmap!!.rotate(-90f))

        val onSuccessListener = { firebaseVisionText: FirebaseVisionText ->
            // Task completed successfully
            //Log.d("ocr detect", "detect ended")
            //Log.d("ocr detect", "blocks: ${firebaseVisionText.blocks.size}")

            var isBarcodeFound = false
            var isDateFound = false

            for (block in firebaseVisionText.blocks) {
                //val blockText: String = block.text
                for (line in block.lines) {
                    val lineText = line.text
                    val barcode = findBarcode(lineText)

                    if (barcode != null) {
                        Log.d("ocr detect", "barcode found: $barcode")

                        food.barcode = barcode
                        isBarcodeFound = true
                    }

                    try {
                        val localDate = findDate(lineText)

                        if (localDate != null) {
                            Log.d("ocr detect", "date found: $localDate")

                            if (localDate.isAfter(LocalDate.now())) {
                                food.expirationDate = localDate
                            } else {
                                food.productionDate = localDate
                            }
                            isDateFound = true
                        }
                    } catch (e: NumberFormatException) {
                        //e.printStackTrace()
                        Log.d("ocr detect", "NumberFormatException, lineText = $lineText")
                    } catch (e: DateTimeException) {
                        //e.printStackTrace()
                        Log.d("ocr detect", "DateTimeException, lineText = $lineText")
                    }
                }
            }

            if (!isBarcodeFound) {
                Log.d("ocr detect", "no ocr barcode found")
            }
            if (!isDateFound) {
                Log.d("ocr detect", "no ocr date found")
            }
            setOcrDetected(food)
            detectCallback(food)
        }

        val onFailureListener = { _: Exception ->
            // Task failed with an exception
            Log.d("ocr detect", "OCR detect error")
            //_.printStackTrace()
            setOcrDetected(food)
            detectCallback(food)
        }

        detector.detectInImage(image1)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener)
        detector.detectInImage(image2)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener)
        detector.detectInImage(image3)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener)
    }

    private fun findBarcode(str: String): String? {
        if (containsDigit(str) && !containsDivider(str) && str.trim().length >= 13) {
            val result = str.replace(Regex("[^\\d]"), "")

            //Log.d("ocr detect", "possible barcode: $str")
            //Log.d("ocr detect", "after: $result")

            return if (result.length == 13) result else null
        }
        return null
    }

    private fun findDate(str: String): LocalDate? {
        if (containsDigit(str) && containsDivider(str) && str.length <= 10) {
            Log.d("ocr detect", "possible date: $str")
            var result: LocalDate? = null

            val splitStr: ArrayList<String> = when {
                str.contains('.') -> str.split('.') as ArrayList<String>
                str.contains('-') -> str.split('-') as ArrayList<String>
                str.contains('/') -> str.split('/') as ArrayList<String>

                else -> ArrayList()
            }

            if (splitStr.size <= 3) {
                for (i in splitStr.indices) {
                    splitStr[i] = splitStr[i].replace(Regex("[^\\d]"), "")
                }

                if (splitStr.size == 3) {
                    val int1 = splitStr[0].toInt()
                    val int2 = splitStr[1].toInt()
                    val int3 = splitStr[2].toInt()

                    // yyyy-MM-dd
                    if (splitStr[0].length == 4 && splitStr[1].length == 2 && splitStr[2].length == 2) {
                        result = LocalDate.of(int1, int2, int3)
                    }
                    // dd-MM-YYYY
                    else if (splitStr[0].length == 2 && splitStr[1].length == 2 && splitStr[2].length == 4) {
                        result = LocalDate.of(int3, int2, int1)
                    }
                    // dd-MM-yy       (ignore yy-MM-dd)
                    else if (splitStr[0].length == 2 && splitStr[1].length == 2 && splitStr[2].length == 2) {
                        result = LocalDate.of(int3 + 2000, int2, int1)
                    }
                } else if (splitStr.size == 2) {
                    val int1 = splitStr[0].toInt()
                    val int2 = splitStr[1].toInt()
                    // MM-dd
                    if ((int1 in 1..12) && (int2 in 1..31)
                            && (int1 == LocalDate.now().monthValue || int1 == LocalDate.now().month.value + 1)) {
                        result = LocalDate.of(LocalDate.now().year, int1, int2)
                    }
                    // dd-MM
                    else if ((int2 in 1..12) && (int1 in 1..31)
                            && (int2 == LocalDate.now().monthValue || int2 == LocalDate.now().month.value + 1)) {
                        result = LocalDate.of(LocalDate.now().year, int2, int1)
                    }
                } // if (splitStr.size == 2) AND else if (splitStr.size == 3)

                Log.d("ocr detect", "after: $result")
                return result
            } // if (splitStr.size <= 3)
            return null

        } // if (containsDigit(str) && containsDivider(str) && str.length <= 10)
        return null
    }

    private fun containsDigit(str: String): Boolean {
        return str.contains('1') || str.contains('2') || str.contains('3') ||
                str.contains('4') || str.contains('5') || str.contains('8') ||
                str.contains('7') || str.contains('8') || str.contains('9') ||
                str.contains('0')
    }

    private fun containsDivider(str: String): Boolean {
        return str.contains('/') || str.contains('-') || str.contains('.')
    }

    private fun setOcrDetected(food: Food) {
        if (!food.isOcr1Detected) {
            Log.d("ocr detect", "detect 1 ended")
            food.isOcr1Detected = true
        } else if (!food.isOcr2Detected) {
            Log.d("ocr detect", "detect 2 ended")
            food.isOcr2Detected = true
        } else {
            Log.d("ocr detect", "detect 3 ended")
            food.isOcr3Detected = true
        }
    }
}