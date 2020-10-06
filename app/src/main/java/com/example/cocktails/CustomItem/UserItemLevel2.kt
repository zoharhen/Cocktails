package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.cocktails.Cocktails
import com.example.cocktails.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_user_item_level2.*


data class StepItem(var stemNum: Int, val step: String)

const val INGREDIENT_LIST_JSON_STR_KEY= "ingredients_List_Json_String"
const val PREPARATION_LIST_JSON_STR_KEY= "preparation_List_Json_String"

class UserItemLevel2 : AppCompatActivity() {
    private lateinit var ingredientsValList: ArrayList<IngredientItem>
    private lateinit var preparationValList: ArrayList<StepItem>
    private lateinit var ingredientTableRow: TableRow
    private lateinit var preparationTableRow: TableRow

    private val NUM_OF_DEFUALT_ROWS = 2
    private val REQUEST_CODE_ADD_INGREDIENT = 1
    private val REQUEST_CODE_EDIT_INGREDIENT = 2
    private val INFO_DIALOG_TITLE=" Information"
    private val INFO_DIALOG_BODY="Press on item:\n- Short press in order to edit.\n- Long press in order to delete."

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level2)

        initToolBar()
        initButtons()
        initIngredientsTable()
        initPreparationTable()
        initForEditMode()
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

    override fun onBackPressed() {
        super.onBackPressed()
        saveListsToSP()
    }

    private fun saveListsToSP(){
        val c = (this.applicationContext as Cocktails)
        val gson = Gson()
        if(ingredientsValList.isNotEmpty()) {
            val ingredientsJson = gson.toJson(ingredientsValList)
            c.mUserInputs.edit().putString(INGREDIENT_LIST_JSON_STR_KEY, ingredientsJson).apply()
        }
        if(preparationValList.isNotEmpty()) {
            val preparationJson = gson.toJson(preparationValList)
            c.mUserInputs.edit().putString(PREPARATION_LIST_JSON_STR_KEY, preparationJson).apply()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initForEditMode() {
        val c = (this.applicationContext as Cocktails)
        val gson = Gson()
        val ingredientJson = c.mUserInputs.getString(INGREDIENT_LIST_JSON_STR_KEY, null)
        if(!ingredientJson.isNullOrEmpty()) {
            val objectList = gson.fromJson(ingredientJson, Array<IngredientItem>::class.java).asList()
            objectList.forEach { item -> addNewIngredientRow(item.quantity, item.unit,
                item.ingredient, true) }
        }
        val stepsJson = c.mUserInputs.getString(PREPARATION_LIST_JSON_STR_KEY, null)
        if(!stepsJson.isNullOrEmpty()) {
            val objectList = gson.fromJson(stepsJson, Array<StepItem>::class.java).asList()
            objectList.forEach { item -> addNewStepRow(item.step, true) }
        }

    }

    private fun initButtons(){
        findViewById<ImageButton>(R.id.nextButton).setOnClickListener {
            if (checkedValidationLevel2()) {
                openLevel3Activity()
            }
        }

        findViewById<ImageButton>(R.id.prevButton).setOnClickListener {
            onBackPressed()
        }

        info_ingredients_button.setOnClickListener{
            showDialogOnInfoPress()
        }

        info_steps_button.setOnClickListener{
            showDialogOnInfoPress()
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

        ingredient_table.setColumnStretchable(0, true)
        ingredient_table.setColumnStretchable(1, true)
        ingredient_table.setColumnStretchable(2, true)
        ingredient_table.setColumnStretchable(3, true)

        val addRowButton: View = findViewById(R.id.adding_new_row)
        addRowButton.setOnClickListener {
            startSelectIngredient()
        }

    }

    private fun startSelectIngredient() {
        val intent = Intent(this, AddIngredientItem::class.java)
        startActivityForResult(intent, REQUEST_CODE_ADD_INGREDIENT)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addNewIngredientRow(
        quantityVal: String,
        unitVal: String,
        ingredientVal: String,
        isNewIngredientItem: Boolean,
        ingredientNum: Int? = null
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
        setTableContentTextStyle(scrNumRow)

        scrQuantity.text = quantityVal
        scrQuantity.gravity = Gravity.CENTER
        setTableContentTextStyle(scrQuantity)

        scrUnit.text = unitVal
        scrUnit.gravity = Gravity.CENTER
        setTableContentTextStyle(scrUnit)

        scrIngredient.text = ingredientVal
        scrIngredient.gravity = Gravity.CENTER
        setTableContentTextStyle(scrIngredient)

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
        ingredients_level2_error.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateIngredientList() {
        while (ingredient_table.childCount > NUM_OF_DEFUALT_ROWS)
            ingredient_table.removeView(ingredient_table.getChildAt(ingredient_table.childCount - 1))

        for (i in 0 until ingredientsValList.size) {
            ingredientsValList[i].ingredientNum = i
            addNewIngredientRow(
                ingredientsValList[i].quantity,
                ingredientsValList[i].unit,
                ingredientsValList[i].ingredient, false, ingredientsValList[i].ingredientNum
            )
        }
    }

    private fun editIngredient(quantityVal: String, unitVal: String, ingredientVal: String,
        rowNum: Int) {
        val ingredientsIntent = Intent(this, AddIngredientItem::class.java)
        ingredientsIntent.putExtra(CURRENT_QUANTITY_KEY, quantityVal)
        ingredientsIntent.putExtra(CURRENT_UNIT_KEY, unitVal)
        ingredientsIntent.putExtra(CURRENT_INGREDIENT_KEY, ingredientVal)
        ingredientsIntent.putExtra(CURRENT_ROW_NUM_KEY, rowNum)

        startActivityForResult(ingredientsIntent, REQUEST_CODE_EDIT_INGREDIENT)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun delIngredient(index: Int) {
        ingredientsValList.removeAt(index)
        updateIngredientList()
        if(ingredientsValList.isEmpty()){
            example_ingredient_table_row.visibility = View.VISIBLE
        }

    }

    private fun ingredientValidation(): Boolean {
        if (ingredientsValList.size > 0) {
            ingredients_level2_error.visibility = View.GONE
            return true
        }
        ingredients_level2_error.visibility = View.VISIBLE
        return false
    }

    //####### Preparation #######
    @RequiresApi(Build.VERSION_CODES.M)
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

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun addNewStepRow(stepInput: String, isNewStepItem: Boolean, stepNum: Int? = null) {
        preparationTableRow = TableRow(this)
        val scrNumRow = TextView(this)
        val scrStep = TextView(this)

        var rowNum = preparationValList.size
        if (stepNum != null) {
            rowNum = stepNum
        }
        scrNumRow.text = (rowNum + 1).toString()
        scrNumRow.gravity= Gravity.CENTER
        setTableContentTextStyle(scrNumRow)

        scrStep.text = stepInput
        setTableContentTextStyle(scrStep)

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
        preparation_error.visibility = View.GONE

    }

    @SuppressLint("ResourceAsColor")
    fun setTableContentTextStyle(textView: TextView){
        val font: Typeface? = ResourcesCompat.getFont(this, R.font.raleway_light)
        textView.typeface = font
        textView.setTextColor(Color.BLACK)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F);
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun delPreparation(index: Int) {
        preparationValList.removeAt(index)
        updatePreparationList()
        if(preparationValList.isEmpty()){
            example_step_table_row.visibility = View.VISIBLE
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun showUpdateStepDialog(c: Context, step: String, stepNum: Int) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(40, 40, 40, 0)

        val textBox = EditText(c)
        textBox.setText(step)
        textBox.requestFocus()
        textBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18F);
        textBox.setTextColor(Color.BLACK)
        textBox.typeface = ResourcesCompat.getFont(c, R.font.raleway_light)
        textBox.setRawInputType(InputType.TYPE_CLASS_TEXT)

        val title=TextView(c)
        title.text = "Update step"
        setStyleDialogTitle(title,0,this)
        val paramsTitle = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        paramsTitle.setMargins(40, 40, 50, 10)

        layout.addView(title,paramsTitle)
        layout.addView(textBox, params)

        val dialog = AlertDialog.Builder(c)
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val input = textBox.text.toString()
                updateStep(input, stepNum)
            }
            .setNeutralButton("Cancel", null)
            .create()
        dialog.show()

        val font: Typeface? = ResourcesCompat.getFont(c, R.font.raleway_semibold)
        dialog.findViewById<Button>(android.R.id.button1).typeface = font
        dialog.findViewById<Button>(android.R.id.button3).typeface = font
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateStep(input: String, stepNum: Int) {
        preparationValList[stepNum] = StepItem(stepNum, input)
        updatePreparationList()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updatePreparationList() {
        while (preparation_table.childCount > 2)
            preparation_table.removeView(
                preparation_table.getChildAt(
                    preparation_table.childCount - 1
                )
            )
        for (i in 0 until preparationValList.size) {
            preparationValList[i].stemNum = i
            addNewStepRow(preparationValList[i].step, false, preparationValList[i].stemNum)
        }
    }

    private fun preparationValidation(): Boolean {
        if (preparationValList.size > 0) {
            preparation_error.visibility = View.GONE
            return true
        }
        preparation_error.visibility = View.VISIBLE
        return false
    }


    //##############validations#################
    private fun checkedValidationLevel2(): Boolean {
        val ingredientIsValid = ingredientValidation()
        val preparationIsValid = preparationValidation()
        return (ingredientIsValid && preparationIsValid)
    }

    //###########acivity#############
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val quantityVal = data.getStringExtra(QUANTITY_KEY)
            val unitVal = data.getStringExtra(UNIT_KEY)
            val ingredientVal = data.getStringExtra(INGREDIENT_KEY)

            if (requestCode == REQUEST_CODE_ADD_INGREDIENT) {
                if (!quantityVal.isNullOrEmpty() && !unitVal.isNullOrEmpty() && !ingredientVal.isNullOrEmpty()) {
                    addNewIngredientRow(quantityVal, unitVal, ingredientVal, true)
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

    private fun openLevel3Activity() {
        val intentLevel3 = Intent(this, UserItemLevel3::class.java)
        saveListsToSP()
        if(intent.getBooleanExtra(EDIT_MODE_KEY, false)) {
            intentLevel3.putExtra(EDIT_MODE_KEY, true)
        }
        startActivity(intentLevel3)
    }

    //################Dialogs################
    @RequiresApi(Build.VERSION_CODES.M)
    private fun showDelItemDialog(index: Int, isPreparation: Boolean): View.OnLongClickListener? {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(40, 40, 40, 0)

        val textBox = TextView(this)
        setStyleDialogBodyTV(textBox,this)
        textBox.text="Are you sure?"
        val title=TextView(this)
        setStyleDialogTitle(title,R.drawable.ic_warning_30,this)
        title.text=" Delete item"

        layout.addView(title,params)
        layout.addView(textBox, params)

        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton(YES) { _, _ ->
                    if (isPreparation) {
                        delPreparation(index)
                    } else {
                        delIngredient(index)
                    }
            }
            .setNegativeButton(NO, null)

        dialog = builder.create()
        dialog.show()
        val font: Typeface? =  ResourcesCompat.getFont(this, R.font.raleway_semibold)
        dialog.findViewById<Button>(android.R.id.button1).typeface = font
        dialog.findViewById<Button>(android.R.id.button2).typeface = font
        return null
    }

    private fun showDialogOnInfoPress() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(40, 30, 40, 0)

        val textBox = TextView(this)
        setStyleDialogBodyTV(textBox,this)
        textBox.text=INFO_DIALOG_BODY
        val title=TextView(this)
        setStyleDialogTitle(title,R.drawable.ic_info_24,this)
        title.text=" $INFO_DIALOG_TITLE"

        layout.addView(title,params)
        layout.addView(textBox, params)

        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->}
        dialog = builder.create()
        dialog.show()
        val font: Typeface? =  ResourcesCompat.getFont(this, R.font.raleway_semibold)
        dialog.findViewById<Button>(android.R.id.button1).typeface = font
        dialog.findViewById<Button>(android.R.id.button2).typeface = font
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showAddStepDialog(c: Context) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(40, 40, 40, 0)

        val textBox = EditText(c)
        setStyleDialogBodyET(textBox,this)

        val title=TextView(c)
        title.text = "Add a new step"
        setStyleDialogTitle(title,0,this)
        val paramsTitle = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        paramsTitle.setMargins(40, 40, 50, 0)

        layout.addView(title,paramsTitle)
        layout.addView(textBox, params)
        val dialog = AlertDialog.Builder(c)
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val input = textBox.text.toString()
                if(input.isNotEmpty()) {
                    addNewStepRow(input, true)
                }
                else{
                    Toast.makeText(
                        this, "step can't be empty",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
            .setNeutralButton("Cancel", null)
            .create()
        dialog.show()

        val font: Typeface? = ResourcesCompat.getFont(c, R.font.raleway_semibold)
        dialog.findViewById<Button>(android.R.id.button1).typeface = font
        dialog.findViewById<Button>(android.R.id.button3).typeface = font
    }

}

