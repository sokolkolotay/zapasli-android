package ru.zapasli.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Pantry : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data class ProductDetails(val itemId: String) : NavKey
