package com.example.culinarycompanion.data

import com.example.culinarycompanion.model.Recipe
import com.example.culinarycompanion.model.RecipeCategory

object DataSource {
    // Updated to use enum values
    val categories = listOf(
        "Breakfast", "Lunch", "Dinner", "Desserts", "Vegan"
    )

    val recipes = listOf(
        Recipe(
            id = 1,
            title = "Fluffy Pancakes",
            ingredients = listOf(
                "1 cup all-purpose flour",
                "2 tbsp sugar",
                "2 tsp baking powder",
                "1/4 tsp salt",
                "1 cup milk",
                "1 large egg",
                "2 tbsp melted butter"
            ),
            instructions = listOf(
                "In a bowl, mix dry ingredients",
                "In another bowl, beat egg and mix with milk and melted butter",
                "Combine wet and dry ingredients, stir until just combined",
                "Heat a lightly oiled griddle over medium-high heat",
                "Pour batter, cook until bubbles form and edges are dry",
                "Flip and cook until browned on both sides"
            ),
            prepTime = 10,
            cookTime = 15,
            servings = 4,
            category = RecipeCategory.BREAKFAST.name,
            dietaryTags = listOf("Vegetarian"),
            imageUrl = "https://images.immediate.co.uk/production/volatile/sites/30/2024/06/FluffyJapanesePancakes-e6d7773.jpg?quality=90&webp=true&resize=375,341"
        ),
        Recipe(
            id = 2,
            title = "Vegetable Stir Fry",
            ingredients = listOf(
                "2 cups mixed vegetables (bell peppers, broccoli, carrots)",
                "1 tbsp sesame oil",
                "2 cloves garlic, minced",
                "1 tbsp ginger, grated",
                "3 tbsp soy sauce",
                "1 tbsp honey",
                "1 tsp cornstarch",
                "2 tbsp water"
            ),
            instructions = listOf(
                "Prepare sauce: mix soy sauce, honey, cornstarch and water",
                "Heat oil in wok over high heat",
                "Add garlic and ginger, stir for 30 seconds",
                "Add vegetables, stir fry for 5-7 minutes",
                "Pour sauce over vegetables, cook until thickened",
                "Serve over rice or noodles"
            ),
            prepTime = 15,
            cookTime = 10,
            servings = 2,
            category = RecipeCategory.DINNER.name,
            dietaryTags = listOf("Vegan", "Gluten-Free"),
            imageUrl = "https://i2.wp.com/lifemadesimplebakes.com/wp-content/uploads/2021/04/vegetable-stir-fry-resize-15.jpg"
        ),
        Recipe(
            id = 3,
            title = "Chocolate Cake",
            ingredients = listOf(
                "1 3/4 cups all-purpose flour",
                "2 cups sugar",
                "3/4 cup cocoa powder",
                "1 1/2 tsp baking powder",
                "1 1/2 tsp baking soda",
                "1 tsp salt",
                "2 eggs",
                "1 cup milk",
                "1/2 cup vegetable oil",
                "2 tsp vanilla extract",
                "1 cup boiling water"
            ),
            instructions = listOf(
                "Preheat oven to 350°F (175°C)",
                "Grease and flour two 9-inch cake pans",
                "Mix dry ingredients in large bowl",
                "Add eggs, milk, oil and vanilla, beat for 2 minutes",
                "Stir in boiling water (batter will be thin)",
                "Pour into pans, bake for 30-35 minutes",
                "Cool for 10 minutes before removing from pans"
            ),
            prepTime = 20,
            cookTime = 35,
            servings = 8,
            category = RecipeCategory.DESSERTS.name,
            dietaryTags = listOf("Vegetarian"),
            imageUrl = "https://www.giverecipe.com/wp-content/uploads/2020/06/Chocolate-Strawberry-Cake-Recipe.jpg"
        )
    )

    // Helper function to get category names for UI
    fun getCategoryNames(): List<String> {
        return listOf("All") + categories
    }
}