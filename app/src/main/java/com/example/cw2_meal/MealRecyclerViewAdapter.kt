package com.example.cw2_meal

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

//Reference : https://www.youtube.com/watch?v=WqrpcWXBz14

//Recycler view class
class MealRecyclerViewAdapter (val meals: ArrayList<MealDBEntity>)
    : RecyclerView.Adapter<MealRecyclerViewAdapter.MealRecyclerViewHolder>(){

    //object that has image hashmap which stores the url of image and the image bitmap
    object CacheImage{
        val image = HashMap<String, Bitmap>()

        //Get the image bitmap for the given image url
        fun getBitmap(urlImage: String?): Bitmap? {
            return image[urlImage]
        }

        //Add the bitmap and the image url to the hashmap
        fun addBitmap(urlImage: String, bitmap: Bitmap){
            image[urlImage] = bitmap
        }
    }

    class MealRecyclerViewHolder(mealView: View): RecyclerView.ViewHolder(mealView){
        //The Views in the recycler view (Image of meal and name of meal)
        val mealImageView: ImageView = mealView.findViewById(R.id.MealImage)
        val mealName: TextView = mealView.findViewById(R.id.displayMeal)
    }

    @Override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealRecyclerViewHolder {
        //Inflate the layout when new view is added
        val mealView = LayoutInflater.from(parent.context).inflate(R.layout.each_item_database,parent,false)
        return MealRecyclerViewHolder(mealView)
    }

    @Override
    override fun onBindViewHolder(holder: MealRecyclerViewHolder, position: Int) {
        //For each meal in meal list passed
        val meal = meals[position]
        //Get the meal thumbnail image url
        val mealImage = meal.mealThumb
        //Get the image bitmap if its available in the hashmap
        val imageBitmapCache = CacheImage.getBitmap(mealImage)
        try {
            //If the bitmap is not null, means the hashmap already has the image stored in hashmap
            if(imageBitmapCache != null){
                //Display the name of meal
                holder.mealName.text = meal.mealName
                //Display the image of meal
                holder.mealImageView.setImageBitmap(imageBitmapCache)
            }
            //If not there in the hashmap
            else {
                //Coroutine
                runBlocking {
                    launch {
                        withContext(Dispatchers.IO) {
                            //Get the thumbnail of the meal
                            val mealImageUrl = meal.mealThumb
                            //Connect with the api
                            val imageUrl = URL(mealImageUrl)
                            val ingredientConnection: HttpURLConnection =
                                imageUrl.openConnection() as HttpsURLConnection
                            //Decode the image to bitmap
                            val image =
                                BitmapFactory.decodeStream(ingredientConnection.inputStream)
                            if (mealImageUrl != null) {
                                //Add the image to the hashmap, so next time not needed to connect to the internet
                                CacheImage.addBitmap(mealImageUrl,image)
                            }
                            //Set the name and the image
                            holder.mealImageView.setImageBitmap(image)
                            holder.mealName.text = meal.mealName
                        }
                    }
                }
            }
        }
        catch (e: Exception) {
            Toast.makeText(
                holder.itemView.context,
                "Type The Correct Meal Name \n Else, Please Try Again Later",
                Toast.LENGTH_LONG
            ).show()
        }

        //Setting on click listener to the views in the recycler view
        holder.itemView.setOnClickListener{
            //Creating an intent to pass the meal details and meal thumbnail to another activity and start it
            val mealDetailIntent = Intent(holder.itemView.context,MealDetailDB::class.java)
            mealDetailIntent.putExtra("meal",meal.toString())
            mealDetailIntent.putExtra("mealImage",meal.mealThumb)
            holder.itemView.context.startActivity(mealDetailIntent)
            Log.d("Meal detail",meal.toString())
        }
    }

    //Number of of items in the meal list (number of meals)
    @Override
    override fun getItemCount(): Int {
        return meals.size
    }

}