package com.example.cocktails.CustomItem

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktails.Cocktail
import com.example.cocktails.ItemDetailsActivity
import com.example.cocktails.R
import kotlinx.android.synthetic.main.tabbed_item.view.*
import java.util.*


class UserItemLevel3 : AppCompatActivity() {
    val DEFAULT_CLIPART = "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level3)
        initButtons()
        initToolBar()
    }

    private fun initToolBar() {
        supportActionBar?.title = getString(R.string.title_user_item)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.create_button_menu -> {
                createCocktail()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createCocktail() {
        TODO("Not yet implemented")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_level3, menu)
        return true
    }

    private fun initButtons() {
        findViewById<Button>(R.id.preview_button).setOnClickListener {
            val cocktailName = intent.getStringExtra(COCKATIL_NAME_KEY)
            val category = intent.getStringExtra(CATEGORY_KEY)
            val iconUri = intent.getStringExtra(ICON_KEY)
            val uploadImgStr = intent.getStringExtra(UPLOAD_IMG_KEY)
            val rotation = intent.getFloatExtra(ROTATE_UPLOAD_IMG_KEY, 0F)
            var ingredients: ArrayList<String>? = null
            if (intent.getStringArrayListExtra(INGREDIENT_LIST_STR_KEY) != null) {
                ingredients = intent.getStringArrayListExtra(INGREDIENT_LIST_STR_KEY)
            }
            var steps: ArrayList<String>? = null
            if (intent.getStringArrayListExtra(PREPARATION_LIST_STR_KEY) != null) {
                steps = intent.getStringArrayListExtra(PREPARATION_LIST_STR_KEY)
            }
            val glass = "cocktail glass.sfb"//todo default need to change
            if (cocktailName.isNullOrEmpty() || category.isNullOrEmpty() || iconUri.isNullOrEmpty()
                || ingredients.isNullOrEmpty() || steps.isNullOrEmpty() ) {
                return@setOnClickListener
            }

            val cocktail = Cocktail(
                cocktailName, category, steps, ingredients, DEFAULT_CLIPART,
                uploadImgStr, true, glass, true, rotation
            )

            val intent = Intent(applicationContext, ItemDetailsActivity::class.java)
            intent.putExtra("cocktail", cocktail)
            startActivity(intent)
        }
//        findViewById<Button>(R.id.create_button).setOnClickListener {
//        }

    }


}