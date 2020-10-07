package com.cocktailsppc.cocktails.CustomItem

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.cocktailsppc.cocktails.*
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import kotlin.collections.ArrayList


class UserItemLevel3 : AppCompatActivity() {
    val DEFAULT_GLASS = "Water Glass.sfb"
    val FINISH="Finish"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level3)
        initButtons()
        initToolBar()
    }

    private fun initToolBar() {
        supportActionBar?.title = getString(R.string.title_user_item)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.create_button_menu -> {
                createCocktail()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun createCocktail() {
        val cocktail : Cocktail = getCocktail() ?: return
        saveCocktailToFirebase(cocktail)
        val cnt = (applicationContext as Cocktails)
        cnt.mUserInputs.edit().clear().apply()
        saveNewIngredientsToSp(cocktail.ingredientItemsJsonList)
        val intent = Intent(applicationContext, ScrollingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun saveNewIngredientsToSp(ingredientItemsJsonList:String) {
        val cnt = (applicationContext as Cocktails)
        val ingredientItemsCurrent=Gson().fromJson(ingredientItemsJsonList, Array<IngredientItem>::class.java).asList()
        val ingredientsListSP=cnt.getUserSpIngredients()
        val ingredientListApp=resources.getStringArray(R.array.ingredients)
        val ingredientsList=ArrayList<String>()
        if(!ingredientsListSP.isNullOrEmpty()){
            ingredientsList.addAll(ingredientsListSP)
        }
        ingredientItemsCurrent.forEach { ingredientItem->
            if(!ingredientsListSP.contains(ingredientItem.ingredient)&& !ingredientListApp.contains(ingredientItem.ingredient)){
                ingredientsList.add(ingredientItem.ingredient)
            }
        }
        val json=Gson().toJson(ingredientsList)
        cnt.mFirstTimeModeSP.edit().putString(USER_INGREDIENTS_LIST_KEY_SP,json).apply()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveCocktailToFirebase(cocktail: Cocktail){
        val cnt = (applicationContext as Cocktails)
        saveCocktailDataToFirebase(cocktail)
        if(cnt.mUserInputs.getString(UPLOAD_IMG_PATH_KEY, null)!=null) {
            delCocktailImg(cocktail.name)
        }
        cocktail.image?.let { saveImgToFirebase(cocktail.name, it,cocktail.rotation) }
        if(intent.getBooleanExtra(EDIT_MODE_KEY,false)) {
            cnt.mUserCocktailsList.removeIf { it.name == cocktail.name }
        }
        cnt.addUserCocktail(cocktail)
    }

    @SuppressLint("LogNotTimber")
    private fun delCocktailImg(name:String) {
        val cnt = (applicationContext as Cocktails)
        val path = cnt.getUploadUserImgPath(name)
        val desertRef = cnt.mStorageRef.child(path)
        desertRef.delete()
    }

    @SuppressLint("LogNotTimber")
    private fun saveCocktailDataToFirebase(cocktail: Cocktail){
        val cnt = (applicationContext as Cocktails)
        cnt.mCocktailsRef.document(cocktail.name).set(cocktail)
            .addOnSuccessListener{
                if(intent.getBooleanExtra(EDIT_MODE_KEY,false)) {
                    messageToast("'${cocktail.name}' item was updated")
                }else {
                    messageToast("'${cocktail.name}' item was created")
                }
                Log.i("storage_new_cocktail",
                    "OnSuccess: Cocktail Name: ${cocktail.name}"
                );}
            .addOnFailureListener { Log.i(
                "storage_new_cocktail",
                "OnFailure: Cocktail Name: ${cocktail.name}"
            );}
    }

    private fun messageToast(message:String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("LogNotTimber")
    private fun saveImgToFirebase(imgName:String, imgPath:String, imgRotation:Float){
        val uploadImgUri = Uri.parse(imgPath)
        val bitmapUploadImg = getUploadUriToBitmap(imgRotation, uploadImgUri)
        val baos = ByteArrayOutputStream()
        bitmapUploadImg.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val cnt = (applicationContext as Cocktails)
        val path = cnt.getUploadUserImgPath(imgName)
        val mountainImagesRef = cnt.mStorageRef.child(path)
        val uploadTask = mountainImagesRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Log.i("upload_new_cocktail_img", "OnFailure: Cocktail Name: $imgName")
        }.addOnSuccessListener { Log.i("upload_new_cocktail_img", "OnSuccess: Cocktail Name: $imgName") }
    }

    private fun getUploadUriToBitmap(rotation:Float,uploadImgUri: Uri): Bitmap {
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uploadImgUri)
        val matrix= Matrix()
        matrix.setRotate(rotation)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_level3, menu)
        if(intent.getBooleanExtra(EDIT_MODE_KEY,false)){
            if (menu != null) {
                menu.findItem(R.id.create_button_menu).title = FINISH
            }
        }
        return true
    }

    private fun initButtons() {
        findViewById<ImageButton>(R.id.prevButton).setOnClickListener {
            onBackPressed()
        }

        findViewById<Button>(R.id.preview_button).setOnClickListener {
            val cocktail : Cocktail = getCocktail(true) ?: return@setOnClickListener
            val intent = Intent(applicationContext, ItemDetailsActivity::class.java)
            intent.putExtra("cocktail", cocktail)
            startActivity(intent)
        }
    }

    @SuppressLint("LongLogTag")
    private fun getCocktail(isForPreview:Boolean=false):Cocktail?{
        val c = (this.applicationContext as Cocktails)
        val cocktailName = c.mUserInputs.getString(COCKTAIL_NAME_KEY,null)
        val category = c.mUserInputs.getString(CATEGORY_KEY,null)
        var iconUri = c.mUserInputs.getString(ICON_KEY,null)
        if(!iconUri.isNullOrEmpty()){
            iconUri=iconUri.replace(".png","")
        }
        val uploadImgStr = c.mUserInputs.getString(UPLOAD_IMG_KEY,null)
        val rotation = c.mUserInputs.getFloat(ROTATE_UPLOAD_IMG_KEY, 0F)
        val ingredients=getIngredientsListStr()
        val ingredientsJson = c.mUserInputs.getString(INGREDIENT_LIST_JSON_STR_KEY,null)
        val steps= getPreparationListStr()
        val stepsJson = c.mUserInputs.getString(PREPARATION_LIST_JSON_STR_KEY,null)
        if (cocktailName.isNullOrEmpty() || category.isNullOrEmpty() || iconUri.isNullOrEmpty()
            || ingredients.isNullOrEmpty() ||ingredientsJson.isNullOrEmpty()|| steps.isNullOrEmpty()||
            stepsJson.isNullOrEmpty()) {
            return null
        }

        return Cocktail(
            cocktailName, category, steps, ingredients, iconUri,
            uploadImgStr, true, DEFAULT_GLASS, isForPreview, rotation,ingredientsJson,stepsJson
        )
    }

    private  fun getIngredientsListStr():ArrayList<String>{
        val ingredientsValList =getIngredients()
        val ingredientsStr=ArrayList<String>()
        for (element in ingredientsValList){
            ingredientsStr.add(element.getIngredientStrItem())
        }
        return ingredientsStr
    }

    private fun getIngredients():List<IngredientItem>{
        val c = (this.applicationContext as Cocktails)
        val ingredientJson = c.mUserInputs.getString(INGREDIENT_LIST_JSON_STR_KEY,null)
        if(!ingredientJson.isNullOrEmpty()) {
            return Gson().fromJson(ingredientJson, Array<IngredientItem>::class.java).asList()
        }
        return emptyList()

    }

    private fun getPreparation():List<StepItem>{
        val c = (this.applicationContext as Cocktails)
        val stepsJson = c.mUserInputs.getString(PREPARATION_LIST_JSON_STR_KEY,null)
        if(!stepsJson.isNullOrEmpty()) {
            return Gson().fromJson(stepsJson, Array<StepItem>::class.java).asList()
        }
        return emptyList()
    }

    private  fun getPreparationListStr():ArrayList<String>{
        val preparationValList=getPreparation()
        val preparationStr=ArrayList<String>()
        for (element in preparationValList){
            preparationStr.add(element.step)
        }
        return preparationStr
    }

}