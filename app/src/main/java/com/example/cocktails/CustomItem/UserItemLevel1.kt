package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.cocktails.Cocktails
import com.example.cocktails.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_item_level1.*


class UserItemLevel1 : AppCompatActivity() {
    private lateinit var mUploadImgButton: Button
    private lateinit var mIconButton: Button
    private lateinit var mNextButton: Button
    private lateinit var mIconView: ImageView
    private lateinit var mUserImg: ImageView
    private lateinit var mCocktailName: TextInputLayout
    private var mUploadImgUri: Uri?=null
    private var mIconUri: Uri?=null
    private lateinit var mCategoryChip: ChipGroup

    val COCKATIL_NAME_KEY="cocktailName"
    val CATEGORY_KEY = "category"
    val ICON_KEY = "icon"
    val UPLOAD_IMG_KEY = "upload_img"
    val EMPTY_FIELD_ERROR_MSG: String = "Field can not be empty"
    val MAX_LENGTH_COCKTAIL_NAME: Int = 12
    val ACTIVITY_TITLE: String = "CREATE NEW COCKTAIL"
    val COCKTAIL_ERROR_MSG_LENGTH: String =
        "cocktail name too long ,\n must me under $MAX_LENGTH_COCKTAIL_NAME characters"

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level1)

        //activity title
        supportActionBar?.title = ACTIVITY_TITLE

        initView()

    }

    private fun initView() {
        mCocktailName = findViewById(R.id.cocktailNameInput)
        cocktailNameInput.counterMaxLength = MAX_LENGTH_COCKTAIL_NAME
        mCategoryChip = findViewById(R.id.selectCategoryChipGroup)
        category_chip_error.visibility = View.INVISIBLE

        //Icon
        mIconView = findViewById(R.id.selected_icon_IV)
        mIconButton = findViewById(R.id.select_icon_Button)
        icon_error.visibility = View.INVISIBLE

        //upload img
        mUploadImgButton = findViewById(R.id.upload_img_Button)
        mUserImg = findViewById(R.id.upload_user_img_TV)

        mNextButton = findViewById(R.id.nextButton)

        initCategory()
        initButtonsListener()
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

        mIconButton.setOnClickListener {
            openImagesActivity()
        }

        mNextButton.setOnClickListener {
            checkedLevel2()
        }
    }

    private fun checkedLevel2() {
        //save info
        val cocktailName = getCocktailName()
        val categoryChipIdSelected: Int = mCategoryChip.checkedChipId

        //check all the values filled and correct 
        if (confirmInput(cocktailName, categoryChipIdSelected)) {
            //start level 2 activity
            //todo for debug Toast.makeText(this, "all the values is valid!! :)", Toast.LENGTH_LONG).show()
            startLevel2(cocktailName,categoryChipIdSelected)
            //TODO()
        } else {
            //user need to fill the values
            //todo for debug Toast.makeText(this, "error :( ", Toast.LENGTH_LONG).show()
        }
    }

    private fun startLevel2(cocktailName: String, categoryChipIdSelected: Int){
        val intentLevel2 = Intent(this, UserItemLevel2::class.java)
        intentLevel2.putExtra(COCKATIL_NAME_KEY,cocktailName)
        intentLevel2.putExtra(CATEGORY_KEY,(mCategoryChip.getChildAt(categoryChipIdSelected-1) as Chip).text)
        intentLevel2.putExtra(ICON_KEY,mIconUri)
        intentLevel2.putExtra(UPLOAD_IMG_KEY,mUploadImgUri)
        startActivity(intentLevel2)
    }

    private fun validateCocktailName(cocktailName: String?): Boolean {
        if (cocktailName.isNullOrEmpty() ) {
            mCocktailName.error = EMPTY_FIELD_ERROR_MSG
            return false
        } else if( cocktailName.length > MAX_LENGTH_COCKTAIL_NAME) {
            mCocktailName.error = COCKTAIL_ERROR_MSG_LENGTH
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
        if (mIconView.drawable==null) {
            icon_error.visibility = View.VISIBLE
            return false
        } //else{
        icon_error.visibility = View.INVISIBLE
        return true

    }


    private fun confirmInput(cocktailName: String?, chipId: Int): Boolean {
        /** return true if all the inputs is valid */
        val validName:Boolean= validateCocktailName(cocktailName)
        val validCategory:Boolean= validateCategory(chipId)
        val validIcon:Boolean=validateIcon()
        if(validName && validCategory && validIcon){
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
            } else {
                mIconUri=data.data!!
                val applicationContext = (this.applicationContext as Cocktails)
                val ref = applicationContext.mStorageRef.child("cliparts/" + mIconUri)
                ref.downloadUrl.addOnSuccessListener {
                    Glide.with(applicationContext)
                        .load(it)
                        .apply(RequestOptions().placeholder(null).dontAnimate().fitCenter())
                        .into(mIconView)
                        .clearOnDetach()
                }
            }
        }
    }

    private fun openImagesActivity() {
        val intent = Intent(this, CocktailIconLevel1::class.java)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 2)
    }
}
