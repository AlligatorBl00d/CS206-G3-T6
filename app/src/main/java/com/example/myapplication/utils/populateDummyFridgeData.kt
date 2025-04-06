    package com.example.myapplication.utils

    import android.content.Context
    import android.os.Build
    import android.util.Log
    import androidx.annotation.RequiresApi
    import com.example.myapplication.viewmodel.InventoryViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    fun populateDummyFridgeData(viewModel: InventoryViewModel, context: Context) {
        val dummyItems = listOf(
            Triple("Chicken", "Meat", "chicken_image"),
            Triple("Carrot", "Vegetables", "carrot_image")
        )

        val today = "01/04/25"

        dummyItems.forEach { (name, category, imageResName) ->
            viewModel.inventoryItems.value
                .filter { it.name.equals(name, ignoreCase = true) }
                .forEach { existingItem ->
                    viewModel.deleteItem(existingItem.id ?: "")
                }

            viewModel.addItemWithFsisLookup(
                context = context,
                itemName = name,
                category = category,
                quantity = 1,
                unitSize = "500g",
                storageLocation = "Fridge",
                purchaseDate = today,
                imageUrl = imageResName, // 🔹 Store the drawable name (not the ID)
                onSuccess = { Log.d("DummyData", "✅ Added $name with image") },
                onFailure = { e -> Log.e("DummyData", "❌ Failed to add $name: ${e.message}") }
            )
        }
    }
