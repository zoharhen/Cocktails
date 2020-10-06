package com.example.cocktails.CustomItem

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.cocktails.Cocktail
import com.example.cocktails.Cocktails
import com.example.cocktails.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_item_level1.*
import java.io.File

const val EMPTY_FIELD_ERROR_MSG: String = "Field can not be empty"
const val COCKTAIL_NAME_KEY = "cocktailName"
const val CATEGORY_KEY = "category"
const val ICON_KEY = "icon"
const val UPLOAD_IMG_KEY = "upload_img"
const val UPLOAD_IMG_PATH_KEY = "upload_img_path"
const val ROTATE_UPLOAD_IMG_KEY = "rotate_upload_img"
const val DEL_BODY_MSG =
    "You may be deleting cocktail item.\nAfter you delete this, it can't be recovered."
const val DEL_CHANGES_BODY_MSG="You may be deleting your changes.\nAfter you delete this, it can't be recovered."
const val DEL_TITLE_MSG = "Delete cocktail"
const val DEL_CHANGES_TITLE_MSG = "Delete changes"
const val EDIT_MODE_KEY = "edit"
const val UPLOAD_IMG_BOOLEAN_KEY = "boolean_upload_image"
const val YES = "Yes"
const val NO = "No"
const val CLIPART_STORAGE_PATH="cliparts/"

fun setStyleDialogTitle(textView: TextView,icon:Int,context: Context){
    val font: Typeface? = ResourcesCompat.getFont(context, R.font.raleway_semibold)
    textView.typeface=font
    textView.setTextColor(Color.BLACK)
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon,0,0,0)
}

fun setStyleDialogBodyTV(textView: TextView, context: Context){
    val fontBody: Typeface? = ResourcesCompat.getFont(context, R.font.raleway_regular)
    textView.typeface = fontBody
    textView.setRawInputType(InputType.TYPE_CLASS_TEXT)
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
    textView.setTextColor(Color.BLACK)
}

fun setStyleDialogBodyET(editText: EditText, context: Context){
    editText.requestFocus()
    editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
    editText.setTextColor(Color.BLACK)
    editText.setRawInputType(InputType.TYPE_CLASS_TEXT)
    editText.typeface = ResourcesCompat.getFont(context, R.font.raleway_regular)
}

class UserItemLevel1 : AppCompatActivity() {

    private var mUploadImgUri: Uri? = null
    private var mIconUri: Uri? = null
    private lateinit var mCategoryChip: ChipGroup
    private var isEditMode : Boolean= false

    private val DIALOG_UPLOAD_MSG = "You may be deleting your uploaded image."
    private val MAX_LENGTH_COCKTAIL_NAME: Int = 12
    private val COCKTAIL_ERROR_MSG_NAME = "Cocktail name already exist, choose different name."
    private val COCKTAIL_ERROR_MSG_LENGTH: String =
        "Cocktail name too long,\nmust be under $MAX_LENGTH_COCKTAIL_NAME characters."
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
        isEditMode=intent.getBooleanExtra(EDIT_MODE_KEY, false)
        if (isEditMode) {
            initEditMode()
        } else { //for safe
            cleanSpUserInput()
        }
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

    private fun cleanSpUserInput() {
        (this.applicationContext as Cocktails).mUserInputs.edit().clear().apply()
    }

    private fun initEditMode() {
        //set name
        val name: String? = (this.applicationContext as Cocktails).mUserInputs.getString(COCKTAIL_NAME_KEY, "")
        if (!name.isNullOrEmpty()) {
            cocktailNameText.setText(name)
        }

        setCategoryView()
        setIconView()
        setUploadImgView(name)
    }

    private fun setCategoryView(){
        val categoryName = (this.applicationContext as Cocktails).mUserInputs.getString(CATEGORY_KEY, "")
        selectCategoryChipGroup.clearCheck()
        selectCategoryChipGroup.children.forEach {
            if (((it as Chip).chipDrawable as ChipDrawable).text == categoryName) {
                it.isChecked = true
            }
        }
    }

    private fun setIconView(){
        val icon = (this.applicationContext as Cocktails).mUserInputs.getString(ICON_KEY, null)
        if (!icon.isNullOrEmpty()) {
            mIconUri = Uri.parse("$icon.png")
            displayIcon()
        }
    }

    private fun setUploadImgView(name:String?){
        val c = (this.applicationContext as Cocktails)
        val uploadImgPath = c.mUserInputs.getString(UPLOAD_IMG_PATH_KEY, null)
        if (uploadImgPath != null && name != null) {
            val imgPath = c.getUploadUserImgPath(name)
            val islandRef = c.mStorageRef.child(imgPath)
            // Local temp file has been created
            val localFile = File.createTempFile("images", "jpg")
            islandRef.getFile(localFile).addOnSuccessListener {
                mUploadImgUri = Uri.fromFile(localFile)
                displayUploadImg()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun initView() {
        cocktailNameInput.counterMaxLength = MAX_LENGTH_COCKTAIL_NAME

        mCategoryChip = findViewById(R.id.selectCategoryChipGroup)
        setUploadImgButtonState(View.GONE)
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
            showDelUploadImgDialog()
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



    private fun getCocktailName(): String {
        // Get input text
        return cocktailNameInput.editText?.text.toString().trim()
    }


    //###########valitaion##################
    private fun isCocktailNameExist(cocktailName: String): Boolean {
        /**check if cocktail name exist in app and user cocktail collection
         * return true if the name already exist otherwise return false */
        val jsonString =
            applicationContext.assets.open("predefined.json").bufferedReader().use { it.readText() }
        val listCocktailType = object : TypeToken<List<Cocktail>>() {}.type
        val items = Gson().fromJson<List<Cocktail>>(jsonString, listCocktailType)
        for (item in items) {
            if (cocktailName == item.name) {
                return true
            }
        }
        //check if cocktail Name exists in user collection at firebase
        return  checkIfNameExistUserCocktail(cocktailName)
    }

    private fun checkIfNameExistUserCocktail(cocktailName:String):Boolean{
        for(cocktail in (this.applicationContext as Cocktails).mUserCocktailsList){
            if(cocktail.name==cocktailName){
                if(isEditMode){
                    val name: String? = (this.applicationContext as Cocktails).mUserInputs.getString(COCKTAIL_NAME_KEY, "")
                    if (!name.isNullOrEmpty() && cocktail.name==name) {
                        continue
                    }
                }
                return true
            }
        }
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

    //#######diaplay images##############
    private fun displayIcon() {
        val applicationContext = (this.applicationContext as Cocktails)
        val ref = applicationContext.mStorageRef.child(CLIPART_STORAGE_PATH + mIconUri)
        ref.downloadUrl.addOnSuccessListener {
            Glide.with(applicationContext)
                .load(it)
                .apply(RequestOptions().placeholder(null).dontAnimate().fitCenter())
                .into(selected_icon_IV)
                .clearOnDetach()
        }
        selected_icon_IV.visibility = View.VISIBLE
    }

    private fun displayUploadImg() {
        Picasso.with(this).load(mUploadImgUri).into(upload_user_img_TV)
        setUploadImgButtonState(View.VISIBLE)
    }

    //#############Activity###################
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
        val selectedChip =
            selectCategoryChipGroup.findViewById<Chip>(categoryChipIdSelected).text.toString()
        val c = (this.applicationContext as Cocktails)
        c.mUserInputs.edit().putString(COCKTAIL_NAME_KEY, cocktailName).putString(
            CATEGORY_KEY,
            selectedChip
        )
            .putString(ICON_KEY, mIconUri.toString()).apply()
        if (mUploadImgUri != null) {
            c.mUserInputs.edit().putString(UPLOAD_IMG_KEY, mUploadImgUri.toString()).putFloat(
                ROTATE_UPLOAD_IMG_KEY, upload_user_img_TV.rotation
            ).apply()
        } else {
            val isUploadImg = c.mUserInputs.getBoolean(UPLOAD_IMG_BOOLEAN_KEY, false)
            if (isUploadImg) {
                c.mUserInputs.edit().putFloat(ROTATE_UPLOAD_IMG_KEY, upload_user_img_TV.rotation
                ).apply() }
        }
        if (isEditMode) {
            intentLevel2.putExtra(EDIT_MODE_KEY, isEditMode)
        }
        startActivity(intentLevel2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_CODE_UPLOAD_IMG || requestCode == REQUEST_CODE_ICONS) &&
            resultCode == Activity.RESULT_OK && data != null && data.data != null
        ) {
            if (requestCode == REQUEST_CODE_UPLOAD_IMG) {
                mUploadImgUri = data.data!!
                displayUploadImg()
            } else {
                mIconUri = data.data!!
                displayIcon()
            }
        } else {
            if (requestCode == REQUEST_CODE_UPLOAD_IMG) {
                if(mUploadImgUri==null) {
                    setUploadImgButtonState(View.GONE)
                }
            } else if (requestCode == REQUEST_CODE_ICONS) {
                if(mIconUri==null) {
                    selected_icon_IV.visibility = View.GONE
                }
            }
        }
    }

    private fun openIconsActivity() {
        val intent = Intent(this, CocktailIconLevel1::class.java)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_CODE_ICONS)
    }

    //##################Dialogs########################
    private fun showDelUploadImgDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(40, 30, 40, 0)

        val textBox = TextView(this)
        setStyleDialogBodyTV(textBox,this)
        textBox.text="Are you sure?"
        val title=TextView(this)
        setStyleDialogTitle(title,R.drawable.ic_warning_30,this)
        title.text=" Delete upload image"

        layout.addView(title,params)
        layout.addView(textBox, params)

        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton(YES) { _, _ ->
                mUploadImgUri=null
                upload_user_img_TV.setImageDrawable(null)
                setUploadImgButtonState(View.GONE)
            }
            .setNegativeButton(NO, null)

        dialog = builder.create()
        dialog.show()
        val font: Typeface? =  ResourcesCompat.getFont(this, R.font.raleway_semibold)
        dialog.findViewById<Button>(android.R.id.button1).typeface = font
        dialog.findViewById<Button>(android.R.id.button2).typeface = font
    }

    @SuppressLint("ResourceType", "SetTextI18n")
    private fun showDialogOnBackPress() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(40, 30, 40, 0)

        val textBox = TextView(this)
        setStyleDialogBodyTV(textBox,this)
        val title=TextView(this)
        setStyleDialogTitle(title,R.drawable.ic_warning_30,this)
        val paramsTitle = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        paramsTitle.setMargins(40, 40, 40, 0)

        if(isEditMode){
            textBox.text = DEL_CHANGES_BODY_MSG
            title.text = " $DEL_CHANGES_TITLE_MSG"
        }
        else {
            textBox.text = DEL_BODY_MSG
            title.text = " $DEL_TITLE_MSG"
        }

        layout.addView(title,paramsTitle)
        layout.addView(textBox, params)

        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Delete") { _, _ ->
                (this.applicationContext as Cocktails).mUserInputs.edit().clear().apply()
                finish()
            }
            .setNeutralButton("Cancel", null)
        dialog = builder.create()
        dialog.show()
        val font: Typeface? = ResourcesCompat.getFont(this, R.font.raleway_semibold)
        dialog.findViewById<Button>(android.R.id.button1).typeface = font
        dialog.findViewById<Button>(android.R.id.button2).typeface = font
    }

    //###keyBoard#######
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

    //###############Permission##################
    private fun checkReadExternalStoragePermission() {
        if (checkManifestReadExternalStoragePermission()) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_CODE_UPLOAD_IMG)
        } else {
            requestReadExternalStoragePermissions()
        }
    }

    private fun checkManifestReadExternalStoragePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestReadExternalStoragePermissions() {
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
                            "you can't upload an image without storage permission",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}