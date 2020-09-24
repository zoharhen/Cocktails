package com.example.cocktails.CustomItem.insertIngredients

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktails.CustomItem.CURRENT_UNIT_KEY
import com.example.cocktails.R


val UNIT_VAL_KEY = "unit_val"


class SelectUnit : AppCompatActivity() {
    private lateinit var mRadioGroup: RadioGroup
    private lateinit var mUnitList: ArrayList<String>
    private val TITLE_ACTIVITY="Unit"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_unit)
        supportActionBar?.title = TITLE_ACTIVITY
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var curVal:String=resources.getString(R.string.defaultUnit)
        if( intent.extras!=null) {
            curVal = intent.extras!!.getString(CURRENT_UNIT_KEY).toString()
        }
        createRadioButtons(curVal)

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.done_button_menu -> {
                finish()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_add_ingredient, menu)
        return true
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            android.R.id.home -> {
//                onBackPressed()
//                return true
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    @SuppressLint("SetTextI18n")
    private fun createRadioButtons(curVal: String) {
        mRadioGroup = findViewById(R.id.unit_radio_group)
        mUnitList = ArrayList(listOf(*resources.getStringArray(R.array.unitTypes_array)))
        for (i in 0 until mUnitList.size) {
            val radioButtonView = RadioButton(this, null, R.attr.radioButtonStyle)
            radioButtonView.text = mUnitList[i]
            radioButtonView.setCircleColor(Color.parseColor("#1974D2"))
//            if (radioButtonView.text == resources.getString(R.string.defaultUnit) ) {
//                radioButtonView.text = radioButtonView.text as String + " (default) "
//            }
            if(radioButtonView.text ==curVal){
                radioButtonView.isChecked=true
            }
            radioButtonView.id = i
            mRadioGroup.addView(radioButtonView)
            //todo set on click callback
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
        intentBack.putExtra(UNIT_VAL_KEY, itemChecked)
        setResult(RESULT_OK, intentBack)
        super.finish()
    }

    private fun getItemChecked(): String {
        val checkedItemId = mRadioGroup.checkedRadioButtonId
        var itemVal = resources.getString(R.string.defaultUnit)
        if (0 <= checkedItemId && checkedItemId < mUnitList.size) {
            itemVal = mUnitList[checkedItemId]
        }
        return itemVal
    }


}