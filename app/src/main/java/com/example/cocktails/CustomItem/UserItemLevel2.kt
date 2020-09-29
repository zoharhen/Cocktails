package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TableRow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import com.example.cocktails.R
import kotlinx.android.synthetic.main.activity_user_item_level1.stepsView
import kotlinx.android.synthetic.main.activity_user_item_level2.*


data class StepItem(var stemNum: Int, val step: String)

const val INGREDIENT_LIST_STR_KEY= "ingredients_List_String"
const val PREPARATION_LIST_STR_KEY= "preparation_List_String"

class UserItemLevel2 : AppCompatActivity() {
    private lateinit var ingredientsValList: ArrayList<IngredientItem>
    private lateinit var preparationValList: ArrayList<StepItem>
    private lateinit var ingredientTableRow: TableRow
    private lateinit var preparationTableRow: TableRow

    private val NUM_OF_DEFUALT_ROWS = 2
    private val REQUEST_CODE_ADD_INGREDIENT = 1
    private val REQUEST_CODE_EDIT_INGREDIENT = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level2)

        initToolBar()
        initView()
        initIngredientsTable()
        initPreparationTable()
    }

    private fun initView() {

        stepsView.setLabels(arrayOf("", "", ""))
            .setBarColorIndicator(resources.getColor(R.color.material_blue_grey_800))
            .setProgressColorIndicator(resources.getColor(R.color.stepBg))
            .setLabelColorIndicator(resources.getColor(R.color.stepBg))
            .setCompletedPosition(1)
            .drawView()

        findViewById<ImageButton>(R.id.nextButton).setOnClickListener {
            if (checkedValidationLevel2()) {
                openLevel3Activity()
            }
        }

        findViewById<ImageButton>(R.id.prevButton).setOnClickListener {
            onBackPressed()
        }
    }

    private fun initToolBar() {
        supportActionBar?.title = getString(R.string.title_user_item)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    //####### Ingredients #######
    @SuppressLint("ResourceAsColor")
    private fun initIngredientsTable() {
        ingredientsValList = ArrayList()
        ingredientTableRow = findViewById(R.id.example_ingredient_table_row)

        //todo check for correct Stretch
        ingredient_table.setColumnStretchable(0, true)
        ingredient_table.setColumnStretchable(1, true)
        ingredient_table.setColumnStretchable(2, true)
        ingredient_table.setColumnStretchable(3, true)

        val addRowButton: View = findViewById(R.id.adding_new_row)
        addRowButton.setOnClickListener {
            startSelectIngredient()
        }

    }

    private fun initPreparationTable() {
        preparationValList = ArrayList()
        preparationTableRow = findViewById(R.id.table_row_preparation)

        preparation_table.setColumnStretchable(0, true)
        preparation_table.setColumnStretchable(1, true)

        val addPreparationRowButton: View = findViewById(R.id.adding_new_row_preparation)
        addPreparationRowButton.setOnClickListener {
            showAddStepDialog(this)
        }
    }

    private fun startSelectIngredient() {
        val intent = Intent(this, AddIngredientItem::class.java)
        startActivityForResult(intent, REQUEST_CODE_ADD_INGREDIENT)
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
        val scrQuantity = TextView(this)
        val scrUnit = TextView(this)
        val scrIngredient = TextView(this)

        var rowNum = ingredientsValList.size
        if (ingredientNum != null) {
            rowNum = ingredientNum
        }
        scrNumRow.text = (rowNum + 1).toString()
        scrNumRow.gravity = Gravity.CENTER
        scrNumRow.setTextAppearance(this, R.style.TableContentText)

        scrQuantity.text = quantityVal
        scrQuantity.gravity = Gravity.CENTER
        TextViewCompat.setTextAppearance(scrQuantity, R.style.TableContentText)

        scrUnit.text = unitVal
        scrUnit.gravity = Gravity.CENTER
//        scrUnit.setTextAppearance(R.style.TableContentText)

        scrIngredient.text = ingredientVal
        scrIngredient.gravity = Gravity.CENTER
//        scrIngredient.setTextAppearance(R.style.TableContentText)

        val params = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT, 1f
        )
        ingredientTableRow.addView(scrNumRow)
        ingredientTableRow.addView(scrQuantity, params)
        ingredientTableRow.addView(scrUnit)
        ingredientTableRow.addView(scrIngredient, params)
        ingredientTableRow.setPadding(10, 10, 10, 10)
        ingredientTableRow.setBackgroundColor(resources.getColor(R.color.colorAccent))

        ingredientTableRow.setOnClickListener {
            editIngredient(quantityVal, unitVal, ingredientVal, rowNum)
        }
        ingredientTableRow.setOnLongClickListener {
            showDelItemDialog(rowNum, false)
            return@setOnLongClickListener true
        }

        if (isNewIngredientItem) {
            val ingredientItem = IngredientItem(quantityVal, unitVal, ingredientVal, rowNum)
            ingredientsValList.add(rowNum, ingredientItem)
        }

        ingredient_table.addView(ingredientTableRow)
        example_ingredient_table_row.visibility = View.GONE
    }

    private fun updateIngredientList() {
        while (ingredient_table.childCount > NUM_OF_DEFUALT_ROWS)
            ingredient_table.removeView(ingredient_table.getChildAt(ingredient_table.childCount - 1))

        for (i in 0 until ingredientsValList.size) {
            ingredientsValList[i].ingredientNum = i //todo check
            addNewIngredientRow(
                ingredientsValList[i].quantity,
                ingredientsValList[i].unit,
                ingredientsValList[i].ingredient, false, ingredientsValList[i].ingredientNum
            )
        }
    }

    private fun editIngredient(
        quantityVal: String,
        unitVal: String,
        ingredientVal: String,
        rowNum: Int
    ) {
        val ingredientsIntent = Intent(this, AddIngredientItem::class.java)
        ingredientsIntent.putExtra(CURRENT_QUANTITY_KEY, quantityVal)
        ingredientsIntent.putExtra(CURRENT_UNIT_KEY, unitVal)
        ingredientsIntent.putExtra(CURRENT_INGREDIENT_KEY, ingredientVal)
        ingredientsIntent.putExtra(CURRENT_ROW_NUM_KEY, rowNum)

        //todo requestCode
        startActivityForResult(ingredientsIntent, REQUEST_CODE_EDIT_INGREDIENT)
    }

    private fun delIngredient(index: Int) {
        ingredientsValList.removeAt(index)
        updateIngredientList()
        if(ingredientsValList.isEmpty()){
            example_ingredient_table_row.visibility = View.VISIBLE
        }

    }

    private fun ingredientValidation(): Boolean {
        if (ingredientsValList.size > 0) {//todo check init size list
            ingredients_level2_error.visibility = View.GONE
            return true
        }
        ingredients_level2_error.visibility = View.VISIBLE
        return false
    }

    //####### Preparation #######
    private fun addNewStepRow(stepInput: String, stepNum: Int?, isNewStepItem: Boolean) {
        preparationTableRow = TableRow(this)
        val scrNumRow = TextView(this)
        val scrStep = TextView(this)

        var rowNum = preparationValList.size
        if (stepNum != null) {
            rowNum = stepNum
        }

        scrNumRow.text = (rowNum + 1).toString()
        scrNumRow.gravity = Gravity.CENTER
//        scrNumRow.setTextAppearance(R.style.TableContentText)

        scrStep.text = stepInput
//        scrStep.setTextAppearance(R.style.TableContentText)

        val stepParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT, 1f
        )
        preparationTableRow.addView(scrNumRow)
        preparationTableRow.addView(scrStep, stepParams)
        preparationTableRow.setPadding(10, 10, 10, 10)
        preparationTableRow.setBackgroundColor(resources.getColor(R.color.colorAccent))
        preparationTableRow.setOnClickListener {
            showUpdateStepDialog(this, stepInput, rowNum)
        }
        preparationTableRow.setOnLongClickListener {
            showDelItemDialog(rowNum, true)
            return@setOnLongClickListener true
        }
        if (isNewStepItem) {
            val stepItem = StepItem(rowNum, stepInput)
            preparationValList.add(rowNum, stepItem)
        }
        preparation_table.addView(preparationTableRow)
        example_step_table_row.visibility = View.GONE
    }

    private fun delPreparation(index: Int) {
        preparationValList.removeAt(index)
        updatePreparationList()
        if(preparationValList.isEmpty()){
            example_step_table_row.visibility = View.VISIBLE
        }
    }

    private fun showAddStepDialog(c: Context) {
        val inputText = EditText(c)
        inputText.setRawInputType(InputType.TYPE_CLASS_TEXT)
        val dialog = AlertDialog.Builder(c)
            .setView(inputText)
            .setPositiveButton("Add") { _, _ ->
                val input = inputText.text.toString()
                addNewStepRow(input, null, true)
            }
            .setNeutralButton("Cancel", null)
            .setTitle("Add a new step")
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
        dialog.create()
        dialog.show()
    }

    private fun updateStep(input: String, stepNum: Int) {
        preparationValList[stepNum] = StepItem(stepNum, input)
        updatePreparationList()
    }

    private fun updatePreparationList() {
        while (preparation_table.childCount > 2)
            preparation_table.removeView(
                preparation_table.getChildAt(
                    preparation_table.childCount - 1
                )
            )
        for (i in 0 until preparationValList.size) {
            preparationValList[i].stemNum = i //todo
            addNewStepRow(preparationValList[i].step, preparationValList[i].stemNum, false)
        }
    }

    private fun preparationValidation(): Boolean {
        if (preparationValList.size > 0) {//todo check init size list
            preparation_error.visibility = View.GONE
            return true
        }
        preparation_error.visibility = View.VISIBLE
        return false
    }

    private fun checkedValidationLevel2(): Boolean {
        val ingredientIsValid = ingredientValidation()
        val preparationIsValid = preparationValidation()
        return (ingredientIsValid && preparationIsValid)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val quantityVal = data.getStringExtra(QUANTITY_KEY)
            val unitVal = data.getStringExtra(UNIT_KEY)
            val ingredientVal = data.getStringExtra(INGREDIENT_KEY)

            if (requestCode == REQUEST_CODE_ADD_INGREDIENT) {
                if (!quantityVal.isNullOrEmpty() && !unitVal.isNullOrEmpty() && !ingredientVal.isNullOrEmpty()) {
                    addNewIngredientRow(quantityVal, unitVal, ingredientVal, true, null)
                }
            } else if (requestCode == REQUEST_CODE_EDIT_INGREDIENT) {
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

    private fun showDelItemDialog(
        index: Int,
        isPreparation: Boolean
    ): View.OnLongClickListener? {
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
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

    private  fun getIngredientsListStr():ArrayList<String>{
        val ingredientsStr=ArrayList<String>()
        for (i in 0 until ingredientsValList.size){
            ingredientsStr.add(ingredientsValList[i].getIngredientStrItem())
        }
        return ingredientsStr
    }

    private  fun getPreparationListStr():ArrayList<String>{
        val preparationStr=ArrayList<String>()
        for (i in 0 until preparationValList.size){
            preparationStr.add(preparationValList[i].step)
        }
        return preparationStr
    }

    private fun openLevel3Activity() {
        val intentLevel3 = Intent(this, UserItemLevel3::class.java)
        //todo check
        //args from level 1
        val cocktailName= intent.getStringExtra(COCKTAIL_NAME_KEY)
        val category = intent.getStringExtra(CATEGORY_KEY)
        val iconUri = intent.getStringExtra(ICON_KEY)
        val uploadImgStr = intent.getStringExtra(UPLOAD_IMG_KEY)
        val rotate:Float=intent.getFloatExtra(ROTATE_UPLOAD_IMG_KEY,0F)

        intentLevel3.putExtra(COCKTAIL_NAME_KEY, cocktailName)
        intentLevel3.putExtra(CATEGORY_KEY, category)
        intentLevel3.putExtra(ICON_KEY, iconUri)
        intentLevel3.putExtra(UPLOAD_IMG_KEY, uploadImgStr)
        intentLevel3.putExtra(ROTATE_UPLOAD_IMG_KEY, rotate)

        intentLevel3.putStringArrayListExtra(INGREDIENT_LIST_STR_KEY, getIngredientsListStr())
        intentLevel3.putStringArrayListExtra(PREPARATION_LIST_STR_KEY, getPreparationListStr())

        startActivity(intentLevel3)
    }
}

