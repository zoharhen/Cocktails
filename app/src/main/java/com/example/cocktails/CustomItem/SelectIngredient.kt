package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktails.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_select_ingredient.*
import java.util.*
import kotlin.collections.ArrayList

val QUANTITY_KEY = "quantity"
val INGREDIENT_KEY = "ingredient"
val UNIT_KEY = "unit"

class SelectIngredient : AppCompatActivity() {
    private lateinit var mQuantity: AutoCompleteTextView
    private lateinit var mUnit: TextInputLayout
    private lateinit var mIngredient: AutoCompleteTextView
    private lateinit var mIngredientNew: TextInputLayout
    private lateinit var mQuantityList: ArrayList<String>
    private lateinit var mIngredientList: ArrayList<String>
    val ERROR_MSG_INGREDIENT="This ingredient exist in the collection."
    val ERROR_MSG_EMPTY_INGREDIENT= "error - choose ingredient."
    val ERROR_MSG_EMPTY_QUANTITY="error - choose quantity."
    val ERROR_MSG_UNIT = "error - only a integer or float is allow."


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_ingredient)
        initView()
        initButtons()

    }

    @SuppressLint("CutPasteId")
    private fun initView() {
        mQuantityList = ArrayList(listOf(*resources.getStringArray(R.array.quantityTypes_array)))
        val quantityAdapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, mQuantityList)
        mQuantity = findViewById(R.id.quantity_dropdown)
        mQuantity.setAdapter(quantityAdapter)

        mUnit = findViewById(R.id.textFieldUnit)

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
        intentBack.putExtra(QUANTITY_KEY, mQuantity.text.toString())
        intentBack.putExtra(UNIT_KEY, getUnit())
        intentBack.putExtra(INGREDIENT_KEY, getIngredientVal())
        setResult(RESULT_OK, intentBack)
        finish()
    }

    private fun checkValidation(): Boolean {
        val quantityIsValid = validQuantity()
        val unitIsValid = validUnit()
        val ingredientIsValid = ingredientValidation()
        return (quantityIsValid && unitIsValid && ingredientIsValid)
    }

    private fun validQuantity(): Boolean {
        val value = mQuantity.text
        if (value.isNullOrEmpty()) {
            mQuantity.error = ERROR_MSG_EMPTY_QUANTITY
            return false
        }
        mQuantity.error = null
        return true
    }

    private fun validUnit(): Boolean {
        if (getUnit().isEmpty() || getUnit().length > 10) {
            textFieldUnit.error = ERROR_MSG_UNIT
            return false
        }
        textFieldUnit.error = null
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

    private fun getUnit(): String {
        // Get input text
        //todo trim
        return mUnit.editText?.text.toString().trim()
    }

    private fun getIngredientVal():String{
        return if (radio_exist_ingredient.isChecked) {
            mIngredient.text.toString()
        } else{
            mIngredientNew.editText?.text.toString()
        }
    }

}

