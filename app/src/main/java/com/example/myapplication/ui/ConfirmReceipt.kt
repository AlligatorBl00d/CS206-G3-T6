package com.example.myapplication.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.repository.InventoryRepository
import com.example.myapplication.models.InventoryItem
import com.example.myapplication.models.ScannedItem
import com.example.myapplication.utils.FsisUtils
import com.example.myapplication.utils.OcrMappingUtils
import com.example.myapplication.viewmodel.InventoryViewModel
import com.example.myapplication.viewmodel.InventoryViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmReceiptPage(
    navController: NavController,
    date: String,
    initialItems: List<ScannedItem>,
) {
    var itemList by remember { mutableStateOf(initialItems.toMutableList()) }
    val context = LocalContext.current
    val viewModel: InventoryViewModel = viewModel(
        factory = InventoryViewModelFactory(InventoryRepository())
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Purchases", color = Color.White, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF6200EE))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("🗓️ Date: $date", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                itemsIndexed(itemList) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = item.name,
                            onValueChange = { newName ->
                                val updatedList = itemList.toMutableList()
                                updatedList[index] = updatedList[index].copy(name = newName)
                                itemList = updatedList
                            },
                            label = { Text("Item ${index + 1}") },
                            modifier = Modifier.weight(0.5f)
                        )

                        TextField(
                            value = item.quantity.toString(),
                            onValueChange = { newQty ->
                                val qty = newQty.toIntOrNull() ?: 1
                                val updatedList = itemList.toMutableList()
                                updatedList[index] = updatedList[index].copy(quantity = qty)
                                itemList = updatedList
                            },
                            label = { Text("Qty") },
                            modifier = Modifier.weight(0.25f)
                        )

                        TextField(
                            value = item.unitSize,
                            onValueChange = { newSize ->
                                val updatedList = itemList.toMutableList()
                                updatedList[index] = updatedList[index].copy(unitSize = newSize)
                                itemList = updatedList
                            },
                            label = { Text("Size") },
                            modifier = Modifier.weight(0.25f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
//

                    val fsisData = FsisUtils.loadFsisData(context)

                    itemList.forEach { scannedItem ->
                        val mappedName = OcrMappingUtils.applyMapping(scannedItem.name, context)

                        // Skip clearly invalid items (metadata lines)
                        if (
                            mappedName.lowercase().contains("approved") ||
                            mappedName.lowercase().contains("signature") ||
                            mappedName.lowercase().contains("contactless") ||
                            mappedName.length < 5
                        ) {
                            Log.d("ConfirmScreen", "Skipping invalid item: $mappedName")
                            return@forEach
                        }

                        val match = FsisUtils.findMatch(mappedName, fsisData)
                        val estimatedExpiry = match?.let {
                            FsisUtils.estimateExpiryDate(it, "fridge")
                        } ?: ""

                        val inventoryItem = InventoryItem(
                            id = java.util.UUID.randomUUID().toString(),
                            name = mappedName,
                            category = "Unknown",
                            quantity = scannedItem.quantity,
                            storageLocation = "Fridge",
                            purchaseDate = date,
                            estimatedExpiryDate = estimatedExpiry
                        )

                        viewModel.addItem(
                            inventoryItem,
                            onSuccess = {
                                Log.d("Firestore", "✅ Added ${inventoryItem.name}")
                                navController.navigate("home") {
                                    popUpTo("confirm_receipt") { inclusive = true } // optional: clears back stack
                                }
                            },
                            onFailure = {
                                Toast.makeText(context, "❌ Failed to add ${inventoryItem.name}", Toast.LENGTH_SHORT).show()
                                navController.navigate("home") {
                                    popUpTo("confirm_receipt") { inclusive = true } // optional: clears back stack
                                }
                            }
                        )
                    }

                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE), // Same as top bar
                    contentColor = Color.White
                )
            ) {
                Text("Confirm")
            }
        }
    }
}
