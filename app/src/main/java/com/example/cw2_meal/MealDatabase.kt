package com.example.cw2_meal

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MealDBEntity::class], version = 1)
abstract class MealDatabase: RoomDatabase() {
    abstract fun mealDao(): MealDAO
}