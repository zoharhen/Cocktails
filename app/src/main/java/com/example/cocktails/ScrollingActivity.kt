package com.example.cocktails

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_scrolling.*


@Parcelize
data class Cocktail(val name: String, val type: String, val steps: Array<String>, val ingredients: Array<String>, val image: String): Parcelable

class ScrollingActivity : AppCompatActivity() {

    private var adapter: CocktailItemAdapter? = null
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
        recyclerView.adapter = adapter
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

        adapter = CocktailItemAdapter(this, items)

        adapter?.onItemClick = { cocktail ->
            val intent = Intent(applicationContext, ItemDetailsActivity::class.java)
            intent.putExtra("cocktail", cocktail)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
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