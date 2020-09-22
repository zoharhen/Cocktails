package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktails.R


class UserItemLevel2 : AppCompatActivity() {
    private lateinit var ingredientTableRow: TableRow
    private lateinit var ingredientTableLayout: TableLayout
    private lateinit var preparationTableRow: TableRow
    private lateinit var preparationTableLayout: TableLayout
    private val REQUEST_CODE_SELECT_INGREDIENTS = 1
    private var counterIngredientRowTable: Int = 0
    private var counterPreparationRowTable: Int = 0

    //    private  var preparationList:ArrayList<String> = arrayListOf()
    private val NEW_VALUE = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level2)
        initIngredientsTable()
        initPreparationTable()
        //todo
        //initToolBar()
    }

    //todo
//    private fun initToolBar() {
//    }

    @SuppressLint("ResourceAsColor")
    private fun initIngredientsTable() {
        ingredientTableLayout = findViewById(R.id.ingredient_table)
        ingredientTableRow = findViewById(R.id.table_row)
        counterIngredientRowTable = -1
        ingredientTableLayout.setColumnStretchable(0, true)
        ingredientTableLayout.setColumnStretchable(1, true)
        ingredientTableLayout.setColumnStretchable(2, true)
        ingredientTableLayout.setColumnStretchable(3, true)

        val addRowButton: View = findViewById(R.id.adding_new_row)
        addRowButton.setOnClickListener {
            startSelectIngredient()
        }

    }

    private fun initPreparationTable() {
        preparationTableLayout = findViewById(R.id.preparation_table)
        preparationTableRow = findViewById(R.id.table_row_preparation)
        counterPreparationRowTable = -1
        ingredientTableLayout.setColumnStretchable(0, true)
        ingredientTableLayout.setColumnStretchable(1, true)

        val addPreparationRowButton: View = findViewById(R.id.adding_new_row_preparation)
        addPreparationRowButton.setOnClickListener {
            showAddStepDialog(this)
        }
    }

    private fun startSelectIngredient() {
        val intent = Intent(this, AddIngredientItem::class.java)
        startActivityForResult(intent, 1)
    }

    private fun addNewIngredientRow(quantityVal: String, unitVal: String, ingredientVal: String) {
        ingredientTableRow = TableRow(this)
        val scrNumRow = TextView(this)
        val scrQuantity = TextView(this)
        val scrUnit = TextView(this)
        val scrIngredient = TextView(this)

        scrNumRow.text = (counterIngredientRowTable + 1).toString()
        scrNumRow.gravity = Gravity.CENTER
        scrNumRow.textSize = 15F

        scrQuantity.text = quantityVal
        scrQuantity.gravity = Gravity.CENTER
        scrQuantity.textSize = 15F

        scrUnit.text = unitVal
        scrUnit.gravity = Gravity.CENTER
        scrUnit.textSize = 15F

        scrIngredient.text = ingredientVal
        scrIngredient.gravity = Gravity.CENTER
        scrIngredient.textSize = 15F

        ingredientTableRow.addView(scrNumRow)
        ingredientTableRow.addView(scrQuantity)
        ingredientTableRow.addView(scrUnit)
        ingredientTableRow.addView(scrIngredient)
        ingredientTableRow.setPadding(10, 10, 10, 10)
        ingredientTableRow.setBackgroundColor(resources.getColor(R.color.rowColor))
        ingredientTableLayout.addView(ingredientTableRow)
    }


    private fun showAddStepDialog(c: Context) {
        val inputText = EditText(c)
        inputText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        val dialog = AlertDialog.Builder(c)
            .setView(inputText)
            .setPositiveButton("Add") { _, _ ->
                val input = inputText.text.toString()
                addNewStepRow(input)
            }
            .setNegativeButton("Cancel", null)
            .setTitle("Add a new step")
        //todo for edit
//        if (index != NEW_VALUE) { //phone num clear or never insert
//            dialog.setMessage("current step: "+ preparationList[index])
//        }
        dialog.create()
        dialog.show()
    }

    private fun addNewStepRow(stepInput: String) {
        preparationTableRow = TableRow(this)
        val scrNumRow = TextView(this)
        val scrStep = TextView(this)

        scrNumRow.text = (counterPreparationRowTable + 1).toString()
        scrNumRow.gravity = Gravity.CENTER
        scrNumRow.textSize = 15F

        scrStep.text = stepInput
        scrStep.textSize = 15F

        val stepParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT, 1f
        )
        preparationTableRow.addView(scrNumRow)
//        scrStep.layoutParams(param)
        preparationTableRow.addView(scrStep, stepParams)


        preparationTableRow.setPadding(10, 10, 10, 10)
        preparationTableRow.setBackgroundColor(resources.getColor(R.color.rowColor))

        preparationTableLayout.addView(preparationTableRow)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_INGREDIENTS && resultCode == Activity.RESULT_OK && data != null) {
            val quantityVal = data.getStringExtra(UNIT_KEY)
            val unitVal = data.getStringExtra(QUANTITY_KEY)
            val ingredientVal = data.getStringExtra(INGREDIENT_KEY)
            if (!quantityVal.isNullOrEmpty() && !unitVal.isNullOrEmpty() && !ingredientVal.isNullOrEmpty()) {
                addNewIngredientRow(quantityVal, unitVal, ingredientVal)
                counterIngredientRowTable += 1
            }
        }
    }
}

