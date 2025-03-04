package com.example.healthhack25


import android.content.Context
import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class OCRProcessor(private val context: Context) {

    private val tessBaseAPI: TessBaseAPI = TessBaseAPI()
    private val DATA_PATH = context.filesDir.absolutePath + "/tesseract/"
    private val LANG = "eng"

    init {
        checkAndCopyTrainedData()
        tessBaseAPI.init(DATA_PATH, LANG)

    }

    private fun checkAndCopyTrainedData() {
        val tessdataDir = File(DATA_PATH, "tessdata")
        if (!tessdataDir.exists()) {
            tessdataDir.mkdirs()
            copyTrainedData()
        }
    }

    private fun copyTrainedData() {
        try {
            val trainedDataPath = "$DATA_PATH/tessdata/$LANG.traineddata"
            val trainedDataFile = File(trainedDataPath)
            if (!trainedDataFile.exists()) {
                val inputStream = context.assets.open("tessdata/$LANG.traineddata")
                val outputStream = FileOutputStream(trainedDataFile)
                val buffer = ByteArray(1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                inputStream.close()
                outputStream.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun extractTextFromImage(bitmap: Bitmap): String {
        tessBaseAPI.setImage(bitmap)
        val extractedText = tessBaseAPI.utF8Text
        tessBaseAPI.end()
        return extractedText
    }
}
