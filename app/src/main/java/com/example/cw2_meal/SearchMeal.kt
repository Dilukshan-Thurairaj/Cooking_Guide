package com.example.cw2_meal

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class SearchMeal : AppCompatActivity() {

    //Text view to display all the meals retrieved from api url
    lateinit var display:TextView
    //String builder that adds the meals response as string
    val mealStringBuilder = StringBuilder()
    //Stores all the jsonObject
    val mealList = mutableListOf<JSONObject>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_meal)

        //Hide the top action bar
        supportActionBar?.hide()

        //Text view to display response
        display = findViewById(R.id.displayMeal)
        //Views in the xml layout
        val searchText = findViewById<EditText>(R.id.ingredientEditText)
        val retrieveMealBtn = findViewById<Button>(R.id.retrieveSearchMeal)
        val saveMealToDB = findViewById<Button>(R.id.saveSearchToDB)

        //If there is any saved instance
        if (savedInstanceState != null){
            //Then take the saved string builder to show the user the same response even after configuration change
            mealStringBuilder.append(savedInstanceState.getString("mealDetail"))
            display.text = mealStringBuilder.toString()
        }

        //String builder to keep the meals with the ingredient
        val ingredientStringBuilder = StringBuilder()

        //List to keep track of the meal ids of the meals with the user entered ingredient
        var mealIDList = mutableListOf<String>()

        //Url to find all the meal using the ingredient
        val urlStringIngredient =
            "https://www.themealdb.com/api/json/v1/1/filter.php?i="

        //Url to find all the meal using the meal id
        val urlStringMeal =
            "https://www.themealdb.com/api/json/v1/1/lookup.php?i="

        //Create Database
        val db = Room.databaseBuilder(this,MealDatabase::class.java,"mealDatabase").build()
        //Calling DAO
        val dbDAO = db.mealDao()


        //Button to retrieve all the meals
        retrieveMealBtn.setOnClickListener {
            //Checking internet connection
            if (isConnected()) {
                //Coroutine
                runBlocking {
                    launch {
                        try {
                            withContext(Dispatchers.IO) {
                                //Clearing the string builder, to use next time when button is clicked
                                ingredientStringBuilder.clear()
                                //Creating the url with the user typed ingredient
                                val userIngredientUrl =
                                    urlStringIngredient + searchText.text.toString()
                                        .replace(" ", "%20")
                                Log.d("MyLink", userIngredientUrl)
                                //Connecting to url
                                val urlIngredient = URL(userIngredientUrl)
                                val ingredientConnection: HttpURLConnection =
                                    urlIngredient.openConnection() as HttpsURLConnection
                                //Reading the response and storing it to a string builder
                                val reader =
                                    BufferedReader(InputStreamReader(ingredientConnection.inputStream))
                                var eachLine: String? = reader.readLine()
                                while (eachLine != null) {
                                    ingredientStringBuilder.append(eachLine + "\n")
                                    eachLine = reader.readLine()
                                }
                                //Passing the string builder to get the meal ids that have the searched ingredient
                                mealIDList = parseJsonIngredient(ingredientStringBuilder)
                            }
                        }
                        //Catch any exception that occur
                        catch (e: Exception) {
                            Toast.makeText(
                                this@SearchMeal,
                                "Type The Correct Ingredient Name \n Else, Please Try Again Later",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                //Coroutine
                runBlocking {
                    launch {
                        withContext(Dispatchers.IO) {
                            //Clearing the string builder, next time button is clicked previous responses are removed
                            mealStringBuilder.clear()
                            //For all the meal ids
                            for (i in 0 until mealIDList.size) {
                                launch {
                                    withContext(Dispatchers.IO) {
                                        //This string builder to get each meal
                                        val mealStringBuilder = StringBuilder()

                                        //Create url with the meal id to get the information of each meal
                                        val userMealUrl = urlStringMeal + mealIDList[i]

                                        //Repeat
                                        Log.d("MyLink", userMealUrl)
                                        //Connect to the api
                                        val urlIngredient2 = URL(userMealUrl)
                                        val mealConnection: HttpURLConnection =
                                            urlIngredient2.openConnection() as HttpsURLConnection
                                        //Read and store each meal response from api to string builder
                                        val mealReader =
                                            BufferedReader(InputStreamReader(mealConnection.inputStream))
                                        var mealEachLine: String? = mealReader.readLine()
                                        while (mealEachLine != null) {
                                            mealStringBuilder.append(mealEachLine + "\n")
                                            Log.d("StingBuild", mealStringBuilder.toString())
                                            mealEachLine = mealReader.readLine()
                                        }
                                        //Pass the stored string builder to a function
                                        parseJsonMeal(mealStringBuilder)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //Inform user if ot connected to internet
            else{
                Toast.makeText(
                    this@SearchMeal,
                    "Please, Connect to the Internet!!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        //Button to save the meals retrieved
        saveMealToDB.setOnClickListener {
            //Coroutine
            runBlocking {
                launch {
                    //For all the json Object in mealList
                    for (i in mealList) {
                        //Function to save to database, with the dao
                        saveToDB(i, dbDAO)
                    }
                }
            }
        }
    }

    //Check internet connectivity
    fun isConnected(): Boolean {
        val connectionManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectionManager.activeNetwork
        val networkConnection = connectionManager.getNetworkCapabilities(network)

        //Check whether user phone is connected to wifi or cellular
        if (networkConnection != null) {
            return networkConnection.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkConnection.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI)
        }
        //If not connected inform user
        else {
            return false
        }
    }

    //Save the instance when configuration change occurs
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Save the string builder which has the api response before change
        outState.putString("mealDetail", mealStringBuilder.toString())
    }


    private fun saveToDB(i: JSONObject, dbDAO: MealDAO) {
        //coroutine
        runBlocking {
            launch {
                //Creating string builder to stores all ingredient and measures as a string
                val allIngredientString = StringBuilder()
                val allMeasureString = StringBuilder()
                //Array of meals
                val jsonArray: JSONArray = i.getJSONArray("meals")

                //For all meals
                for (j in 0 until jsonArray.length()) {
                    val meal: JSONObject = jsonArray[j] as JSONObject

                    //For the keys in the meal jsonObject
                    for (k in meal.keys()) {
                        //If the key has ingredient then its add to string builder
                        if (k.contains("Ingredient")) {
                            allIngredientString.append("${meal.get(k)},")
                        }
                        if (k.contains("Measure")) {
                            //Same as above if key has measure then it is a measure so we add it to a string builder
                            allMeasureString.append("${meal.get(k)},")
                        }
                    }

                    // Create a meal entity for each meal
                    val storeMeal = MealDBEntity(
                        meal.get("idMeal").toString(), meal.get("strMeal").toString(), meal.get("strDrinkAlternate").toString(),
                        meal.get("strCategory").toString(), meal.get("strArea").toString(), meal.get("strInstructions").toString(),
                        meal.get("strMealThumb").toString(), meal.get("strTags").toString(), meal.get("strYoutube").toString(),
                        allIngredientString.toString(), allMeasureString.toString(), meal.get("strSource").toString(),
                        meal.get("strImageSource").toString(), meal.get("strCreativeCommonsConfirmed").toString(), meal.get("dateModified").toString()
                    )
                    //Insert the entity to Database
                    dbDAO.insertMeal(storeMeal)
                }
            }
        }
    }

    //Function to get all meal information for the meal ids
    private fun parseJsonMeal(eachMealStringBuilder: StringBuilder) {
        //Search Meal Name
        val json = JSONObject(eachMealStringBuilder.toString())
        //List that holds all the jsonObject of the meal returned by api, this is stored to save to database when necessary
        mealList.add(json)

        //Array of meals
        val jsonArray: JSONArray = json.getJSONArray("meals")

        //For each meal
        for(i in 0 until jsonArray.length()){
            val meal: JSONObject = jsonArray[i] as JSONObject
            //Add each meal information to the string builder
            mealStringBuilder.append(meal.toString())
            mealStringBuilder.append("\n\n")
            Log.d("MealNameMEAL",mealStringBuilder.toString())
        }
        runOnUiThread {
            //display all meals in the string builder
            display.text = mealStringBuilder.toString()
        }

    }

    //Function that returns the meal ids
    private fun parseJsonIngredient(ingredientStringBuilder: StringBuilder): MutableList<String> {

        //Making the string builder to json Object
        val json = JSONObject(ingredientStringBuilder.toString())

        //List to store the meal ids
        val mealNameList = mutableListOf<String>()

        //Array of the meals
        val jsonArray: JSONArray = json.getJSONArray("meals")

        for (i in 0 until jsonArray.length()) {
            //For each meal
            val meal: JSONObject = jsonArray[i] as JSONObject
            //Store the meal id of the meal
            val mealID = meal["idMeal"] as String
            mealNameList.add(mealID)
            Log.d("MealNameIng", mealID)
        }
        //return the list with meal id
        return mealNameList
    }
}


