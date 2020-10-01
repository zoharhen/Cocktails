package com.example.cocktails

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


const val COLLECTION_PATH = "user_cocktails"
const val SUB_COLLECTION_PATH = "cocktails"
const val USER_ID_KEY_SP="user_id"

class Cocktails : Application() {

    val INGREDIENTS_FILTERS = "INGREDIENTSFILTERS"
    val TYPE_FILTERS = "TYPEFILTERS"
    val FAVORITES = "FAVORITES"
    val FIRST_TIME_MODE = "FIRSTMODE"



    // shared data
    lateinit var mFAuth: FirebaseAuth
    lateinit var mFavorites: SharedPreferences
    lateinit var mActiveIngredientsFilters: SharedPreferences
    lateinit var mActiveTypeFilters: SharedPreferences
    lateinit var mStorageRef: StorageReference
    lateinit var mFirstTimeModeSP: SharedPreferences
    private var mUserId: String = ""
    //    private var mUserId: String= "enavDebug"//todo remove only for debug
    var mUserCocktailsList =ArrayList<Cocktail>()
    lateinit var mCocktailsRef: CollectionReference


    override fun onCreate() {
        super.onCreate()
        mFAuth = FirebaseAuth.getInstance()
        mFavorites = this.getSharedPreferences(FAVORITES, Context.MODE_PRIVATE)
        mActiveIngredientsFilters = this.getSharedPreferences(
            INGREDIENTS_FILTERS,
            Context.MODE_PRIVATE
        )
        mActiveTypeFilters = this.getSharedPreferences(TYPE_FILTERS, Context.MODE_PRIVATE)
        mStorageRef = FirebaseStorage.getInstance().reference
        mFirstTimeModeSP = this.getSharedPreferences(FIRST_TIME_MODE, Context.MODE_PRIVATE)

        if(mUserId.isEmpty()){
            val userId=mFirstTimeModeSP.getString(USER_ID_KEY_SP,"")
            if (userId.isNullOrEmpty()){
                mUserId= java.util.UUID.randomUUID().toString()
                mFirstTimeModeSP.edit().putString(USER_ID_KEY_SP, mUserId).apply()
            }
            else{
                mUserId=userId
            }
        }
        mCocktailsRef = FirebaseFirestore.getInstance().collection(COLLECTION_PATH).document(mUserId).collection(
            SUB_COLLECTION_PATH
        )
        loadUserCocktailData()//todo check
    }

    fun getUploadUserImgPath(imageName: String):String{
        return "usersImages/$mUserId/$imageName.jpg"
    }

    @SuppressLint("LogNotTimber")
    private fun loadUserCocktailData() { //todo check fun
        mUserCocktailsList.clear()
        mCocktailsRef.get().addOnSuccessListener { queryDocumentSnapshot->
            for (doc in queryDocumentSnapshot) {
                val cocktail=doc.toObject(Cocktail::class.java)
                mUserCocktailsList.add(cocktail)
                Log.i("cocktails", cocktail.name)//todo remove only for debug
            }
        }
    }

    fun addUserCocktail(newCocktail: Cocktail){
        mUserCocktailsList.add(newCocktail)
    }

    fun removeUserCocktail(delCocktail:Cocktail){
        mUserCocktailsList.remove(delCocktail)
    }


}