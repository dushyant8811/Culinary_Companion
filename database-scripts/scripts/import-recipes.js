const admin = require('firebase-admin');
const path = require('path');
const fs = require('fs');

// Path to your JSON file
// Assuming 'scripts' and 'data' are sibling folders inside 'database-scripts'
const jsonPath = path.join(__dirname, '../data/recipes.json');
const recipes = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));

// Initialize Firebase
const serviceAccount = require('./serviceAccountKey.json'); // Get this from Firebase console

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://culinary-companion-android.firebaseio.com" // Replace with your URL
});

const db = admin.firestore();
const batchSize = 100; // Using a smaller batch size is safer

async function importRecipes() {
  try {
    console.log(`Starting import of ${recipes.length} recipes...`);

    for (let i = 0; i < recipes.length; i += batchSize) {
      const batch = db.batch();
      const batchRecipes = recipes.slice(i, i + batchSize);
      const recipesRef = db.collection('recipes');

      batchRecipes.forEach(recipe => {
        const docRef = recipesRef.doc(); // Auto-generate a new ID for the document

        // --- THIS IS THE UPDATED PART ---
        // Create a new, complete recipe object that matches your Kotlin data class
        const completeRecipeData = {
          ...recipe, // Copy all existing fields from the JSON file (title, ingredients, etc.)
          id: docRef.id, // Add the auto-generated document ID to the object itself
          author: "Culinary Companion", // Add a default author for these base recipes
          averageRating: 0.0, // Add the default averageRating
          reviewCount: 0,     // Add the default reviewCount
          createdAt: Date.now(), // Add the current timestamp for creation date
          updatedAt: Date.now(), // Add the current timestamp for update date
          isFavorite: false    // Ensure isFavorite is not part of the base data
        };

        // Use the new, complete object to set the data for the document
        batch.set(docRef, completeRecipeData);
      });

      await batch.commit();
      console.log(`Imported ${batchRecipes.length} recipes (${i + batchRecipes.length}/${recipes.length})`);
    }

    console.log('✅ Successfully imported all recipes!');
  } catch (error) {
    console.error('❌ Import failed:', error);
  }
}

importRecipes();