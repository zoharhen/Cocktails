package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktails.R


class UserItemLevel2 : AppCompatActivity() {
    lateinit var tableRow: TableRow
    lateinit var tableLayout: TableLayout
    val REQUEST_CODE_SELECT_INGREDIENTS = 1
    var counterRowTable: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level2)
        initTable()

        //todo
        //initToolBar()
    }

    //todo
//    private fun initToolBar() {
//    }

    @SuppressLint("ResourceAsColor")
    private fun initTable() {
        tableLayout = findViewById(R.id.ingredient_table)
        tableRow = findViewById(R.id.table_row)
        counterRowTable = -1
        tableLayout.setColumnStretchable(0, true)
        tableLayout.setColumnStretchable(1, true)
        tableLayout.setColumnStretchable(2, true)
        tableLayout.setColumnStretchable(3, true)

        val addRowButton: View = findViewById(R.id.adding_new_row)
        addRowButton.setOnClickListener {
            startSelectIngredient()
        }

    }

    private fun startSelectIngredient() {
        val intent = Intent(this, SelectIngredient::class.java)
        startActivityForResult(intent, 1)
    }

    private fun addNewRow(quantityVal: String, unitVal: String, ingredientVal: String) {
        tableRow = TableRow(this)
        val scrNumRow = TextView(this)
        val scrQuantity = TextView(this)
        val scrUnit = TextView(this)
        val scrIngredient = TextView(this)

        scrNumRow.text = (counterRowTable + 1).toString()
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

        tableRow.addView(scrNumRow)
        tableRow.addView(scrQuantity)
        tableRow.addView(scrUnit)
        tableRow.addView(scrIngredient)
        tableRow.setPadding(10, 10, 10, 10)
        tableRow.setBackgroundColor(resources.getColor(R.color.rowColor))
        tableLayout.addView(tableRow)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_INGREDIENTS && resultCode == Activity.RESULT_OK && data != null) {
            val quantityVal = data.getStringExtra(QUANTITY_KEY)
            val unitVal = data.getStringExtra(UNIT_KEY)
            val ingredientVal = data.getStringExtra(INGREDIENT_KEY)
            if (!quantityVal.isNullOrEmpty() && !unitVal.isNullOrEmpty() && !ingredientVal.isNullOrEmpty()) {
                addNewRow(quantityVal, unitVal, ingredientVal)
                counterRowTable += 1
            }
        }
    }
}

