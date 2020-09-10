package com.example.cocktails.CustomItem

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cocktails.Cocktails
import com.example.cocktails.R
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.storage.StorageReference


class CocktailIconLevel1 : AppCompatActivity(),ImageAdapter.ItemClickListener {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: ImageAdapter
    private lateinit var mIcons:ArrayList<IconItem>
    private lateinit var mDataRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selcect_cocktail_img_level1)

        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(this)

        val layoutManager = FlexboxLayoutManager()
        layoutManager.flexWrap = FlexWrap.WRAP

        mRecyclerView.layoutManager = layoutManager
        layoutManager.alignItems = AlignItems.BASELINE;
        layoutManager.justifyContent = JustifyContent.CENTER;
//        mRecyclerView.layoutManager=GridLayoutManager(this, 3)
        mIcons = ArrayList()

        mDataRef = (this.applicationContext as Cocktails).mStorageRef
        val listRef=mDataRef.child("cliparts")
        listRef.listAll()
            .addOnSuccessListener { listResult ->
                listResult.prefixes.forEach { prefix ->
                    // All the prefixes under listRef.
                    // You may call listAll() recursively on them.

                }

                listResult.items.forEach { item ->
                    // All the items under listRef.
                    val iconItem: IconItem? = IconItem(item.name, item.path)
                    if (iconItem != null) {
                        mIcons.add(iconItem)
                    }
                }
                mAdapter = ImageAdapter(applicationContext, mIcons, this)
                //adding adapter to recyclerview
                mRecyclerView.adapter = mAdapter;
            }
            .addOnFailureListener {
                // Uh-oh, an error occurred!
            }

    }

    override fun onItemClicked(position: Int) {
        Toast.makeText(this, "Cell clicked", Toast.LENGTH_SHORT).show()//todo remove?
        val imgUrl:String=mIcons[position].name
        returnImgResult(imgUrl)
    }

    private fun returnImgResult(imgUrl: String){
        val intentBack= Intent()
        intentBack.data= Uri.parse(imgUrl)
        setResult(RESULT_OK, intentBack)
        finish()
    }


}