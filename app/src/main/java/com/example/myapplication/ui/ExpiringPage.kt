package com.example.foodinventory.ui.theme

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.models.InventoryItem
import com.example.myapplication.ui.BottomNavigationBar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiringPageScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val db = Firebase.firestore
    val expiringItems = remember { mutableStateListOf<InventoryItem>() }

    var selectedItems by remember { mutableStateOf(setOf<InventoryItem>()) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("inventoryItems")
            .get()
            .addOnSuccessListener { result ->
                val items = result.mapNotNull { it.toObject(InventoryItem::class.java) }
                    .filter { it.estimatedExpiryDate.isNotEmpty() } // Add actual expiry logic here
                expiringItems.clear()
                expiringItems.addAll(items)
            }
            .addOnFailureListener { exception ->
                Log.e("ExpiringPage", "Error fetching items", exception)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expiring Food", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF6200EE))
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, selectedTab, onTabSelected = { selectedTab = it })
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.warning_image),
                    contentDescription = "Warning Sign",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "These food are expiring soon!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(expiringItems) { item ->
                        val isSelected = selectedItems.contains(item)
                        InventoryItemCard(
                            item = item,
                            isSelected = isSelected,
                            onClick = {
                                selectedItems = if (isSelected) {
                                    selectedItems - item
                                } else {
                                    selectedItems + item
                                }
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Button(
                    onClick = {
                        if (selectedItems.isNotEmpty()) {
                            showDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .height(50.dp)
                ) {
                    Text("I have consumed", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Remove selected items?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val db = Firebase.firestore
                                selectedItems.forEach { item ->
                                    db.collection("inventoryItems").document(item.id).delete()
                                        .addOnSuccessListener {
                                            Log.d("ExpiringPage", "Deleted ${item.name}")
                                            expiringItems.remove(item) // Update UI
                                        }
                                        .addOnFailureListener {
                                            Log.e("ExpiringPage", "Failed to delete ${item.name}", it)
                                        }
                                }
                                selectedItems = emptySet()
                                showDialog = false
                            }
                        ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (item.imageUrl) {
        "carrot_image" -> R.drawable.carrot_image
        "apple_image" -> R.drawable.apple_image
        "grapes_image" -> R.drawable.grapes_image
        "chicken_image" -> R.drawable.chicken_image
        else -> R.drawable.expiring_icon
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFD0F0C0) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 12.dp)
                )

                Column {
                    Text(
                        text = "${item.name} x${item.quantity}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Use by ${item.estimatedExpiryDate}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = "2 days", // You can calculate days left here
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}