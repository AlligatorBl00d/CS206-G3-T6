package com.example.myapplication.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.models.InventoryItem
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Existing FoodItem for display (internal use only)
data class FoodItem(
    val name: String,
    val quantity: String,
    val daysLeft: String,
    val expiryDate: String,
    val icon: Int
)

// Convert InventoryItem to FoodItem for UI
fun InventoryItem.toDisplay(): FoodItem {
    val icon = when (imageUrl) {
        "carrot_image" -> R.drawable.carrot_image
        "apple_image" -> R.drawable.apple_image
        "grapes_image" -> R.drawable.grapes_image
        "chicken_image" -> R.drawable.chicken_image
        else -> R.drawable.expiring_icon
    }

    // Optional: Replace with real calculation
    val daysLeft = "2 days"

    return FoodItem(
        name = name,
        quantity = "x$quantity",
        daysLeft = daysLeft,
        expiryDate = estimatedExpiryDate,
        icon = icon
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FridgeScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val db = Firebase.firestore
    val inventoryItems = remember { mutableStateListOf<InventoryItem>() }
//    val viewModel: InventoryViewModel = viewModel() // 🔹 Place this here
//    val inventoryItems by viewModel.inventoryItems.collectAsState()

    LaunchedEffect(Unit) {
        db.collection("inventoryItems")
            .get()
            .addOnSuccessListener { result ->
                val items = result.mapNotNull { it.toObject(InventoryItem::class.java) }
                inventoryItems.clear()
                inventoryItems.addAll(items)
            }
            .addOnFailureListener { exception ->
                Log.e("FridgeScreen", "Error fetching items", exception)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fridge", color = Color.White, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF6200EE))
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, selectedTab, onTabSelected = { selectedTab = it })
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            // Filter Row (static for now)
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("All", "Vegetables", "Fruits", "Meat").forEachIndexed { index, filter ->
                    FilterChip(filter, selected = index == 0)
                }
            }

            // Inventory Items List
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                inventoryItems.forEach { item ->
                    val displayItem = item.toDisplay()
                    FoodItemRow(displayItem, onDelete = {
                        db.collection("inventoryItems").document(item.id).delete()
                        inventoryItems.remove(item)
                    })
                }
            }
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean) {
    Button(
        onClick = { /* TODO: Add filtering */ },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF6200EE) else Color.LightGray,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text)
    }
}

@Composable
fun FoodItemRow(item: FoodItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFFEDEDED), shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDEDED))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Icon + Name
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = item.icon),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 12.dp)
                )
                Text(
                    text = "${item.name} ${item.quantity}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Expiry Details
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = item.daysLeft,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Use by ${item.expiryDate}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FridgeScreenPreview() {
    val navController = rememberNavController()
    FridgeScreen(navController = navController)
}
