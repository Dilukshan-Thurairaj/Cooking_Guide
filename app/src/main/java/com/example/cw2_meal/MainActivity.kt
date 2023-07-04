package com.example.cw2_meal

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
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

//https://drive.google.com/file/d/18i_FhxM4gnrv9G1NdxXShr5xcVxmd2e2/view?usp=sharing

//Main Activity
class MainActivity : AppCompatActivity() {

    //Pop up window to display meals searched from the web
    lateinit var popUpWindow: PopupWindow
    //Storing all the meal detail retrieved
    lateinit var mealDetail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Hiding the top action bar
        supportActionBar?.hide()

        //Buttons in the main page
        val addToDB = findViewById<Button>(R.id.add_toDB)
        val searchMeal = findViewById<Button>(R.id.search_mealIngredient)
        val databaseSearchMeal = findViewById<Button>(R.id.search_meal)
        val searchWeb = findViewById<Button>(R.id.searchWebService)

        //Create Database
        val db = Room.databaseBuilder(this, MealDatabase::class.java, "mealDatabase").build()
        //Calling DAO
        val dbDAO = db.mealDao()

        //Inflating the layout that displays the retrieved meals from web
        val detailLayout = layoutInflater.inflate(R.layout.web_search_details, null)

        //Width and length of the inflated layout
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT

        //Views in the inflated layout
        val displayDetail = detailLayout.findViewById<TextView>(R.id.webDisplayMeal)
        val closeBtn = detailLayout.findViewById<ImageButton>(R.id.closeBtn)

        //Creating pop up window
        popUpWindow = PopupWindow(detailLayout, width, height)

        //Button to add the text file to the database
        addToDB.setOnClickListener {
            var string: String

            //String builders to hold ingredient and measure
            val ingredientString1 = StringBuilder()
            val measureString1 = StringBuilder()

            val ingredientString2 = StringBuilder()
            val measureString2 = StringBuilder()

            val ingredientString3 = StringBuilder()
            val measureString3 = StringBuilder()

            val ingredientString4 = StringBuilder()
            val measureString4 = StringBuilder()

            //Hashmap to hold key value pair ("meal": "Chicken")
            val map1 = hashMapOf<String, String>()
            val map2 = hashMapOf<String, String>()
            val map3 = hashMapOf<String, String>()
            val map4 = hashMapOf<String, String>()

            //Coroutine
            runBlocking {
                launch {
                    //Read the file which is in the assets folder
                    application.assets.open("meals.txt").bufferedReader().use {
                        for (i in 0..27) {
                            string = it.readLine()
                            //split the line and create an array
                            val split = string.split(':', limit = 2)
                            //taking the split key and value, then adding it to the hashmap
                            val x = split[0].removeSurrounding("\"")
                            val y = split[1].replace(",", "").removeSurrounding("\"")
                            map1.put(x, y)
                        }
                        //Read empty lines
                        Log.d("My text", map1["Meal"].toString())
                        it.readLine()
                        it.readLine()
                        it.readLine()
                        //Continued for the whole text file
                        for (i in 0..25) {
                            string = it.readLine()
                            val split = string.split(':', limit = 2)
                            val x = split[0].removeSurrounding("\"")
                            val y = split[1].replace(",", "").removeSurrounding("\"")
                            map2.put(x, y)
                        }
                        Log.d("My text", map2["Meal"].toString())
                        it.readLine()
                        it.readLine()
                        it.readLine()
                        for (i in 0..28) {
                            string = it.readLine()
                            val split = string.split(':', limit = 2)
                            val x = split[0].removeSurrounding("\"")
                            val y = split[1].replace(",", "").removeSurrounding("\"")
                            map3.put(x, y)
                        }
                        Log.d("My text", map3["Meal"].toString())
                        it.readLine()
                        it.readLine()
                        it.readLine()
                        for (i in 0..31) {
                            string = it.readLine()
                            val split = string.split(':', limit = 2)
                            val x = split[0].removeSurrounding("\"")
                            val y = split[1].replace(",", "").removeSurrounding("\"")
                            map4.put(x, y)
                        }
                        Log.d("My text", map4["Meal"].toString())
                    }

                    //Taking all the ingredients and measures into one string to add to the database
                    for (i in map1.keys) {
                        if (i.contains("Ingredient")) {
                            ingredientString1.append(map1[i] + ",")
                            Log.d("Key", ingredientString1.toString())
                        }
                        if (i.contains("Measure")) {
                            measureString1.append(map1[i] + ",")
                            Log.d("Key", measureString1.toString())
                        }
                    }

                    for (i in map2.keys) {
                        if (i.contains("Ingredient")) {
                            ingredientString2.append(map2[i] + ",")
                            Log.d("Key", ingredientString2.toString())
                        }
                        if (i.contains("Measure")) {
                            measureString2.append(map2[i] + ",")
                            Log.d("Key", measureString2.toString())
                        }
                    }

                    for (i in map3.keys) {
                        if (i.contains("Ingredient")) {
                            ingredientString3.append(map3[i] + ",")
                            Log.d("Key", ingredientString3.toString())
                        }
                        if (i.contains("Measure")) {
                            measureString3.append(map3[i] + ",")
                            Log.d("Key", measureString3.toString())
                        }
                    }

                    for (i in map4.keys) {
                        if (i.contains("Ingredient")) {
                            ingredientString4.append(map4[i] + ",")
                            Log.d("Key", ingredientString4.toString())
                        }
                        if (i.contains("Measure")) {
                            measureString4.append(map4[i] + ",")
                            Log.d("Key", measureString4.toString())
                        }
                    }

                    //Adding each meal in the text file to the database
                    val meal1 = MealDBEntity(
                        "1",
                        map1["Meal"].toString(),
                        map1["DrinkAlternate"],
                        map1["Category"],
                        map1["Area"],
                        map1["Instuctions"],
                        map1["MealThumb"],
                        map1["Tags"],
                        map1["Youtube"],
                        ingredientString1.toString(),
                        measureString1.toString(),
                        map1["Source"],
                        map1["ImageSource"],
                        map1["CreativeCommonsConfirmed"],
                        map1["dateModified"]
                    )

                    val meal2 = MealDBEntity(
                        "2",
                        map2["Meal"].toString(),
                        map2["DrinkAlternate"],
                        map2["Category"],
                        map2["Area"],
                        map2["Instructions"],
                        map2["MealThumb"],
                        map2["Tags"],
                        map2["Youtube"],
                        ingredientString2.toString(),
                        measureString2.toString(),
                        map2["Source"],
                        map2["ImageSource"],
                        map2["CreativeCommonsConfirmed"],
                        map2["dateModified"]
                    )

                    val meal3 = MealDBEntity(
                        "3",
                        map3["Meal"].toString(),
                        map3["DrinkAlternate"],
                        map3["Category"],
                        map3["Area"],
                        map3["Instructions"],
                        map3["MealThumb"],
                        map3["Tags"],
                        map3["Youtube"],
                        ingredientString3.toString(),
                        measureString3.toString(),
                        map3["Source"],
                        map3["ImageSource"],
                        map3["CreativeCommonsConfirmed"],
                        map3["dateModified"]
                    )

                    val meal4 = MealDBEntity(
                        "4",
                        map4["Meal"].toString(),
                        map4["DrinkAlternate"],
                        map4["Category"],
                        map4["Area"],
                        map4["Instructions"],
                        map4["MealThumb"],
                        map4["Tags"],
                        map4["Youtube"],
                        ingredientString4.toString(),
                        measureString4.toString(),
                        map4["Source"],
                        map4["ImageSource"],
                        map4["CreativeCommonsConfirmed"],
                        map4["dateModified"]
                    )

                    //Insert to database
                    dbDAO.insertAll(meal1, meal2, meal3, meal4)
                }
            }
        }

        //Button to search meals by ingredient
        searchMeal.setOnClickListener {
            //Checking for internet connectivity
            if(isConnected()) {
                // Intent to start the next activity
                val searchIntent = Intent(this, SearchMeal::class.java)
                startActivity(searchIntent)
            }
            else{
                //Alert user to connect to internet
                Toast.makeText(
                    this@MainActivity,
                    "Please, Connect to the Internet!!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        //Button to search meals from database
        databaseSearchMeal.setOnClickListener {
            //Checking for internet connectivity
            if(isConnected()) {
                // Intent to start the next activity
                val databaseSearch = Intent(this, SearchMealDatabase::class.java)
                startActivity(databaseSearch)
            }
            else{
                //Alert user to connect to internet
                Toast.makeText(
                    this@MainActivity,
                    "Please, Connect to the Internet!!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        //Button to search from web directly
        searchWeb.setOnClickListener {
            //Checking internet connectivity
            if (isConnected()) {
                // Creating a dialog builder to allow user search a meal
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("Search Meal")

                //Adding the editText
                val searchName = EditText(this)
                dialogBuilder.setView(searchName)

                //Dialog box positive button
                dialogBuilder.setPositiveButton("Search") { _: DialogInterface, _: Int ->

                    //Api url link to search meal by name
                    val urlStringMeal = "https://www.themealdb.com/api/json/v1/1/search.php?s="

                    //Coroutine
                    runBlocking {
                        launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    val mealStringBuilder = StringBuilder()
                                    //Creating the url with the user input
                                    val userIngredientUrl =
                                        urlStringMeal + searchName.text.toString()
                                            .replace(" ", "%20")
                                    Log.d("MyLink", userIngredientUrl)
                                    //Conecting to api with the url created
                                    val urlIngredient = URL(userIngredientUrl)
                                    val ingredientConnection: HttpURLConnection =
                                        urlIngredient.openConnection() as HttpsURLConnection
                                    //Reading the api response and storing it to string builder
                                    val reader =
                                        BufferedReader(InputStreamReader(ingredientConnection.inputStream))
                                    var eachLine: String? = reader.readLine()
                                    while (eachLine != null) {
                                        mealStringBuilder.append(eachLine + "\n")
                                        eachLine = reader.readLine()
                                        Log.d("Mystring", mealStringBuilder.toString())
                                    }
                                    //After reading passing the api response to a function
                                    mealDetail = parseJsonIngredient(mealStringBuilder)
                                    //Display the string on the textview
                                    displayDetail.text = mealDetail
                                }
                                Log.d("Sucess", searchName.text.toString())
                            }
                            //Catch any exception that occurs when user types
                            catch (e: Exception) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Type The Correct Meal Name \n Else, Please Try Again Later",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    //Show thw popUpWindow with the inflated layout
                    popUpWindow.showAtLocation(detailLayout, Gravity.CENTER, 0, 0)

                    //Close the pop up window if the button in inflated layout pressed
                    closeBtn.setOnClickListener {
                        popUpWindow.dismiss()
                    }
                }
                //Show the dialog box
                dialogBuilder.show()
            }
            //If no internet connection inform user
            else{
                Toast.makeText(
                    this@MainActivity,
                    "Please, Connect to the Internet!!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    //Check whether there is active internet connection
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

    //The function to get all the meals from the response
    private fun parseJsonIngredient(mealStringBuilder: StringBuilder): String {
        //Creating a json object
        val json = JSONObject(mealStringBuilder.toString())
        Log.d("JSON",json.toString())

        //Creating an json array of the meals in json object
        val jsonArray: JSONArray = json.getJSONArray("meals")
        Log.d("ArraySize",jsonArray.length().toString())

        //For all the meals
        for(i in 0 until jsonArray.length()){
            Log.d("Length",jsonArray.length().toString())
            //Change each array index meal as an json object
            val meal: JSONObject = jsonArray[i] as JSONObject
            //From the json object get the idMeal of the meals
            val mealId = meal["idMeal"] as String
            //if the string builder does not contain the meal id then add the meal
            if (!mealStringBuilder.contains(mealId)) {
                mealStringBuilder.append(meal.toString())
                mealStringBuilder.append("\n\n")
            }
            Log.d("MealNameMEAL",mealStringBuilder.toString())
        }
        //return back the string with all meals
        return mealStringBuilder.toString()
    }



    //onPause dismissing the pop-up window to avoid leak
    override fun onPause() {
        super.onPause()
        popUpWindow.dismiss()
    }
}