package com.example.myapplication.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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

// Define data class for items
data class FoodItem(
    val name: String,
    val quantity: String,
    val daysLeft: String,
    val expiryDate: String,
    val icon: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FridgeScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) } // Track selected tab
    val foodItems = remember { mutableStateListOf( // ✅ Dynamic list
        FoodItem("Carrot", "x3", "0 days", "20/03/25", R.drawable.carrot_image),
        FoodItem("Apple", "x2", "2 days", "22/03/25", R.drawable.apple_image),
        FoodItem("Grapes", "x3", "2 days", "22/03/25", R.drawable.grapes_image),
        FoodItem("Chicken", "x1", "4 days", "22/03/25", R.drawable.chicken_image)
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fridge", color = Color.White, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF6200EE))
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, selectedTab, onTabSelected = { selectedTab = it }) // ✅ Fix here
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Filter Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("All", "Vegetables", "Fruits", "Meat").forEachIndexed { index, filter ->
                    FilterChip(filter, selected = index == 0)
                }
            }

            // Food Items List
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                foodItems.forEach { item ->
                    FoodItemRow(item, onDelete = { foodItems.remove(item) }) // ✅ Pass delete function
                }
            }
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean) {
    Button(
        onClick = {},
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
fun FoodItemRow(item: FoodItem, onDelete: () -> Unit) { // ✅ Added delete function
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

            // Delete Button
            IconButton(onClick = { onDelete() }) { // ✅ Deletes the item
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
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