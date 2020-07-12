package com.example.cocktails

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_scrolling.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat


@Parcelize
data class Cocktail(val name: String, val type: String, val steps: Array<String>, val ingredients: Array<String>, val image: String): Parcelable

class ScrollingActivity : AppCompatActivity() {

    private var gridViewAdapter: CocktailItemAdapter? = null
//    lateinit var filtersAdapter: ArrayAdapter<*>
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)

        initAdapter()
        initRecyclerview()
    }

    private fun initRecyclerview() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, getColumnNum())
        recyclerView.adapter = gridViewAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() { })
    }

    private fun getColumnNum() : Int {
        //        val columns = resources.getInteger(R.integer.gridColumnNum)
        val screenWidthDp: Float = resources.displayMetrics.widthPixels / resources.displayMetrics.density
       return ((screenWidthDp / 202 + 0.5).toInt()) // +0.5 for correct rounding to int.
    }

    private fun initAdapter() {
        val jsonString = applicationContext.assets.open("predefined.json").bufferedReader().use { it.readText() }
        val listCocktailType = object : TypeToken<List<Cocktail>>() {}.type
        val items = Gson().fromJson<List<Cocktail>>(jsonString, listCocktailType)

        gridViewAdapter = CocktailItemAdapter(this, items)

        gridViewAdapter?.onItemClick = { cocktail ->
            val intent = Intent(applicationContext, ItemDetailsActivity::class.java)
            intent.putExtra("cocktail", cocktail)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        initSearchView(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun initSearchView(menu: Menu) {
        val searchView: SearchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.findViewById<AutoCompleteTextView>(R.id.search_src_text).threshold = 1
        val searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        searchPlate.hint = "Search"
        searchPlate.setHintTextColor(Color.parseColor("#D5D3D3"))
        searchPlate.setTextColor(Color.parseColor("#D5D3D3"))
        val searchPlateView: View = searchView.findViewById(androidx.appcompat.R.id.search_plate)
        searchPlateView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                gridViewAdapter?.filter?.filter(newText)
                return false
            }
        })
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_favorites -> true
            R.id.action_myCocktails -> true
            R.id.action_help -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}