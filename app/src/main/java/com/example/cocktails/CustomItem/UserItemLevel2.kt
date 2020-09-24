package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktails.R

data class StepItem(var stemNum: Int, val step: String)

class UserItemLevel2 : AppCompatActivity() {
    private lateinit var ingredientsValList: ArrayList<IngredientItem>
    private lateinit var ingredientTableRow: TableRow
    private lateinit var ingredientTableLayout: TableLayout

    private lateinit var preparationValList: ArrayList<StepItem>
    private lateinit var preparationTableRow: TableRow
    private lateinit var preparationTableLayout: TableLayout

    private val REQUEST_CODE_SELECT_INGREDIENTS = 1
    private var counterPreparationRowTable: Int = 0

    //    private  var preparationList:ArrayList<String> = arrayListOf()
    private val NEW_VALUE = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level2)
        initIngredientsTable()
        initPreparationTable()
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
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("ResourceAsColor")
    private fun initIngredientsTable() {
        ingredientsValList = ArrayList()
        ingredientTableLayout = findViewById(R.id.ingredient_table)
        ingredientTableRow = findViewById(R.id.example_ingredient_table_row)
//        counterIngredientRowTable = -1
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
        preparationValList = ArrayList()
        preparationTableLayout = findViewById(R.id.preparation_table)
        preparationTableRow = findViewById(R.id.table_row_preparation)
//        counterPreparationRowTable = -1
        preparationTableLayout.setColumnStretchable(0, true)
        preparationTableLayout.setColumnStretchable(1, true)

        val addPreparationRowButton: View = findViewById(R.id.adding_new_row_preparation)
        addPreparationRowButton.setOnClickListener {
            showAddStepDialog(this)
        }
    }

    private fun startSelectIngredient() {
        val intent = Intent(this, AddIngredientItem::class.java)
        startActivityForResult(intent, 1)
    }

    private fun addNewIngredientRow(
        quantityVal: String,
        unitVal: String,
        ingredientVal: String,
        isNewIngredientItem: Boolean,
        ingredientNum: Int?
    ) {
        ingredientTableRow = TableRow(this)
        val scrNumRow = TextView(this)
//        scrNumRow.id = R.id.id_row_num
        val scrQuantity = TextView(this)
        val scrUnit = TextView(this)
        val scrIngredient = TextView(this)

        var rowNum = ingredientsValList.size
        if (ingredientNum != null) {
            rowNum = ingredientNum
        }
        scrNumRow.text = (rowNum).toString()
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
        ingredientTableRow.setOnClickListener {
            val ingredientsIntent = Intent(this, AddIngredientItem::class.java)
            ingredientsIntent.putExtra(CURRENT_QUANTITY_KEY, quantityVal)
            ingredientsIntent.putExtra(CURRENT_UNIT_KEY, unitVal)
            ingredientsIntent.putExtra(CURRENT_INGREDIENT_KEY, ingredientVal)
            ingredientsIntent.putExtra(CURRENT_ROW_NUM_KEY, rowNum)

            //todo requestCode
            startActivityForResult(ingredientsIntent, 10)

        }
        ingredientTableRow.setOnLongClickListener {
            showDelItemDialog(rowNum,false)
            return@setOnLongClickListener true
        }

        if (isNewIngredientItem) {
            val ingredientItem = IngredientItem(quantityVal, unitVal, ingredientVal, rowNum)
            ingredientsValList.add(rowNum, ingredientItem)
        }

        ingredientTableLayout.addView(ingredientTableRow)

    }


    //todo add this option
    private fun showDelItemDialog(
        index: Int,
        isPreparation: Boolean
    ): View.OnLongClickListener? {
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        // Set a message for alert dialog
        builder.setMessage("Are you sure you want to delete?")
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    if (isPreparation) {
                        delPreparation(index)
                    } else {
                        delIngredient(index)
                    }
                }
                //DialogInterface.BUTTON_NEGATIVE ->{}
            }
        }
        builder.setPositiveButton("YES", dialogClickListener)
        // Set the alert dialog negative/no button
        builder.setNegativeButton("NO", dialogClickListener)
        dialog = builder.create()
        dialog.show()
        return null//todo
    }

    private fun delPreparation(index: Int) {
        preparationValList.removeAt(index)
        updatePreparationList()
    }

    private fun delIngredient(index: Int) {
        ingredientsValList.removeAt(index)
        updateIngredientList()
    }


    private fun showAddStepDialog(c: Context) {
        val inputText = EditText(c)
        inputText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        val dialog = AlertDialog.Builder(c)
            .setView(inputText)
            .setPositiveButton("Add") { _, _ ->
                val input = inputText.text.toString()
                addNewStepRow(input, null,true)
            }
            .setNeutralButton("Cancel", null)
            .setTitle("Add a new step")
        //todo for edit
//        if (index != NEW_VALUE) { //phone num clear or never insert
//            dialog.setMessage("current step: "+ preparationList[index])
//        }
        dialog.create()
        dialog.show()
    }

    private fun showUpdateStepDialog(c: Context, step: String, stepNum: Int) {
        val inputText = EditText(c)
        inputText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        inputText.setText(step)
        val dialog = AlertDialog.Builder(c)
            .setView(inputText)
            .setPositiveButton("Update") { _, _ ->
                val input = inputText.text.toString()
                updateStep(input, stepNum)
            }
            .setNeutralButton("Cancel", null)
            .setTitle("Update step")
        //todo for edit
//        if (index != NEW_VALUE) { //phone num clear or never insert
//            dialog.setMessage("current step: "+ preparationList[index])
//        }
        dialog.create()
        dialog.show()
    }

    private fun updateStep(input: String, stepNum: Int) {
        preparationValList[stepNum] = StepItem(stepNum, input)
        updatePreparationList()
    }

    private fun updatePreparationList() {
        while (preparationTableLayout.childCount > 2)
            preparationTableLayout.removeView(
                preparationTableLayout.getChildAt(
                    preparationTableLayout.childCount - 1
                )
            )

        for (i in 0 until preparationValList.size) {
            preparationValList[i].stemNum = i //todo
            addNewStepRow(preparationValList[i].step, preparationValList[i].stemNum,false)
        }
    }

    private fun addNewStepRow(stepInput: String, stepNum: Int?,isNewStepItem: Boolean) {
        preparationTableRow = TableRow(this)
        val scrNumRow = TextView(this)
        val scrStep = TextView(this)

        var rowNum = preparationValList.size
        if (stepNum != null) {
            rowNum = stepNum
        }

        scrNumRow.text = rowNum.toString()
        scrNumRow.gravity = Gravity.CENTER
        scrNumRow.textSize = 15F

        scrStep.text = stepInput
        scrStep.textSize = 15F

        val stepParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT, 1f
        )
        preparationTableRow.addView(scrNumRow)
        preparationTableRow.addView(scrStep, stepParams)
        preparationTableRow.setPadding(10, 10, 10, 10)
        preparationTableRow.setBackgroundColor(resources.getColor(R.color.rowColor))
        preparationTableRow.setOnClickListener {
            showUpdateStepDialog(this, stepInput, rowNum)
        }
        preparationTableRow.setOnLongClickListener {
            showDelItemDialog(rowNum,true)
            return@setOnLongClickListener true
        }
        if (isNewStepItem) {
            val stepItem = StepItem(rowNum, stepInput)
            preparationValList.add(rowNum, stepItem)
        }
        preparationTableLayout.addView(preparationTableRow)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val quantityVal = data.getStringExtra(QUANTITY_KEY)
            val unitVal = data.getStringExtra(UNIT_KEY)
            val ingredientVal = data.getStringExtra(INGREDIENT_KEY)

            if (requestCode == REQUEST_CODE_SELECT_INGREDIENTS) {
                if (!quantityVal.isNullOrEmpty() && !unitVal.isNullOrEmpty() && !ingredientVal.isNullOrEmpty()) {
                    addNewIngredientRow(quantityVal, unitVal, ingredientVal, true, null)
//                    counterIngredientRowTable += 1
                }
            } else if (requestCode == 10) {
                val rowNum = data.getIntExtra(ROW_NUM_KEY, -1)
                if (rowNum == -1 || quantityVal == null || unitVal == null || ingredientVal == null) {
                    return
                }

                ingredientsValList[rowNum] =
                    IngredientItem(quantityVal, unitVal, ingredientVal, rowNum)
                updateIngredientList()
            }
        }

    }

    private fun updateIngredientList() {
        while (ingredientTableLayout.childCount > 2)
            ingredientTableLayout.removeView(ingredientTableLayout.getChildAt(ingredientTableLayout.childCount - 1))

        for (i in 0 until ingredientsValList.size) {
            ingredientsValList[i].ingredientNum = i //todo
            addNewIngredientRow(
                ingredientsValList[i].quantity,
                ingredientsValList[i].unit,
                ingredientsValList[i].ingredient, false, ingredientsValList[i].ingredientNum
            )
        }
    }


}

