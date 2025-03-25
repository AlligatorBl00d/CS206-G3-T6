package com.example.myapplication.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.utils.FsisUtils
import com.example.myapplication.viewmodel.InventoryViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.example.myapplication.models.InventoryItem
import java.io.File
import java.io.IOException


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScannerScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) } // Track selected tab
    val context = LocalContext.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var extractedText by remember { mutableStateOf("") }
    val viewModel: InventoryViewModel = viewModel()


    // Launcher for picking an image from the gallery
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            capturedImageUri = it
            processImageUri(context, it, recognizer,viewModel) { text ->
                extractedText = text
            }
        }
    }


    // Launcher for capturing an image using the camera
    val cameraLauncher = rememberCameraLauncher { uri ->
        capturedImageUri = uri
        processImageUri(context, uri, recognizer,viewModel) { text ->
            extractedText = text
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                cameraLauncher()
            } else {
                Toast.makeText(context, "Camera permission is required to capture images", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Receipt", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF6200EE))
            )
        },
                bottomBar = {
            BottomNavigationBar(navController, selectedTab, onTabSelected = { selectedTab = it }) // ✅ Fix here
        },

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Button to pick an image from the gallery
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Select from Gallery", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button to take a picture with the camera
            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        // Request Camera Permission
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    } else {
                        // Permission already granted, launch camera
                        cameraLauncher()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC5))
            ) {
                Text("Capture with Camera", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display the selected/captured image
            capturedImageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Extracted Text:", style = MaterialTheme.typography.titleMedium)
            Text(extractedText, modifier = Modifier.padding(8.dp))
        }
    }
}

// Function to process image URI using ML Kit OCR
@RequiresApi(Build.VERSION_CODES.O)
fun processImageUri(
    context: Context,
    uri: Uri,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    viewModel: InventoryViewModel, // pass it in
    onTextExtracted: (String) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text

                // Extract date and items
                val date = extractDate(rawText)
                val items = extractItems(rawText)

                val fsisData = FsisUtils.loadFsisData(context)

                items.forEach { itemName ->
                    val match = FsisUtils.findMatch(itemName, fsisData)
                    val estimatedExpiry = match?.let {
                        FsisUtils.estimateExpiryDate(it, "fridge") // or use dropdown from UI later
                    } ?: ""

                    val inventoryItem = InventoryItem(
                        name = itemName,
                        category = "Unknown",
                        purchaseDate = extractDate(rawText),
                        estimatedExpiryDate = estimatedExpiry,
                        quantity = 1,
                        storageLocation = "Fridge"
                    )

                    viewModel.addItem(inventoryItem, onSuccess = {}, onFailure = { e ->
                        Log.e("Firestore", "Failed to add item: ${e.message}")
                    })
                }

                // Build formatted display text
                val formattedText = buildString {
                    appendLine("🗓️ Date of Purchase: $date")
                    appendLine("\n🛒 Items Bought:")
                    items.forEach { appendLine("- $it") }
                }


                // Send formatted text to UI
                onTextExtracted(formattedText)
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "Text recognition failed", e)
            }
    } catch (e: IOException) {
        Log.e("OCR", "Failed to load image from URI", e)
    }
}

// Extracts date from OCR text using regex
fun extractDate(text: String): String {
    val regex = Regex("""\b\d{1,2}[/-]\d{1,2}[/-]\d{2,4}\b""") // e.g., 12/03/2025
    return regex.find(text)?.value ?: "Not found"
}

// Filters out total, payment, store info and keeps likely item lines
fun extractItems(text: String): List<String> {
    return text.lines()
        .map { it.trim() }
        .filter { line ->
            val lower = line.lowercase()

            val isLikelyMetadata = listOf(
                "total", "visa", "payment", "cashier", "change", "ntuc",
                "mall", "thank", "saved", "terminal", "transaction", "gst",
                "selfserv", "amount", "price", "acct", "acnt", "card"
            ).any { keyword -> lower.contains(keyword) }

            val hasMaskedDigits = Regex("""x{4,}|[*]{4,}""").containsMatchIn(line.lowercase())
            val isLongDigitLine = Regex("""\d{4,}""").containsMatchIn(line) && line.length < 25

            val isAllCaps = line == line.uppercase() && line.any { it.isLetter() }

            // Only return lines that look like valid item names
            !isLikelyMetadata && !hasMaskedDigits && !isLongDigitLine && isAllCaps
        }
        .filter { it.length > 5 }
}




@Composable
fun rememberCameraLauncher(
    onImageCaptured: (Uri) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val tempImageUri = remember {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "captured_receipt.jpg"
        )
        FileProvider.getUriForFile(context, "com.example.myapplication.provider", file)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                onImageCaptured(tempImageUri)
            }
        }
    )

    return { cameraLauncher.launch(tempImageUri) }
}

