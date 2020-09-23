package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktails.CustomItem.insertIngredients.*
import com.example.cocktails.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_add_ingredient_item.*
import java.util.*
import kotlin.collections.ArrayList

val UNIT_KEY = "quantity"
val INGREDIENT_KEY = "ingredient"
val QUANTITY_KEY = "unit"
val CURRENT_UNIT_KEY="current_unit_key"
val CURRENT_INGREDIENT_KEY="current_ingredient_key"

class AddIngredientItem : AppCompatActivity() {
    private lateinit var mUnit: TextView
    private lateinit var mQuantity: TextInputLayout
    private lateinit var mIngredient: TextView
    private lateinit var mIngredientNew: TextInputLayout
    private lateinit var mIngredientList: ArrayList<String>
    val ERROR_MSG_EMPTY_INGREDIENT= "error - choose ingredient."
    val ERROR_MSG_EMPTY_UNIT="error - choose quantity."
    val ERROR_MSG_QUANTITY = "error - only a integer or float is allow."
    private val REQUEST_CODE_UNIT=2
    private val REQUEST_CODE_INGREDIENT=3
    private val TITLE_ACTIVITY="INGREDIENTS"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_ingredient_item)

        initToolBar()
        initView()
        initButtons()
    }

    private fun initToolBar() {
        supportActionBar?.title = TITLE_ACTIVITY
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    @SuppressLint("CutPasteId")
    private fun initView() {
        mQuantity = findViewById(R.id.textFieldQuantity)
        mUnit = findViewById(R.id.unit_textView)
        mIngredient = findViewById(R.id.ingredient_textView)

        initErrors()
    }

    private fun initErrors() {
        ingredient_error.visibility = View.GONE
        unit_error.visibility = View.GONE
    }


    private fun initButtons() {
        done_button.setOnClickListener {
            if (checkValidation()) {
                returnResult()
            }
        }

        cancel_button.setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.ingredient_layout).setOnClickListener{
            val ingredientIntent = Intent(this, SelectIngredient::class.java)
            ingredientIntent.putExtra(CURRENT_INGREDIENT_KEY,mIngredient.text)
            startActivityForResult(ingredientIntent, REQUEST_CODE_INGREDIENT)
        }

        findViewById<LinearLayout>(R.id.unit_layout).setOnClickListener{
            val unitIntent = Intent(this, SelectUnit::class.java)
            unitIntent.putExtra(CURRENT_UNIT_KEY,mUnit.text)
            startActivityForResult(unitIntent, REQUEST_CODE_UNIT)
        }
    }

    private fun validIngredientVal(): Boolean {
        val value = mIngredient.text
        if (value.isNullOrEmpty() || value==DEFAULT_INGREDIENT_VAL) {
            ingredient_error.visibility=View.VISIBLE
            return false
        }
        ingredient_error.visibility=View.GONE
        return true
    }

    private fun returnResult() {
        val intentBack = Intent()
        intentBack.putExtra(UNIT_KEY, mUnit.text)
        intentBack.putExtra(QUANTITY_KEY, getQuantity())
        intentBack.putExtra(INGREDIENT_KEY, mIngredient.text)
        setResult(RESULT_OK, intentBack)
        finish()
    }

    private fun checkValidation(): Boolean {
        val unitIsValid = validUnit()
        val quantityIsValid = validQuantity()
        val ingredientIsValid = validIngredientVal()
        return (unitIsValid && quantityIsValid && ingredientIsValid)
    }

    private fun validUnit(): Boolean {
        val value = mUnit.text
        if (value.isNullOrEmpty()) {
            unit_error.visibility= View.VISIBLE
            return false
        }
        unit_error.visibility= View.GONE
        return true
    }

    private fun validQuantity(): Boolean {
        if (getQuantity().isEmpty() || getQuantity().length > 10) {
            textFieldQuantity.error = ERROR_MSG_QUANTITY
            return false
        }
        textFieldQuantity.error = null
        return true
    }

    private fun getQuantity(): String {
        // Get input text
        //todo trim
        return mQuantity.editText?.text.toString().trim()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && data != null){
            if (requestCode == REQUEST_CODE_UNIT) {
                val unitTV : TextView = findViewById(R.id.unit_textView)
                unitTV.text = data.getStringExtra(UNIT_VAL_KEY)
            }
            else if (requestCode == REQUEST_CODE_INGREDIENT) {
                val ingredientTV : TextView = findViewById(R.id.ingredient_textView)
                if (!ingredientTV.text.isNullOrEmpty()) {
                    ingredientTV.text = data.getStringExtra(INGREDIENT_VAL_KEY)
                }
            }
        }
    }

}

