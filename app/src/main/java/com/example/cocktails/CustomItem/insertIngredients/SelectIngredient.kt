package com.example.cocktails.CustomItem.insertIngredients

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.example.cocktails.CustomItem.CURRENT_INGREDIENT_KEY
import com.example.cocktails.CustomItem.EMPTY_FIELD_ERROR_MSG
import com.example.cocktails.R
import java.util.*
import kotlin.collections.ArrayList

val INGREDIENT_VAL_KEY = "ingredient_val"
val DEFAULT_INGREDIENT_VAL="None"

class SelectIngredient : AppCompatActivity() {
    private lateinit var mRadioGroup: RadioGroup
    private lateinit var mIngredientsList: ArrayList<String>
    private val ERROR_MSG_INGREDIENT="Ingredient already exists"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_ingredient)
        initToolBar()
        var curVal=  DEFAULT_INGREDIENT_VAL
        if( intent.extras!=null) {
            curVal = intent.extras!!.getString(CURRENT_INGREDIENT_KEY).toString()
        }
        createRadioButtons(curVal)
        initButtons()

    }

    private fun initButtons() {
        //init add button
        val addIngredientButton: View = findViewById(R.id.add_new_ingredient_button)
        addIngredientButton.setOnClickListener {
            showAddNewIngredientDialog(this)
        }
//        //init done button
//        findViewById<Button>(R.id.done_button_toolbar).setOnClickListener {
//            finish()
//        }
    }

    private fun validationNewIngredient(input: String?): Boolean {
        if (input == "" || input.isNullOrEmpty()) {
            userMsg(EMPTY_FIELD_ERROR_MSG)
            return false
        } else {
            val ingredientsListLower = mIngredientsList.map { it.toLowerCase(Locale.ROOT) }
            if (ingredientsListLower.contains(input.toLowerCase(Locale.ROOT))) {
                userMsg(ERROR_MSG_INGREDIENT)
                return false
            }
        }
        return true
    }

    private fun showAddNewIngredientDialog(c: Context) {
        val inputText = EditText(c)
        inputText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        val dialog = AlertDialog.Builder(c)
            .setView(inputText)
            .setPositiveButton("Add") { _, _ ->
                val input = inputText.text.toString()
                if(validationNewIngredient(input)) {
                    addNewRadioButton(input)
                }
            }
            .setNeutralButton("Cancel", null)
            .setTitle("Add a new ingredient")
        dialog.create()
        dialog.show()
    }

    @SuppressLint("ResourceType")
    private fun addNewRadioButton(newIngredient: String) {
        if(!validationNewIngredient(newIngredient)) return
        val radioButtonView = RadioButton(this, null, R.attr.radioButtonStyle)
        radioButtonView.text = newIngredient
        radioButtonView.setCircleColor(Color.parseColor("#1974D2"))
        mIngredientsList.add(newIngredient)
        radioButtonView.id = mIngredientsList.size -1
        mRadioGroup.clearCheck()
        radioButtonView.isChecked=true
        mRadioGroup.addView(radioButtonView)
    }

    private fun initToolBar(){
//        val toolbar: Toolbar = findViewById<View>(R.id.ingredient_toolbar) as Toolbar
//        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.ingredient_user)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_add_ingredient, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.done_button_menu -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun createRadioButtons(curVal:String?) {
        mRadioGroup = findViewById(R.id.ingredient_radio_group)
        mIngredientsList = ArrayList(listOf(*resources.getStringArray(R.array.ingredients)))
        var isNewIngredient=true
        for (i in 0 until mIngredientsList.size) {
            val radioButtonView = RadioButton(this, null, R.attr.radioButtonStyle)
            radioButtonView.text = mIngredientsList[i]
            radioButtonView.setCircleColor(Color.parseColor("#1974D2"))
            if(curVal!=null && radioButtonView.text ==curVal){
                radioButtonView.isChecked=true
                isNewIngredient=false
            }
            radioButtonView.id = i
            mRadioGroup.addView(radioButtonView)
        }
        if (isNewIngredient){
            if (curVal != null && curVal!=DEFAULT_INGREDIENT_VAL) {
                addNewRadioButton(curVal)
            }
        }
    }

    private fun RadioButton.setCircleColor(color: Int){
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked), // unchecked
                intArrayOf(android.R.attr.state_checked) // checked
            ), intArrayOf(
                Color.GRAY, // unchecked color
                color // checked color
            )
        )
        buttonTintList = colorStateList
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            buttonTintBlendMode = BlendMode.SRC_IN
        }else{
            buttonTintMode = PorterDuff.Mode.SRC_IN
        }
        invalidate()
    }

    @Override
    override fun finish() {
        val itemChecked = getItemChecked()
        val intentBack = Intent()
        intentBack.putExtra(INGREDIENT_VAL_KEY, itemChecked)
        setResult(RESULT_OK, intentBack)
        super.finish()
    }

    private fun getItemChecked(): String {
        val checkedItemId = mRadioGroup.checkedRadioButtonId
        var itemVal = DEFAULT_INGREDIENT_VAL
        if (0 <= checkedItemId && checkedItemId < mIngredientsList.size) {
            itemVal = mIngredientsList[checkedItemId]
        }
        return itemVal
    }

    private fun userMsg(text:String){
        Toast.makeText(applicationContext,text, Toast.LENGTH_LONG).show();
    }


}