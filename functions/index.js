const functions = require("firebase-functions");
const admin = require("firebase-admin");
const express = require("express");
const cors = require("cors");

// 🔥 Initialize Firebase Admin SDK
admin.initializeApp();
const db = admin.firestore();

//if (process.env.FUNCTIONS_EMULATOR) {
//  console.log("Using Firestore Emulator on localhost:8080");
//  db.settings({
//    host: "localhost:8080",
//    ssl: false,
//  });
//}

// 🚀 Set up Express for API Routing
const app = express();
app.use(cors({origin: true}));
app.use(express.json());

// 🟢 Create an Inventory Item (POST)
app.post("/inventory", async (req, res) => {
  try {
    const newItem = req.body;

    // Reference to the metadata document storing the last used ID
    const metadataRef = db.collection("metadata").doc("inventoryCounter");

    // Get the current counter value (or initialize to 0 if it doesn't exist)
    await db.runTransaction(async (transaction) => {
      const metadataDoc = await transaction.get(metadataRef);
      let newId = 1; // Default starting ID

      if (metadataDoc.exists) {
        const data = metadataDoc.data();
        newId = data.lastId + 1; // Increment the last ID
      }

      // Set the new ID in the document
      const newItemWithId = { id: newId, ...newItem };
      transaction.set(db.collection("inventory").doc(newId.toString()), newItemWithId);

      // Update the metadata counter
      transaction.set(metadataRef, { lastId: newId });

      res.status(201).json(newItemWithId);
    });

  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// 🔵 Get All Inventory Items (GET)
app.get("/inventory", async (req, res) => {
  try {
    const snapshot = await db.collection("inventory").get();
    const items = snapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    }));
    res.json(items);
  } catch (error) {
    res.status(500).json({error: error.message});
  }
});

// 🟡 Update an Inventory Item (PUT)
app.put("/inventory/:id", async (req, res) => {
  try {
    const {id} = req.params;
    const updatedData = req.body;
    await db.collection("inventory").doc(id).update(updatedData);
    res.json({id, ...updatedData});
  } catch (error) {
    res.status(500).json({error: error.message});
  }
});

// 🔴 Delete an Inventory Item (DELETE)
app.delete("/inventory/:id", async (req, res) => {
  try {
    const {id} = req.params;
    await db.collection("inventory").doc(id).delete();
    res.json({success: true, id});
  } catch (error) {
    res.status(500).json({error: error.message});
  }
});

// 🌍 Deploy Express API as Firebase Cloud Function
exports.api = functions.https.onRequest(app);
