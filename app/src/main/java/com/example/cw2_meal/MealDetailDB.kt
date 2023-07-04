package com.example.cw2_meal

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MealDetailDB : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_detail_db)

        //Hiding the top action bar
        supportActionBar?.hide()

        //Get the intent extra
        val mealInformation = intent.getStringExtra("meal")
        val mealImage = intent.getStringExtra("mealImage")
        if (mealInformation != null ) {
            //The views in the new activity
            val mealText = findViewById<TextView>(R.id.mealInformation)
            val mealSetImage = findViewById<ImageView>(R.id.mealDetailImage)
            //Setting the meal information to the text view
            mealText.text = mealInformation

            // Displaying the meal image if it is not null
            if (mealImage != null) {
                runBlocking {
                    launch {
                        withContext(Dispatchers.IO) {
                            val imageUrl = URL(mealImage)
                            val ingredientConnection: HttpURLConnection =
                                imageUrl.openConnection() as HttpsURLConnection
                            val image =
                                BitmapFactory.decodeStream(ingredientConnection.inputStream)
                            mealSetImage.setImageBitmap(image)
                        }
                    }
                }
            }
        }
    }
}