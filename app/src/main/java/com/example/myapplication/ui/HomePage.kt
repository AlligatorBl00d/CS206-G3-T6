package com.example.myapplication.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.R
import com.example.myapplication.workers.ExpiryCheckWorker
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

data class Item(val title: String, val subtitle: String)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavHostController) {
    //val context = LocalContext.current

    var fridgeItemCount by remember { mutableStateOf(0) }
    val db = Firebase.firestore

    LaunchedEffect(Unit) {
        db.collection("inventoryItems")
            .get()
            .addOnSuccessListener { result ->
                // Only count items in the fridge
                val count = result.documents.count {
                    val location = it.getString("storageLocation")?.lowercase()
                    location == "fridge"
                }
                fridgeItemCount = count
            }
            .addOnFailureListener {
                Log.e("HomePage", "Failed to fetch fridge items", it)
            }
    }

    val items = listOf(
        Item("Fridge", "$fridgeItemCount items total"),
        Item("Freezer", "Not-in-use"),
        Item("Pantry", "Not-in-use")
    )

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home", color = Color.White, fontSize = 24.sp) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF6200EE)),
                actions = {
                    IconButton(onClick = { /* TODO: Settings action */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController, selectedTab, onTabSelected = { selectedTab = it })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar()
            LazyColumn {
                items(items) { item ->
                    ListItem(item, navController)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        val items = listOf("Home", "Scan", "Expiring", "Stomach")
        val icons = listOf(
            R.drawable.home_icon,
            R.drawable.scan_icon,
            R.drawable.expiring_icon,
            R.drawable.stomach_icon
        )

        items.forEachIndexed { index, label ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = {
                    onTabSelected(index)
                    if (label == "Home") {
                        navController.navigate("home") // ✅ Navigate to HomeScreen
                    }

                    if (label == "Scan") {
                        navController.navigate("receiptScanner")
                    }
                    if (label == "Expiring") {
                        navController.navigate("expiringPage")
                    }
                    if (label == "Stomach") {
                        navController.navigate("stomachPage")
                    }
                },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = icons[index]),
                            contentDescription = label,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            color = if (selectedTab == index) Color.Black else Color.Gray
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = Color.Black,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
@Composable
fun SearchBar() {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(Color(0xFFEDEDED), shape = RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray,
                modifier = Modifier.padding(end = 8.dp)
            )
            BasicTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* TODO: Handle search action */ })
            )
        }
    }
}

@Composable
fun ListItem(item: Item, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                if (item.title == "Fridge") {
                    navController.navigate("fridgePage") // ✅ Navigate to FridgePage
                }
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(
                    id = if (item.title == "Fridge") R.drawable.fridge_image
                    else if (item.title == "Freezer") R.drawable.snowflake
                    else R.drawable.pantry
                ),
                contentDescription = item.title,
                modifier = Modifier
                    .size(50.dp)
                    .padding(6.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, fontSize = 18.sp)
                Text(text = item.subtitle, fontSize = 14.sp, color = Color.Gray)
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Arrow",
                tint = Color(0xFF6200EE)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController() // ✅ Define NavController instance
    HomeScreen(navController)
}