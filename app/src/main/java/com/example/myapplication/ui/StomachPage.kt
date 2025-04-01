package com.example.foodinventory.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ui.BottomNavigationBar

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.myapplication.data.repository.InventoryRepository
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val inventoryRepository = InventoryRepository()

class TelegramBot(private val token: String) {
    private val bot = bot {
        token = this@TelegramBot.token
        dispatch {
            text {
                val response = "You said: ${text}"
                bot.sendMessage(ChatId.fromId(message.chat.id), response)
            }
            command("start") {
                val response = "Welcome to the Food Inventory bot!"
                bot.sendMessage(ChatId.fromId(message.chat.id), response)
            }
        }
    }

    fun start() {
        bot.startPolling()
    }

    suspend fun sendMessage(text: String): String = withContext(Dispatchers.IO) {
        when {
            text.contains("remove Chicken", ignoreCase = true) -> {
                inventoryRepository.deleteName("Spicy Japanese Fried Chicken")
                "Successfully removed Chicken from inventory."
            }
            text.contains("remove Fish Cake", ignoreCase = true) -> {
                inventoryRepository.deleteName("Fish Cake")
                "Successfully removed Fish Cake from inventory."
            }
            text.contains("remove Strawberry", ignoreCase = true) -> {
                inventoryRepository.deleteName("Strawberry")
                "Successfully removed Strawberry from inventory."
            }
            text.contains("remove Cheese", ignoreCase = true) -> {
                inventoryRepository.deleteName("Cheese")
                "Successfully removed Cheese from inventory."
            }
            else -> "I'm not sure how to process that. Can you be more specific?"
        }
    }
}


data class ChatMessage(val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StomachScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(2) } // index for Stomach tab
    var inputText by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("Hello! What have you digested?", isUser = false),
            )
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val telegramBot = remember { TelegramBot("7967667172:AAFTPRH8k5lgaKRkiXN9LuJiwRSPCHtSq7A") }

    LaunchedEffect(Unit) {
        telegramBot.start()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digest your food!", color = MaterialTheme.colorScheme.onPrimary) },
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            // Message list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = false
            ) {
                items(messages) { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (msg.isUser) Color(0xFFE0F7FA) else Color.White,
                            tonalElevation = 1.dp
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .widthIn(max = 250.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Chat input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Message") },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            messages = messages + ChatMessage(inputText, isUser = true)
                            val currentInput = inputText
                            inputText = ""
                            // TODO: Add chatbot response logic here
                            coroutineScope.launch {
                                val botResponse = telegramBot.sendMessage(currentInput)
                                val botMessage = ChatMessage(botResponse, isUser = false)
                                messages = messages + botMessage
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF6200EE), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Send input",
                        tint = Color.White
                    )
                }
            }
        }
    }
}