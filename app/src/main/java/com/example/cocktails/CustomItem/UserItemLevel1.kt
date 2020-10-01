package com.example.cocktails.CustomItem

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.cocktails.Cocktail
import com.example.cocktails.Cocktails
import com.example.cocktails.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_item_level1.*

const val EMPTY_FIELD_ERROR_MSG: String = "Field can not be empty"
const val COCKTAIL_NAME_KEY = "cocktailName"
const val CATEGORY_KEY = "category"
const val ICON_KEY = "icon"
const val UPLOAD_IMG_KEY = "upload_img"
const val ROTATE_UPLOAD_IMG_KEY = "rotate_upload_img"
const val DEL_BODY_MSG = "You may be deleting cocktail item.\nAfter you delete this, it can't be recovered."
const val DEL_TITLE_MSG="Delete cocktail"

class UserItemLevel1 : AppCompatActivity() {

    private var mUploadImgUri: Uri? = null
    private var mIconUri: Uri? = null
    private lateinit var mCategoryChip: ChipGroup

    private val YES = "Yes"
    private val NO = "No"
    private val DIALOG_UPLOAD_MSG = "If you go back now, your upload image will be removed."
    private val MAX_LENGTH_COCKTAIL_NAME: Int = 12
    private val COCKTAIL_ERROR_MSG_NAME = "Cocktail name already exist, choose different name."
    private val COCKTAIL_ERROR_MSG_LENGTH: String =
        "Cocktail name too long,\n must be under $MAX_LENGTH_COCKTAIL_NAME characters."
    private val REQUEST_CODE_ICONS = 2
    private val REQUEST_CODE_UPLOAD_IMG = 1
    private val PERMISSION_EXTERNAL_STORAGE_ID = 44

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level1)
        initToolBar()
        initView()
        closeKeyBoard()
    }

    private fun initToolBar() {
        supportActionBar?.title = getString(R.string.title_user_item)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                showDialogOnBackPress()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Override
    override fun onBackPressed() {
        showDialogOnBackPress()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initView() {
        cocktailNameInput.counterMaxLength = MAX_LENGTH_COCKTAIL_NAME

        mCategoryChip = findViewById(R.id.selectCategoryChipGroup)
        setUploadImgButtonState(View.GONE)
        initCategory()
        initButtonsListener()

        stepsView.setLabels(arrayOf("", "", ""))
            .setBarColorIndicator(resources.getColor(R.color.material_blue_grey_800))
            .setProgressColorIndicator(resources.getColor(R.color.stepBg))
            .setLabelColorIndicator(resources.getColor(R.color.stepBg))
            .setCompletedPosition(0)
            .drawView()
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
        chip.setTextAppearance(R.style.chipText)
        chip.text = item
        chip.isCheckable = true
        chipGroup.addView(chip)
    }


    private fun initButtonsListener() {
        upload_img_Button.setOnClickListener {
            checkReadExternalStoragePermission()

        }
        rotate_upload_view_Button.setOnClickListener {
            if (upload_user_img_TV.rotation == 360F) {
                upload_user_img_TV.rotation = 0F
            }
            upload_user_img_TV.rotation = upload_user_img_TV.rotation + 90F
        }
        del_upload_img_Button.setOnClickListener {
            showDialog()
        }
        select_icon_Button.setOnClickListener {
            openIconsActivity()
        }
        nextButton.setOnClickListener {
            checkedLevel1()
        }
    }

    private fun setUploadImgButtonState(visible: Int) {
        upload_user_img_TV.visibility = visible
        rotate_upload_view_Button.visibility = visible
        del_upload_img_Button.visibility = visible
    }

    private fun showDialog() {
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setMessage(DIALOG_UPLOAD_MSG)
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    upload_user_img_TV.setImageDrawable(null)
                    setUploadImgButtonState(View.GONE)
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
        val categoryChipIdSelected: Int = selectCategoryChipGroup.checkedChipId

        //check all the values filled and correct
        if (confirmInput(cocktailName, categoryChipIdSelected)) {
            //start level 2 activity
            startLevel2(cocktailName, categoryChipIdSelected)
        }
    }

    private fun startLevel2(cocktailName: String, categoryChipIdSelected: Int) {
        val intentLevel2 = Intent(this, UserItemLevel2::class.java)
        intentLevel2.putExtra(COCKTAIL_NAME_KEY, cocktailName)
        val selectedChip=selectCategoryChipGroup.findViewById<Chip>(categoryChipIdSelected).text.toString()
        intentLevel2.putExtra(
            CATEGORY_KEY, selectedChip)
        intentLevel2.putExtra(ICON_KEY, mIconUri.toString())
        var uploadImgStr: String? = null
        if (mUploadImgUri != null) {
            uploadImgStr = mUploadImgUri.toString()
        }
        intentLevel2.putExtra(UPLOAD_IMG_KEY, uploadImgStr)
        intentLevel2.putExtra(
            ROTATE_UPLOAD_IMG_KEY,
            upload_user_img_TV.rotation
        )//TODO CHECK INVALID WHEN UPLOAD NOT MUST
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
            cocktailNameInput.error = EMPTY_FIELD_ERROR_MSG
            return false
        } else if (cocktailName.length > MAX_LENGTH_COCKTAIL_NAME) {
            cocktailNameInput.error = COCKTAIL_ERROR_MSG_LENGTH
            return false
        }
        //check name not already exist
        if (isCocktailNameExist(cocktailName)) {
            cocktailNameInput.error = COCKTAIL_ERROR_MSG_NAME
            return false
        }
        cocktailNameInput.error = null
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
        if (selected_icon_IV.drawable == null) {
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

    private fun displayIcon(iconUri: Uri) {
        val applicationContext = (this.applicationContext as Cocktails)
        val ref = applicationContext.mStorageRef.child("cliparts/" + mIconUri)
        ref.downloadUrl.addOnSuccessListener {
            Glide.with(applicationContext)
                .load(it)
                .apply(RequestOptions().placeholder(null).dontAnimate().fitCenter())
                .into(selected_icon_IV)
                .clearOnDetach()
        }
        selected_icon_IV.visibility = View.VISIBLE
    }

    private fun displayUploadImg(img: Uri) {
        Picasso.with(this).load(mUploadImgUri).into(upload_user_img_TV)
        setUploadImgButtonState(View.VISIBLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_CODE_UPLOAD_IMG || requestCode == REQUEST_CODE_ICONS) &&
            resultCode == Activity.RESULT_OK && data != null && data.data != null
        ) {
            if (requestCode == REQUEST_CODE_UPLOAD_IMG) {
                mUploadImgUri = data.data!!
                displayUploadImg(mUploadImgUri!!)
            } else {
                mIconUri = data.data!!
                displayIcon(mIconUri!!)
            }
        } else {
            if (requestCode == REQUEST_CODE_UPLOAD_IMG) {
                setUploadImgButtonState(View.GONE)
            } else if (requestCode == REQUEST_CODE_ICONS) {
                selected_icon_IV.visibility = View.GONE
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
        builder.setMessage(DEL_BODY_MSG)
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    finish()
                }
                //DialogInterface.BUTTON_NEGATIVE ->{}
                //DialogInterface.BUTTON_NEUTRAL->{}
            }
        }
        builder.setPositiveButton("Delete", dialogClickListener)
        builder.setNegativeButton("Cancel", dialogClickListener)
        dialog = builder.create()
        dialog.setIcon(R.drawable.ic_warning_30)
        dialog.setTitle(DEL_TITLE_MSG)
        dialog.show()
    }

    private fun getCocktailName(): String {
        // Get input text
        //todo trim
        return cocktailNameInput.editText?.text.toString().trim()
    }

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun checkReadExternalStoragePermission() {
        if (checkManifestReadExternalStoragePermission()) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_CODE_UPLOAD_IMG)
        } else {
            requestLocationPermissions()
        }
    }

    private fun checkManifestReadExternalStoragePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_EXTERNAL_STORAGE_ID
        )
    }

    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_EXTERNAL_STORAGE_ID -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    upload_img_Button.performClick()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this, Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        Toast.makeText(
                            applicationContext,
                            "you can't upload an image without location permission",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}