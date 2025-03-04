package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.healthhack25.OCRProcessor
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var ocrProcessor: OCRProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize OCR
        ocrProcessor = OCRProcessor(this)

        setContent {
            MyApplicationTheme {
                OCRScreen(ocrProcessor)
            }
        }
    }
}

@Composable
fun OCRScreen(ocrProcessor: OCRProcessor) {
    var extractedText by remember { mutableStateOf("") }

    // Load a sample image from resources
    val bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.sample_text)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "OCR Image Scanner", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Sample Image", modifier = Modifier.size(200.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            extractedText = ocrProcessor.extractTextFromImage(bitmap)
        }) {
            Text(text = "Extract Text")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Extracted Text:", style = MaterialTheme.typography.titleMedium)
        BasicText(text = extractedText, modifier = Modifier.padding(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun OCRScreenPreview() {
    MyApplicationTheme {
        OCRScreen(OCRProcessor(LocalContext.current))
    }
}


