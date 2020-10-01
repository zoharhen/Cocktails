package com.example.cocktails.CustomItem

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cocktails.*
import kotlinx.android.synthetic.main.activity_user_item_level1.*
import java.io.ByteArrayOutputStream
import java.util.*


class UserItemLevel3 : AppCompatActivity() {
    val DEFAULT_CLIPART = "default"
    val DEFAULT_GLASS = "Water Glass.sfb"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item_level3)
        initViews()
        initButtons()
        initToolBar()
    }

    private fun initViews() {
        stepsView.setLabels(arrayOf("", "", ""))
            .setBarColorIndicator(resources.getColor(R.color.material_blue_grey_800))
            .setProgressColorIndicator(resources.getColor(R.color.stepBg))
            .setLabelColorIndicator(resources.getColor(R.color.stepBg))
            .setCompletedPosition(2)
            .drawView()
    }

    private fun initToolBar() {
        supportActionBar?.title = getString(R.string.title_user_item)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

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


    private fun createCocktail() {
        val cocktail : Cocktail = getCocktail() ?: return
        saveCocktailToFirebase(cocktail)
        val intent = Intent(applicationContext, ScrollingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun saveCocktailToFirebase(cocktail: Cocktail){
        val cnt = (applicationContext as Cocktails)
        saveCocktailDataToFirebase(cocktail)
        cocktail.image?.let { saveImgToFirebase(cocktail.name, it,cocktail.rotation) }
        cnt.addUserCocktail(cocktail)
    }

    @SuppressLint("LogNotTimber")
    private fun saveCocktailDataToFirebase(cocktail: Cocktail){
        val cnt = (applicationContext as Cocktails)
        cnt.mCocktailsRef.document(cocktail.name).set(cocktail)
            .addOnSuccessListener{
                Toast.makeText(this, "Create ${cocktail.name}",
                    Toast.LENGTH_LONG).show();
                Log.i("storage_new_cocktail",
                    "OnSuccess: Cocktail Name: ${cocktail.name}"
                );}
            .addOnFailureListener { Log.i(
                "storage_new_cocktail",
                "OnFailure: Cocktail Name: ${cocktail.name}"
            );}
    }

    @SuppressLint("LogNotTimber")
    private fun saveImgToFirebase(imgName:String, imgPath:String, imgRotation:Float){
        val uploadImgUri = Uri.parse(imgPath)
        val bitmapUploadImg = getUploadUriToBitmap(imgRotation, uploadImgUri)
        val baos = ByteArrayOutputStream()
        bitmapUploadImg.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val cnt = (applicationContext as Cocktails)
        val path=  cnt.getUploadImgPath(imgName)
        val mountainImagesRef = cnt.mStorageRef.child(path)
        val uploadTask = mountainImagesRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Log.i(
                "upload_new_cocktail_img",
                "OnFailure: Cocktail Name: ${imgName}"
            )
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            Log.i("upload_new_cocktail_img",
                "OnSuccess: Cocktail Name: ${imgName}")
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
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
        return true
    }

    private fun initButtons() {
        findViewById<ImageButton>(R.id.prevButton).setOnClickListener {
            onBackPressed()
        }

        findViewById<Button>(R.id.preview_button).setOnClickListener {


            val cocktail : Cocktail = getCocktail() ?: return@setOnClickListener

            val intent = Intent(applicationContext, ItemDetailsActivity::class.java)
            intent.putExtra("cocktail", cocktail)
            startActivity(intent)
        }
    }

    @SuppressLint("LongLogTag")
    private fun getCocktail():Cocktail?{
        val cocktailName = intent.getStringExtra(COCKTAIL_NAME_KEY)
        val category = intent.getStringExtra(CATEGORY_KEY)
        val iconUri = intent.getStringExtra(ICON_KEY)
        val uploadImgStr = intent.getStringExtra(UPLOAD_IMG_KEY)
        val rotation = intent.getFloatExtra(ROTATE_UPLOAD_IMG_KEY, 0F)
        var ingredients: ArrayList<String>? = null
        if (intent.getStringArrayListExtra(INGREDIENT_LIST_STR_KEY) != null) {
            ingredients = intent.getStringArrayListExtra(INGREDIENT_LIST_STR_KEY)
        }
        var steps: ArrayList<String>? = null
        if (intent.getStringArrayListExtra(PREPARATION_LIST_STR_KEY) != null) {
            steps = intent.getStringArrayListExtra(PREPARATION_LIST_STR_KEY)
        }
        
        if (cocktailName.isNullOrEmpty() || category.isNullOrEmpty() || iconUri.isNullOrEmpty()
            || ingredients.isNullOrEmpty() || steps.isNullOrEmpty() ) {
            return null
        }

        return Cocktail(
            cocktailName, category, steps, ingredients, DEFAULT_CLIPART,
            uploadImgStr, true, DEFAULT_GLASS, true, rotation
        )
    }

}