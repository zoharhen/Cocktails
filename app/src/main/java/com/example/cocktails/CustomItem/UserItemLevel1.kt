package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.cocktails.Cocktail
import com.example.cocktails.Cocktails
import com.example.cocktails.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso

val EMPTY_FIELD_ERROR_MSG: String = "Field can not be empty"
val COCKATIL_NAME_KEY = "cocktailName"
val CATEGORY_KEY = "category"
val ICON_KEY = "icon"
val UPLOAD_IMG_KEY = "upload_img"
val ROTATE_UPLOAD_IMG_KEY = "rotate_upload_img"

class UserItemLevel1 : AppCompatActivity() {
    private lateinit var mUploadImgButton: Button
    private lateinit var mIconButton: Button
    private lateinit var mNextButton: Button
    private lateinit var mIconView: ImageView
    private lateinit var mUserImg: ImageView
    private lateinit var mRotateUploadView: Button
    private lateinit var mDelUploadImgButton: Button
    private lateinit var mCocktailName: TextInputLayout
    private lateinit var mCategoryChipErrorTV: TextView
    private lateinit var mIconErrorTV: TextView

    private var mUploadImgUri: Uri? = null
    private var mIconUri: Uri? = null
    private lateinit var mCategoryChip: ChipGroup


    val YES = "yes"
    val NO = "no"
    val DIALOG_UPLOAD_MSG = "If you go back now, your upload image will be removed."
    val MAX_LENGTH_COCKTAIL_NAME: Int = 12
    val COCKTAIL_ERROR_MSG_NAME = "Cocktail name already exist ,choose different name"
    val COCKTAIL_ERROR_MSG_LENGTH: String =
        "Cocktail name too long ,\n must be under $MAX_LENGTH_COCKTAIL_NAME characters"
    val BACK_PRESS_MSG = "Are you sure you want to exit? \nyour details will be deleted. "
    val REQUEST_CODE_ICONS = 2
    val REQUEST_CODE_UPLOAD_IMG = 1

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level1)
        initToolBar()
        initView()
    }

    private fun initToolBar() {
        supportActionBar?.title = getString(R.string.title_user_item)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
//        toolbar.setNavigationOnClickListener {
//            showDialogOnBackPress()
//        }
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            android.R.id.home -> {
//                onBackPressed()
//                return true
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    override fun onBackPressed() {
        super.onBackPressed()
        showDialogOnBackPress()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun initView() {
        mCocktailName = findViewById(R.id.cocktailNameInput)
        mCocktailName.counterMaxLength = MAX_LENGTH_COCKTAIL_NAME
        mCategoryChip = findViewById(R.id.selectCategoryChipGroup)
        mCategoryChipErrorTV = findViewById(R.id.category_chip_error)
        mCategoryChipErrorTV.visibility = View.GONE

        //Icon
        mIconView = findViewById(R.id.selected_icon_IV)
        mIconView.visibility = View.GONE
        mIconButton = findViewById(R.id.select_icon_Button)
        mIconErrorTV = findViewById(R.id.icon_error)
        mIconErrorTV.visibility = View.GONE

        //upload img
        mUploadImgButton = findViewById(R.id.upload_img_Button)
        mUserImg = findViewById(R.id.upload_user_img_TV)
        mRotateUploadView = findViewById(R.id.rotate_upload_view_Button)
        mDelUploadImgButton = findViewById(R.id.del_upload_img_Button)
        buttonStateOffUploadImg()

        mNextButton = findViewById(R.id.nextButton)

        initCategory()
        initButtonsListener()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("InflateParams")
    private fun initCategory() {
        resources.getStringArray(R.array.cocktailTypes_array)
            .forEach { addChip(it, findViewById(R.id.selectCategoryChipGroup)) }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addChip(item: String, chipGroup: ChipGroup) {
        val chip = Chip(chipGroup.context)
        chip.chipBackgroundColor = getColorStateList(R.color.chipColor)
        chip.text = item
        chip.isCheckable = true
        chipGroup.addView(chip)
    }


    private fun initButtonsListener() {
        mUploadImgButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_CODE_UPLOAD_IMG)
        }
        mRotateUploadView.setOnClickListener {
            if(mUserImg.rotation==360F){
                mUserImg.rotation=0F
            }
            mUserImg.rotation = mUserImg.rotation + 90F
        }
        mDelUploadImgButton.setOnClickListener {
            showDialog()
        }
        mIconButton.setOnClickListener {
            openIconsActivity()
        }
        mNextButton.setOnClickListener {
            checkedLevel1()
        }
    }

    private fun buttonStateOnUploadImg() {
        mUserImg.visibility = View.VISIBLE
        mRotateUploadView.visibility = View.VISIBLE
        mDelUploadImgButton.visibility = View.VISIBLE
    }

    private fun buttonStateOffUploadImg() {
        mUserImg.visibility = View.GONE
        mRotateUploadView.visibility = View.GONE
        mDelUploadImgButton.visibility = View.GONE
    }

    private fun showDialog() {
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setMessage(DIALOG_UPLOAD_MSG)
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    mUserImg.setImageDrawable(null)
                    buttonStateOffUploadImg()
                }
                //DialogInterface.BUTTON_NEGATIVE ->{}
            }
        }
        builder.setPositiveButton(YES, dialogClickListener)
        builder.setNegativeButton(NO, dialogClickListener)
        dialog = builder.create()
        dialog.show()
    }

    private fun checkedLevel1() {
        //save info
        val cocktailName = getCocktailName()
        val categoryChipIdSelected: Int = mCategoryChip.checkedChipId

        //check all the values filled and correct 
        if (confirmInput(cocktailName, categoryChipIdSelected)) {
            //start level 2 activity
            //todo for debug Toast.makeText(this, "all the values is valid!! :)", Toast.LENGTH_LONG).show()
            startLevel2(cocktailName, categoryChipIdSelected)
            //TODO()
        }

        //TODO(only for debug !!!!!!!!!! remove this( 2 rows) )
//        val intentLevel2 = Intent(this, UserItemLevel2::class.java)
//        startActivity(intentLevel2)
    }

    private fun startLevel2(cocktailName: String, categoryChipIdSelected: Int) {
        val intentLevel2 = Intent(this, UserItemLevel2::class.java)
        intentLevel2.putExtra(COCKATIL_NAME_KEY, cocktailName)
        intentLevel2.putExtra(
            CATEGORY_KEY,
            (mCategoryChip.getChildAt(categoryChipIdSelected - 1) as Chip).text
        )
        intentLevel2.putExtra(ICON_KEY, mIconUri.toString())
        intentLevel2.putExtra(UPLOAD_IMG_KEY, mUploadImgUri.toString())
        intentLevel2.putExtra(ROTATE_UPLOAD_IMG_KEY, mUserImg.rotation)//TODO CHECK INVALID WHEN UPLOAD NOT MUST
        startActivity(intentLevel2)
    }

    private fun isCocktailNameExist(userCocktailName: String): Boolean {
        /**check if cocktail name exist in app and user cocktail collection
         * return true if the name already exist otherwise return false */
        val jsonString =
            applicationContext.assets.open("predefined.json").bufferedReader().use { it.readText() }
        val listCocktailType = object : TypeToken<List<Cocktail>>() {}.type
        val items = Gson().fromJson<List<Cocktail>>(jsonString, listCocktailType)
        for (item in items) {
            if (userCocktailName == item.name) {
                return true
            }
        }
        //todo check if cocktail Name exists in user collection at firebase
        return false
    }

    private fun validateCocktailName(cocktailName: String?): Boolean {
        if (cocktailName.isNullOrEmpty()) {
            mCocktailName.error = EMPTY_FIELD_ERROR_MSG
            return false
        } else if (cocktailName.length > MAX_LENGTH_COCKTAIL_NAME) {
            mCocktailName.error = COCKTAIL_ERROR_MSG_LENGTH
            return false
        }
        //check name not already exist
        if (isCocktailNameExist(cocktailName)) {
            mCocktailName.error = COCKTAIL_ERROR_MSG_NAME
            return false
        }
        mCocktailName.error = null
        return true
    }

    private fun validateCategory(chipId: Int): Boolean {
        //if chipId==ChipGroup.NO_ID -> not chip selected
        if (chipId == ChipGroup.NO_ID) {
            mCategoryChipErrorTV.visibility = View.VISIBLE
            mCategoryChipErrorTV.error=""
            return false
        }//else{
        mCategoryChipErrorTV.visibility = View.INVISIBLE
        return true
    }

    private fun validateIcon(): Boolean {
        if (mIconView.drawable == null) {
            mIconErrorTV.visibility = View.VISIBLE
            mIconErrorTV.error=""
            return false
        } //else{
        mIconErrorTV.visibility = View.INVISIBLE
        return true

    }


    private fun confirmInput(cocktailName: String?, chipId: Int): Boolean {
        /** return true if all the inputs is valid */
        val validName: Boolean = validateCocktailName(cocktailName)
        val validCategory: Boolean = validateCategory(chipId)
        val validIcon: Boolean = validateIcon()
        if (validName && validCategory && validIcon) {
            return true
        }
        return false
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_CODE_UPLOAD_IMG || requestCode == REQUEST_CODE_ICONS) &&
            resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            if (requestCode == REQUEST_CODE_UPLOAD_IMG) {
                mUploadImgUri = data.data!!
                Picasso.with(this).load(mUploadImgUri).into(mUserImg)
                buttonStateOnUploadImg()
            } else {
                mIconUri = data.data!!
                val applicationContext = (this.applicationContext as Cocktails)
                val ref = applicationContext.mStorageRef.child("cliparts/" + mIconUri)
                ref.downloadUrl.addOnSuccessListener {
                    Glide.with(applicationContext)
                        .load(it)
                        .apply(RequestOptions().placeholder(null).dontAnimate().fitCenter())
                        .into(mIconView)
                        .clearOnDetach()
                }
                mIconView.visibility = View.VISIBLE
            }

        } else {
            if (requestCode == REQUEST_CODE_UPLOAD_IMG) {
                buttonStateOffUploadImg()
            } else if (requestCode == REQUEST_CODE_ICONS) {
                mIconView.visibility = View.GONE
            }
        }
    }

    private fun openIconsActivity() {
        val intent = Intent(this, CocktailIconLevel1::class.java)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_CODE_ICONS)
    }

    private fun showDialogOnBackPress() {
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setMessage(BACK_PRESS_MSG)
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    finish()
                }
                //DialogInterface.BUTTON_NEGATIVE ->{}
                //DialogInterface.BUTTON_NEUTRAL->{}
            }
        }
        builder.setPositiveButton(YES, dialogClickListener)
        builder.setNegativeButton(NO, dialogClickListener)
        dialog = builder.create()
        dialog.show()
    }

    private fun getCocktailName(): String {
        // Get input text
        //todo trim
        return mCocktailName.editText?.text.toString().trim()
    }
}

// Extension function to show toast message
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}