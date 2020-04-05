package com.example.cocktails

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_scrolling.*


data class Cocktail(val name: String, val type: String, val glass: String, val image: Int)

class ScrollingActivity : AppCompatActivity() {

    private val items: List<Cocktail> = listOf(
        Cocktail("Mango tango", "Spring", "Margarita", R.drawable.mangotango),
        Cocktail("Mojito", "Fresh", "Highball", R.drawable.mojito),
        Cocktail("Mango tango", "Spring", "Margarita", R.drawable.mangotango),
        Cocktail("Mojito", "Fresh", "Highball", R.drawable.mojito),
        Cocktail("Mango tango", "Spring", "Margarita", R.drawable.mangotango),
        Cocktail("Mojito", "Fresh", "Highball", R.drawable.mojito),
        Cocktail("Mango tango", "Spring", "Margarita", R.drawable.mangotango),
        Cocktail("Mojito", "Fresh", "Highball", R.drawable.mojito),
        Cocktail("Mango tango", "Spring", "Margarita", R.drawable.mangotango),
        Cocktail("Mojito", "Fresh", "Highball", R.drawable.mojito),
        Cocktail("Mango tango", "Spring", "Margarita", R.drawable.mangotango))
    private var adapter: CocktailItemAdapter? = null
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerView)
        adapter = CocktailItemAdapter(this, items)
        initRecyclerview()
    }

    private fun initRecyclerview() {
        val columns = resources.getInteger(R.integer.gridColumnNum)
        recyclerView.layoutManager = GridLayoutManager(this, columns)
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}