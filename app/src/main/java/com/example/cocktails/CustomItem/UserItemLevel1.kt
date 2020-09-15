package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import kotlinx.android.synthetic.main.activity_user_item_level1.*


class UserItemLevel1 : AppCompatActivity() {
    private lateinit var mUploadImgButton: Button
    private lateinit var mIconButton: Button
    private lateinit var mNextButton: Button
    private lateinit var mIconView: ImageView
    private lateinit var mUserImg: ImageView
    private lateinit var mRotateUploadView: Button
    private lateinit var mDelUploadImgButton: Button
    private lateinit var mCocktailName: TextInputLayout
    private var mUploadImgUri: Uri? = null
    private var mIconUri: Uri? = null
    private lateinit var mCategoryChip: ChipGroup

    val COCKATIL_NAME_KEY = "cocktailName"
    val CATEGORY_KEY = "category"
    val ICON_KEY = "icon"
    val UPLOAD_IMG_KEY = "upload_img"
    val EMPTY_FIELD_ERROR_MSG: String = "Field can not be empty"
    val MAX_LENGTH_COCKTAIL_NAME: Int = 12
    val COCKTAIL_ERROR_MSG_NAME = "Cocktail name already exist ,choose different name"
    val COCKTAIL_ERROR_MSG_LENGTH: String =
        "Cocktail name too long ,\n must me under $MAX_LENGTH_COCKTAIL_NAME characters"
    val BACK_PRESS_MSG="Are you sure you want to exit? \nyour details will be deleted. "

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level1)

        initToolBar()
        initView()
    }

    private fun initToolBar(){
        val toolbar: Toolbar = findViewById<View>(R.id.toolbar_user1) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener {
            showDialogOnBackPress()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        showDialogOnBackPress()
    }


    private fun initView() {
        mCocktailName = findViewById(R.id.cocktailNameInput)
        cocktailNameInput.counterMaxLength = MAX_LENGTH_COCKTAIL_NAME
        mCategoryChip = findViewById(R.id.selectCategoryChipGroup)
        category_chip_error.visibility = View.GONE

        //Icon
        mIconView = findViewById(R.id.selected_icon_IV)
        mIconView.visibility = View.GONE
        mIconButton = findViewById(R.id.select_icon_Button)
        icon_error.visibility = View.GONE

        //upload img
        mUploadImgButton = findViewById(R.id.upload_img_Button)
        mUserImg = findViewById(R.id.upload_user_img_TV)
        mRotateUploadView = findViewById(R.id.rotate_upload_view_Button)
        mDelUploadImgButton= findViewById(R.id.del_upload_img_Button)
        buttonStateOffUploadImg()


        mNextButton = findViewById(R.id.nextButton)

        initCategory()
        initButtonsListener()
    }
    private fun showDialogOnBackPress(){
        // Late initialize an alert dialog object
        lateinit var dialog:AlertDialog
        // Initialize a new instance of alert dialog builder object
        val builder = AlertDialog.Builder(this)

        // Set a message for alert dialog
        builder.setMessage(BACK_PRESS_MSG)

        // On click listener for dialog buttons
        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> {
                    finish()
                }
                //DialogInterface.BUTTON_NEGATIVE ->{}
                //DialogInterface.BUTTON_NEUTRAL->{}
            }
        }
        builder.setPositiveButton("YES", dialogClickListener)

        // Set the alert dialog negative/no button
        builder.setNegativeButton("NO", dialogClickListener)

        // Set the alert dialog neutral/cancel button
        builder.setNeutralButton("CANCEL", dialogClickListener)

        dialog = builder.create()
        dialog.show()
    }

    private fun getCocktailName(): String {
        // Get input text
        //todo trim
        return mCocktailName.editText?.text.toString().trim()
    }

    @SuppressLint("InflateParams")
    private fun initCategory() {
        resources.getStringArray(R.array.cocktailTypes_array)
            .forEach { addChip(it, findViewById(R.id.selectCategoryChipGroup)) }
    }

    private fun addChip(item: String, chipGroup: ChipGroup) {
        val chip = Chip(chipGroup.context)
        chip.text = item
        chip.isCheckable = true
        chipGroup.addView(chip)
    }

    private fun initButtonsListener() {
        mUploadImgButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, 1)
        }

        mRotateUploadView.setOnClickListener{
            mUserImg.rotation = mUserImg.rotation + 90F
        }
        mDelUploadImgButton.setOnClickListener{
            showDialog()
        }
        mIconButton.setOnClickListener {
            openImagesActivity()
        }

        mNextButton.setOnClickListener {
            checkedLevel2()
        }
    }

    private fun buttonStateOnUploadImg(){
        mUserImg.visibility = View.VISIBLE
        mRotateUploadView.visibility = View.VISIBLE
        mDelUploadImgButton.visibility = View.VISIBLE
    }

    private fun buttonStateOffUploadImg(){
        mUserImg.visibility = View.GONE
        mRotateUploadView.visibility = View.GONE
        mDelUploadImgButton.visibility = View.GONE
    }

    private fun showDialog(){
        // Late initialize an alert dialog object
        lateinit var dialog:AlertDialog


        // Initialize a new instance of alert dialog builder object
        val builder = AlertDialog.Builder(this)

        // Set a message for alert dialog
        builder.setMessage("If you go back now , your upload image will be removed.")


        // On click listener for dialog buttons
        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> {
                    mUserImg.setImageDrawable(null)
                    buttonStateOffUploadImg()
                }
                //DialogInterface.BUTTON_NEGATIVE ->{}
                //DialogInterface.BUTTON_NEUTRAL->{}
            }
        }
        builder.setPositiveButton("YES", dialogClickListener)

        // Set the alert dialog negative/no button
        builder.setNegativeButton("NO", dialogClickListener)

        // Set the alert dialog neutral/cancel button
        builder.setNeutralButton("CANCEL", dialogClickListener)

        dialog = builder.create()
        dialog.show()
    }
    private fun checkedLevel2() {
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
    }

    private fun startLevel2(cocktailName: String, categoryChipIdSelected: Int) {
        val intentLevel2 = Intent(this, UserItemLevel2::class.java)
        intentLevel2.putExtra(COCKATIL_NAME_KEY, cocktailName)
        intentLevel2.putExtra(
            CATEGORY_KEY,
            (mCategoryChip.getChildAt(categoryChipIdSelected - 1) as Chip).text
        )
        intentLevel2.putExtra(ICON_KEY, mIconUri)
        intentLevel2.putExtra(UPLOAD_IMG_KEY, mUploadImgUri)
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
            category_chip_error.visibility = View.VISIBLE
            return false
        }//else{
        category_chip_error.visibility = View.INVISIBLE
        return true
    }

    private fun validateIcon(): Boolean {
        if (mIconView.drawable == null) {
            icon_error.visibility = View.VISIBLE
            return false
        } //else{
        icon_error.visibility = View.INVISIBLE
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
        if ((requestCode == 1 || requestCode == 2) && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            if (requestCode == 1) {
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

        }
        else if(requestCode == 1 || requestCode == 2) {
            if(requestCode == 1) {
                buttonStateOffUploadImg()
            }
            else{
                mIconView.visibility=View.GONE
            }
        }
    }

    private fun openImagesActivity() {
        val intent = Intent(this, CocktailIconLevel1::class.java)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 2)
    }
}


// Extension function to show toast message
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}