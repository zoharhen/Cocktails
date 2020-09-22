package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktails.CustomItem.insertIngredients.SelectUnit
import com.example.cocktails.CustomItem.insertIngredients.UNIT_VAL_KEY
import com.example.cocktails.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_select_ingredient.*
import java.util.*
import kotlin.collections.ArrayList

val UNIT_KEY = "quantity"
val INGREDIENT_KEY = "ingredient"
val QUANTITY_KEY = "unit"
val CURRENT_UNIT_KEY="current_unit_key"

class SelectIngredient : AppCompatActivity() {
    private lateinit var mUnit: AutoCompleteTextView
    private lateinit var mQuantity: TextInputLayout
    private lateinit var mIngredient: AutoCompleteTextView
    private lateinit var mIngredientNew: TextInputLayout
    private lateinit var mUnitList: ArrayList<String>
    private lateinit var mIngredientList: ArrayList<String>
    val ERROR_MSG_INGREDIENT="This ingredient exist in the collection."
    val ERROR_MSG_EMPTY_INGREDIENT= "error - choose ingredient."
    val ERROR_MSG_EMPTY_UNIT="error - choose quantity."
    val ERROR_MSG_QUANTITY = "error - only a integer or float is allow."
    private val REQUEST_CODE_UNIT=2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_ingredient)
        initView()
        initButtons()

    }

    @SuppressLint("CutPasteId")
    private fun initView() {
        mUnitList = ArrayList(listOf(*resources.getStringArray(R.array.unitTypes_array)))
        val quantityAdapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, mUnitList)
        mUnit = findViewById(R.id.unit_dropdown)
        mUnit.setAdapter(quantityAdapter)

        findViewById<LinearLayout>(R.id.unit_layout).setOnClickListener{
            val unitIntent = Intent(this, SelectUnit::class.java)
            val unitTV : TextView = findViewById(R.id.unit_textView)
            unitIntent.putExtra(CURRENT_UNIT_KEY,unitTV.text)
            startActivityForResult(unitIntent, REQUEST_CODE_UNIT)
        }

        mQuantity = findViewById(R.id.textFieldQuantity)

        initIngredientView()

    }

    private fun initIngredientView() {
        mIngredientList = ArrayList(listOf(*resources.getStringArray(R.array.ingredients)))
        val ingredientAdapter =
            ArrayAdapter(this, R.layout.dropdown_menu_popup_item, mIngredientList)
        mIngredient = findViewById(R.id.ingredient_dropdown)
        mIngredient.setAdapter(ingredientAdapter)

        //set default in radio group
        radio_group_ingredient.check(R.id.radio_exist_ingredient)
        ingredient_TextInput.visibility = View.VISIBLE

        mIngredientNew = findViewById(R.id.textFieldNewIngredient)
        mIngredientNew.visibility = View.GONE
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_exist_ingredient ->
                    if (checked) {
                        // exist ingredient select
                        mIngredientNew.visibility = View.GONE
                        ingredient_TextInput.visibility = View.VISIBLE
                    }
                R.id.radio_new_ingredient ->
                    if (checked) {
                        ingredient_TextInput.visibility = View.GONE
                        mIngredientNew.visibility = View.VISIBLE

                    }
            }
        }
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
    }

    private fun validationNewIngredient(input: String?): Boolean {
        if (input == "" || input.isNullOrEmpty()) {
            mIngredientNew.error = EMPTY_FIELD_ERROR_MSG
            return false
        } else {
            val ingredientsListLower = mIngredientList.map { it.toLowerCase() }
            if (ingredientsListLower.contains(input.toLowerCase(Locale.ROOT))) {
                mIngredientNew.error =ERROR_MSG_INGREDIENT
                return false
            }
        }
        mIngredientNew.error = null
        return true
    }

    private fun ingredientValidation(): Boolean {
        return if (radio_exist_ingredient.isChecked) {
            validIngredientDropDown()
        } else { //radio_new_ingredient.isChecked()
            validationNewIngredient(mIngredientNew.editText?.text.toString())
        }
    }

    private fun returnResult() {
        val intentBack = Intent()
        intentBack.putExtra(UNIT_KEY, mUnit.text.toString())
        intentBack.putExtra(QUANTITY_KEY, getQuantity())
        intentBack.putExtra(INGREDIENT_KEY, getIngredientVal())
        setResult(RESULT_OK, intentBack)
        finish()
    }

    private fun checkValidation(): Boolean {
        val unitIsValid = validUnit()
        val quantityIsValid = validQuantity()
        val ingredientIsValid = ingredientValidation()
        return (unitIsValid && quantityIsValid && ingredientIsValid)
    }

    private fun validUnit(): Boolean {
        val value = mUnit.text
        if (value.isNullOrEmpty()) {
            mUnit.error = ERROR_MSG_EMPTY_UNIT
            return false
        }
        mUnit.error = null
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

    private fun validIngredientDropDown(): Boolean {
        val id = mIngredient.text
        if (id.isNullOrEmpty()) {
            mIngredient.error = ERROR_MSG_EMPTY_INGREDIENT
            return false
        }
        mIngredient.error = null
        return true
    }

    private fun getQuantity(): String {
        // Get input text
        //todo trim
        return mQuantity.editText?.text.toString().trim()
    }

    private fun getIngredientVal():String{
        return if (radio_exist_ingredient.isChecked) {
            mIngredient.text.toString()
        } else{
            mIngredientNew.editText?.text.toString()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UNIT && resultCode == Activity.RESULT_OK && data != null) {
            val unitTV : TextView = findViewById(R.id.unit_textView)
            unitTV.text = data.getStringExtra(UNIT_VAL_KEY)
        }
    }

}

