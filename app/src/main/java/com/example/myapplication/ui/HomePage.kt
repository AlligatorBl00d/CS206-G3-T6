package com.example.myapplication.ui

import android.annotation.SuppressLint
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

// Define Data Model for List Items
data class Item(val title: String, val subtitle: String)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen() {
    val items = listOf(
        Item("Fridge", "5 items total"),
        Item("Freezer", "8 items total"),
        Item("Pantry", "12 items total")
    )

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
                    ListItem(item)
                }
            }
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
                imageVector = Icons.Default.Search, // Using Built-in Material Icon
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
fun ListItem(item: Item) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /* TODO: Handle item click */ },
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
                painter = painterResource(id =
                if (item.title == "Fridge") R.drawable.fridge_image
                else if (item.title == "Freezer") R.drawable.snowflake
                else R.drawable.pantry
                ),
                contentDescription = item.title,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
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
    HomeScreen()
}