// viewmodel/AppViewModelFactory.kt
package com.example.culinarycompanion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.culinarycompanion.repository.CollectionRepository
import com.example.culinarycompanion.repository.RecipeRepository

class AppViewModelFactory(
    private val repository: CollectionRepository,
    private val recipeRepository: RecipeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository, recipeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}