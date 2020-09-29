package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktails.*
import kotlinx.android.synthetic.main.activity_user_item_level1.*
import java.util.*


class UserItemLevel3 : AppCompatActivity() {
    val DEFAULT_CLIPART = "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level3)
        initViews()
        initButtons()
        initToolBar()
    }

    private fun initViews() {
        stepsView.setLabels(arrayOf("", "", ""))
            .setBarColorIndicator(resources.getColor(R.color.material_blue_grey_800))
            .setProgressColorIndicator(resources.getColor(R.color.stepBg))
            .setLabelColorIndicator(resources.getColor(R.color.stepBg))
            .setCompletedPosition(2)
            .drawView()
    }

    private fun initToolBar() {
        supportActionBar?.title = getString(R.string.title_user_item)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

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
        val cocktail : Cocktail = getCocktail() ?: return
        val cocktailName=intent.getStringExtra(COCKTAIL_NAME_KEY)
        val cnt = (applicationContext as Cocktails)
        cnt.mCocktailsRef.document(cocktailName).set(cocktail)
            .addOnSuccessListener{ Log.i(
                "storage_new_cocktail",
                "OnSuccess: Cocktail Name: ${cocktailName}"
            );}
            .addOnFailureListener { Log.i(
                "storage_new_cocktail",
                "OnFailure: Cocktail Name: ${cocktailName}"
            );}
        cnt.addUserCocktail(cocktail)
        val intent = Intent(applicationContext, ScrollingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
//        cnt.mCocktailsRef.add(cocktail)
    }

//    fun addTodo(item: TodoItem) {
//        items[item.id] = item
//        val newItem: HashMap<String, Any> = hashMapOf("id" to item.id, "creationTimestamp" to item.creationTimestamp,
//            "editTimestamp" to item.editTimestamp, "content" to item.getContent(), "isDone" to item.getIsDone()) // todo: replace hashSet with item? add empty constructor to TodoItem class
//        fb.document(item.id).set(newItem)
//            .addOnSuccessListener{ Log.i("DataHolder_addTodo", "OnSuccess: ItemId: ${item.id}");}
//            .addOnFailureListener { Log.i("DataHolder_addTodo", "OnFailure: ItemId: ${item.id}");}
//        onListChangeListener?.onListChange()
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_level3, menu)
        return true
    }

    private fun initButtons() {
        findViewById<ImageButton>(R.id.prevButton).setOnClickListener {
            onBackPressed()
        }

        findViewById<Button>(R.id.preview_button).setOnClickListener {


            val cocktail : Cocktail = getCocktail() ?: return@setOnClickListener

            val intent = Intent(applicationContext, ItemDetailsActivity::class.java)
            intent.putExtra("cocktail", cocktail)
            startActivity(intent)
        }
//        findViewById<Button>(R.id.create_button).setOnClickListener {
//        }

    }

    @SuppressLint("LongLogTag")
    private fun getCocktail():Cocktail?{
        val cocktailName = intent.getStringExtra(COCKTAIL_NAME_KEY)
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
            return null
        }

        return Cocktail(
            cocktailName, category, steps, ingredients, DEFAULT_CLIPART,
            uploadImgStr, true, glass, true, rotation
        )
    }

}