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
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.example.cocktails.CustomItem.CURRENT_UNIT_KEY
import com.example.cocktails.CustomItem.EMPTY_FIELD_ERROR_MSG
import com.example.cocktails.R
import java.util.*
import kotlin.collections.ArrayList

val INGREDIENT_VAL_KEY = "ingredient_val"

class SelectIngredient : AppCompatActivity() {
    private lateinit var mRadioGroup: RadioGroup
    private lateinit var mIngredientsList: ArrayList<String>
//     mNewIngredientIndex =mIngredientsList.size

    val ERROR_MSG_INGREDIENT="This ingredient exist in the collection."


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_ingredient)
        initToolBar()
        var curVal:String?=null
        if( intent.extras!=null) {
            curVal = intent.extras!!.getString(CURRENT_UNIT_KEY).toString()
        }
        createRadioButtons(curVal)
        initAddButton()
    }

    private fun initAddButton() {
        findViewById<Button>(R.id.add_ingredient_button_toolbar).setOnClickListener {
            showAddNewIngredientDialog(this)
        }
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
            .setNegativeButton("Cancel", null)
            .setTitle("Add a new ingredient")
        //todo for edit
//        if (index != NEW_VALUE) { //phone num clear or never insert
//            dialog.setMessage("current step: "+ preparationList[index])
//        }
        dialog.create()
        dialog.show()
    }

    private fun addNewRadioButton(newIngredient: String) {
        //todo check validation
        val radioButtonView = RadioButton(this, null, R.attr.radioButtonStyle)
        radioButtonView.text = newIngredient
        radioButtonView.setCircleColor(Color.parseColor("#1974D2"))
        mRadioGroup.clearCheck()
        radioButtonView.isChecked=true
        mIngredientsList.add(newIngredient)
        radioButtonView.id = mIngredientsList.size-1
        mRadioGroup.addView(radioButtonView)
    }

    private fun initToolBar(){
        val toolbar: Toolbar = findViewById<View>(R.id.ingredient_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
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
            if (curVal != null) {
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
        var itemVal = ""
        if (0 <= checkedItemId && checkedItemId < mIngredientsList.size) {
            itemVal = mIngredientsList[checkedItemId]
        }
        return itemVal
    }

    private fun userMsg(text:String){
        Toast.makeText(applicationContext,text, Toast.LENGTH_LONG).show();
    }


}