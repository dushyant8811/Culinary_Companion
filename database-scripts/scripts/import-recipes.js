const admin = require('firebase-admin');
const path = require('path');
const fs = require('fs');

// Path to your JSON file
const jsonPath = path.join(__dirname, '../data/recipes.json');
const recipes = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));

// Initialize Firebase
const serviceAccount = require('./serviceAccountKey.json'); // Get this from Firebase console

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://culinary-companion-android.firebaseio.com" // Replace with your URL
});

const db = admin.firestore();
const batchSize = 500; // Firestore batch limit

async function importRecipes() {
  try {
    console.log(`Starting import of ${recipes.length} recipes...`);

    // Process in batches to avoid Firestore limits
    for (let i = 0; i < recipes.length; i += batchSize) {
      const batch = db.batch();
      const batchRecipes = recipes.slice(i, i + batchSize);
      const recipesRef = db.collection('recipes');

      batchRecipes.forEach(recipe => {
        const newRef = recipesRef.doc(); // Auto-generate ID
        batch.set(newRef, recipe);
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