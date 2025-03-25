package com.example.myapplication.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.repository.InventoryRepository
import com.example.myapplication.models.InventoryItem
import com.example.myapplication.models.ScannedItem
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
                title = { Text("Confirm Purchases") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
                    itemList.forEach { scannedItem ->
                        val inventoryItem = InventoryItem(
                            name = scannedItem.name,
                            category = "Unknown",
                            quantity = scannedItem.quantity,
                            storageLocation = "Fridge",
                            purchaseDate = date,
                            estimatedExpiryDate = "" // You can replace this with FSIS estimate if needed
                        )

                        viewModel.addItem(
                            inventoryItem,
                            onSuccess = {},
                            onFailure = {
                                Toast.makeText(context, "Failed to add ${scannedItem.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    Toast.makeText(context, "Items confirmed!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm")
            }
        }
    }
}
