package com.example.myapplication.chatbot

import com.example.myapplication.data.repository.InventoryRepository
import com.example.myapplication.models.InventoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatBotManager(private val inventoryRepository: InventoryRepository) {

    suspend fun getResponse(input: String): String = withContext(Dispatchers.IO) {
        val lower = input.lowercase()

        // Match format: consume itemName 300g or just consume itemName
        val consumeRegex = Regex("consume (.+?) (\\d+)([a-zA-Z]+)")
        val match = consumeRegex.find(lower)

        if (match != null) {
            val itemName = match.groupValues[1].trim().lowercase()
            val amountToConsumeStr = match.groupValues[2]
            val unit = match.groupValues[3].trim().lowercase()

            val amountToConsume: Int = amountToConsumeStr.toIntOrNull()
                ?: return@withContext "❌ Invalid quantity."

            // From here on, amountToConsume is non-null
            var remainingToConsume = amountToConsume  // ✅ Only declare once here


            val allMatching = inventoryRepository.getAllItemsSnapshot().documents
                .mapNotNull { it.toObject(InventoryItem::class.java) }
                .filter {
                    it.name.lowercase().contains(itemName) &&
                            it.unitSize.lowercase().contains(unit)
                }

            if (allMatching.isEmpty()) {
                return@withContext "❌ Item '$itemName' with unit $unit not found in inventory."
            }

            var itemsConsumed = 0
            var totalConsumed = 0

            for (item in allMatching.sortedBy { extractNumericValue(it.unitSize) }) {
                val itemAmount = extractNumericValue(item.unitSize)
                if (remainingToConsume <= 0) break

                if (itemAmount <= remainingToConsume) {
                    inventoryRepository.deleteName(item.name)
                    remainingToConsume -= itemAmount
                    totalConsumed += itemAmount
                    itemsConsumed++
                } else {
                    // Partial consumption (optional enhancement)
                    val newAmount = itemAmount - remainingToConsume
                    val updatedItem = item.copy(unitSize = "${newAmount}${unit}")
                    inventoryRepository.updateItem(updatedItem) // You must implement this
                    totalConsumed += remainingToConsume
                    itemsConsumed++
                    remainingToConsume = 0
                }
            }

            return@withContext if (remainingToConsume <= 0) {
                "✅ Consumed $totalConsumed$unit of $itemName (from $itemsConsumed item(s))."
            } else {
                "❌ Not enough $itemName to consume $amountToConsume$unit. Only consumed $totalConsumed$unit from $itemsConsumed item(s)."
            }
        }


        // List fridge
        else if (lower.contains("list fridge")) {
            val fridgeItems = inventoryRepository.getAllItemsSnapshot().documents
                .mapNotNull { it.toObject(InventoryItem::class.java) }
                .filter { it.storageLocation.equals("Fridge", ignoreCase = true) }

            return@withContext if (fridgeItems.isEmpty()) {
                "🧊 Fridge is empty!"
            } else {
                "🧊 Fridge contains:\n" + fridgeItems.joinToString("\n") {
                    "• ${it.name} (${it.quantity} x ${it.unitSize})"
                }
            }
        }

        else {
            "🤖 Sorry, I didn't understand. Try:\n• 'consume strawberry 300g'\n• 'list fridge'"
        }
    }

    private fun extractNumericValue(unitSize: String): Int {
        val numberRegex = Regex("(\\d+)")
        return numberRegex.find(unitSize)?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }
}
