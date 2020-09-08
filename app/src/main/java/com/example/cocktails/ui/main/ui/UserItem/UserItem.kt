package com.example.cocktails.ui.main.ui.UserItem

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.cocktails.Cocktails
import com.example.cocktails.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.squareup.picasso.Picasso


class UserItem : AppCompatActivity() {
    private lateinit var mUploadUserImgButton: Button
    private lateinit var mChooseImgButton: Button
    private lateinit var mImgView: ImageView
    private lateinit var mUserImg: ImageView
    private lateinit var mImgUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_item)

        //dropdown
//        val items = resources.getStringArray(R.array.cocktailTypes_array)
//        val adapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, items)
//        val editText:AutoCompleteTextView=findViewById(R.id.filled_exposed_dropdown)
//        editText.setAdapter(adapter)

        initNextButton()
        initCategory()
    }

    @SuppressLint("InflateParams")
    private fun initCategory(){
        resources.getStringArray(R.array.cocktailTypes_array).forEach { addChip(it, findViewById(R.id.selectCategoryChipGroup))}
    }

    private fun addChip(item: String, chipGroup: ChipGroup) {
        val chip = Chip(chipGroup.context)
        chip.text = item
        chip.isCheckable = true
        chipGroup.addView(chip)
    }

    private fun initNextButton(){
        mUploadUserImgButton = findViewById(R.id.upload_user_img_Button)
        mUserImg=findViewById(R.id.upload_user_img_TV)
        mImgView=findViewById(R.id.selected_img_image_view)
        mChooseImgButton=findViewById(R.id.select_img_Button)

        mUploadUserImgButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action=Intent.ACTION_GET_CONTENT
            startActivityForResult(intent,1)}

        mChooseImgButton.setOnClickListener {
          openImagesActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if((requestCode==1||requestCode==2) && resultCode== Activity.RESULT_OK&& data!=null && data.data!=null){
            mImgUri= data.data!!
            if(requestCode==1) {
                Picasso.with(this).load(mImgUri).into(mUserImg)
            }
            else{
                val applicationContext = (this.applicationContext as Cocktails)
                val ref = applicationContext.mStorageRef.child("cliparts/" + mImgUri)
                ref.downloadUrl.addOnSuccessListener {
                    Glide.with(applicationContext)
                        .load(it)
                        .apply(RequestOptions().placeholder(null).dontAnimate().fitCenter())
                        .into(mImgView)
                        .clearOnDetach()
                }
            }
        }
    }

    private fun openImagesActivity(){
        val intent=Intent(this,SelectedCocktailImgLevel1::class.java)
        intent.action=Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,2)
    }
}
