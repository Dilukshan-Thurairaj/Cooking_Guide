package com.example.cw2_meal


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SearchMealDatabase : AppCompatActivity() {

    //Recycler view
    lateinit var mealRecyclerView: RecyclerView
    //List of the MealDBEntity object
    lateinit var mealList: ArrayList<MealDBEntity>
    //Recycler view adapter
    lateinit var mealAdapter: MealRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_meal_database)

        //Hiding the top action bar
        supportActionBar?.hide()

        //Create Database
        val db = Room.databaseBuilder(this, MealDatabase::class.java, "mealDatabase").build()
        //Calling DAO
        val dbDAO = db.mealDao()


        //Edit text and search button
        val databaseEditText = findViewById<EditText>(R.id.databaseEditText)
        val searchDatabaseBtn = findViewById<Button>(R.id.retrieveSearchMeal)

        //Creating recycler view
        mealRecyclerView = findViewById(R.id.mealRecyclerView)
        mealRecyclerView.layoutManager = LinearLayoutManager(this)

        mealList = ArrayList()
        //Create view holders o=for the list
        mealAdapter = MealRecyclerViewAdapter(mealList)
        //Display the recycler view
        mealRecyclerView.adapter = mealAdapter

        //If there is saved instance
        if(savedInstanceState != null){
            //Get the parcelable meal list
            mealList = savedInstanceState.getParcelableArrayList("mealList")!!
            //Create the view that were there before configuration change
            mealAdapter = MealRecyclerViewAdapter(mealList)
            //Display the recycler view
            mealRecyclerView.adapter = mealAdapter
        }

        //Button to search the database
        searchDatabaseBtn.setOnClickListener {
            //Clearing the list, so next time button clicked new list created
            mealList.clear()
            //Check internet connection
            if (isConnected()) {
                //coroutine
                runBlocking {
                    launch {
                        //Get all the meals from the database, for each of the meal
                        for (i in dbDAO.getAllMeals()) {
                            launch {
                                //Split the ingredients and measure to an array
                                val splitIngredientArray = i.ingredient?.split(",")
                                val splitMeasureArray = i.measure?.split(",")
                                //List to store each ingredient and measure
                                val listOfIngredient = mutableListOf<String>()
                                val listOfMeasure = mutableListOf<String>()
                                if (splitIngredientArray != null) {
                                    //For each in the split array add it to the mutable list
                                    for (j in splitIngredientArray) {
                                        if (j != "") {
                                            listOfIngredient.add(j)
                                        }
                                    }
                                }
                                if (splitMeasureArray != null) {
                                    for (j in splitMeasureArray) {
                                        if (j != "") {
                                            listOfMeasure.add(j)
                                        }
                                    }
                                }

                                //Check whether there is any ingredient or meal name in the database that contains the text entered by user
                                if (listOfIngredient.toString().lowercase()
                                        .contains(databaseEditText.text.toString().lowercase()) ||
                                    i.mealName.lowercase()
                                        .contains(databaseEditText.text.toString().lowercase())
                                ) {
                                    //Add those meal objects to the list
                                    mealList.add(i)
                                    Log.d("MyResult", i.toString())
                                    Log.d("MyImage", i.mealThumb.toString())
                                }
                            }
                        }
                        //After all the meal object added to list create views in recycler view
                        mealAdapter = MealRecyclerViewAdapter(mealList)
                        //Display the recycler view
                        mealRecyclerView.adapter = mealAdapter
                    }
                }
            }
            //Notify user if no internet connection
            else{
                Toast.makeText(
                    this@SearchMealDatabase,
                    "Please, Connect to the Internet!!",
                    Toast.LENGTH_LONG
                ).show()
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

    //Save the instance when configuration change
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Save the parcelable meal list of MealDBEntity objects
        outState.putParcelableArrayList("mealList",mealList)
    }

}

