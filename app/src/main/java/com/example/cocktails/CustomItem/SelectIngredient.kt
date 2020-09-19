package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktails.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_select_ingredient.*
import kotlin.collections.ArrayList

val QUANTITY_KEY="quantity"
val INGREDIENT_KEY="ingredient"
val UNIT_KEY="unit"

class SelectIngredient : AppCompatActivity() {
    private lateinit var mQuantity: AutoCompleteTextView
    private lateinit var mUnit: TextInputLayout
    private lateinit var mIngredient: AutoCompleteTextView
    private lateinit var mQuantityList: ArrayList<String>
    private lateinit var mIngredientList: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_ingredient)
        initView()
    }

    @SuppressLint("CutPasteId")
    private fun initView() {
        mQuantityList = ArrayList(listOf(*resources.getStringArray(R.array.quantityTypes_array)))
        val quantityAdapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, mQuantityList)
        mQuantity = findViewById(R.id.quantity_dropdown)
        mQuantity.setAdapter(quantityAdapter)

        mUnit = findViewById(R.id.textFieldUnit)

        mIngredientList = ArrayList(listOf(*resources.getStringArray(R.array.ingredients)))
        val ingredientAdapter =
            ArrayAdapter(this, R.layout.dropdown_menu_popup_item, mIngredientList)
        mIngredient = findViewById(R.id.ingredient_dropdown)
        mIngredient.setAdapter(ingredientAdapter)

        done_button.setOnClickListener {
            if (checkValidation()) {
                returnResult()
            }
        }

        cancel_button.setOnClickListener {
            finish()
        }
    }

    private fun returnResult(){
        val intentBack= Intent()
        intentBack.putExtra(QUANTITY_KEY, mQuantity.text.toString())
        intentBack.putExtra(UNIT_KEY,getUnit())
        intentBack.putExtra(INGREDIENT_KEY,mIngredient.text.toString())
        setResult(RESULT_OK, intentBack)
        finish()
    }
    private fun checkValidation(): Boolean {
        val quantityIsValid = validQuantity()
        val unitIsValid = validUnit()
        val ingredientIsValid = validIngredient()
        return (quantityIsValid && unitIsValid && ingredientIsValid)
    }

    private fun validQuantity(): Boolean {
        val value = mQuantity.text
        if (value.isNullOrEmpty() ) {
            mQuantity.error = "error - choose quantity."
            return false
        }
        mQuantity.error = null
        return true
    }

    private fun validUnit(): Boolean {
        if (getUnit().isEmpty() || getUnit().length > 10) {
            textFieldUnit.error = "error - only a integer or float is allow."
            return false
        }
        textFieldUnit.error = null
        return true
    }

    private fun validIngredient(): Boolean {
        val id = mIngredient.text
        if (id.isNullOrEmpty() ) {
            mIngredient.error = "error - choose ingredient."
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
}

