package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktails.CustomItem.insertIngredients.*
import com.example.cocktails.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_add_ingredient_item.*
import java.util.*
import kotlin.collections.ArrayList

val ROW_NUM_KEY="row_num"
val UNIT_KEY = "quantity"
val INGREDIENT_KEY = "ingredient"
val QUANTITY_KEY = "unit"
val CURRENT_UNIT_KEY="current_unit_key"
val CURRENT_INGREDIENT_KEY="current_ingredient_key"
val CURRENT_QUANTITY_KEY="current_quantity_key"
val CURRENT_ROW_NUM_KEY="current_row_num_key"

data class IngredientItem(val quantity: String, val unit: String, val ingredient: String,
                    var ingredientNum: Int){
    fun getIngredientStrItem():String{
        return "$quantity $unit $ingredient"
    }
}

class AddIngredientItem : AppCompatActivity() {
    private lateinit var mUnit: TextView
    private lateinit var mQuantity: TextInputLayout
    private lateinit var mIngredient: TextView
    private lateinit var mIngredientNew: TextInputLayout
    private lateinit var mIngredientList: ArrayList<String>
    private var mRowNum =-1
    val ERROR_MSG_EMPTY_INGREDIENT= "Choose ingredient"
    val ERROR_MSG_EMPTY_UNIT="Choose quantity"
    val ERROR_MSG_QUANTITY = "Enter numbers only"
    private val REQUEST_CODE_UNIT=2
    private val REQUEST_CODE_INGREDIENT=3
    private val TITLE_ACTIVITY="INGREDIENTS"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_ingredient_item)
        initToolBar()
        initView()
        initButtons()

        if( intent.extras!=null) {
            findViewById<TextView>(R.id.quantity_TV).text= intent.extras!!.getString(CURRENT_QUANTITY_KEY)
            mUnit.text = intent.extras!!.getString(CURRENT_UNIT_KEY)
            mIngredient.text= intent.extras!!.getString(CURRENT_INGREDIENT_KEY)
            mRowNum= intent.extras!!.getInt(CURRENT_ROW_NUM_KEY)
        }
    }

    private fun initToolBar() {
        supportActionBar?.title = getString(R.string.ingredients_user)
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
        intentBack.putExtra(ROW_NUM_KEY,mRowNum)
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
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

}

