package com.example.cw2_meal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MealDAO {
    @Query("Select * from Meals")
    suspend fun getAllMeals(): List<MealDBEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meals: MealDBEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg meals: MealDBEntity)
}