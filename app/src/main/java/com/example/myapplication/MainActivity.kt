package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var btnGallery: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGallery = findViewById(R.id.btnGallery)
        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)

        // Open Gallery
        btnGallery.setOnClickListener {
            openGallery()
        }
    }

    // Launch gallery to select an image
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    // Handle gallery selection
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                imageView.setImageURI(it) // Display selected image
                recognizeTextFromUri(it) // Process OCR
            }
        }
    }

    // Convert URI to Bitmap and Process OCR
    private fun recognizeTextFromUri(imageUri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }

            recognizeText(bitmap)
        } catch (e: Exception) {
            Log.e("OCR", "Error loading image: ${e.message}")
        }
    }

    // ML Kit OCR Processing
    private fun recognizeText(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { result: Text ->
                val extractedText = result.text
                if (!extractedText.isNullOrEmpty()) {
                    textView.text = extractedText
                    Log.d("OCR", "Extracted Text: $extractedText")
                } else {
                    textView.text = "No text found in image"
                    Log.d("OCR", "No text detected.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "ML Kit OCR Error", e)
            }
    }
}
