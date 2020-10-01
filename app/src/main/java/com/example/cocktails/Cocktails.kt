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
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

const val COLLECTION_PATH = "user_cocktails"
const val SUB_COLLECTION_PATH = "cocktails"


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
//    private val mUserId: String= UUID.randomUUID().toString()//todo. check for another option
    private val mUserId: String= "enavDebug"//todo remove only for debug
    private var mUserCocktailsMap = HashMap<String, Cocktail>()
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
        mCocktailsRef = FirebaseFirestore.getInstance().collection(COLLECTION_PATH).document(mUserId).collection(
            SUB_COLLECTION_PATH)
//        loadUserCocktailData()
    }

    fun getUploadImgPath(imageName:String):String{
        return "usersImages/$mUserId/$imageName.jpg"
    }

    @SuppressLint("LogNotTimber")
    private fun loadUserCocktailData() { //todo check fun
        mUserCocktailsMap.clear()
        mCocktailsRef.get().addOnSuccessListener { queryDocumentSnapshot->
            for (doc in queryDocumentSnapshot) {
                val cocktail=doc.toObject(Cocktail::class.java)
                mUserCocktailsMap[cocktail.name]=cocktail
                Log.i("cocktails",cocktail.toString())//todo remove only for debug
            }
        }
    }

    fun getAllUsersCocktailsMap() :HashMap<String, Cocktail>
    {
        return HashMap(mUserCocktailsMap)
    }

    fun getAllUsersCocktailsList() :ArrayList<Cocktail>
    {

        return ArrayList<Cocktail>(mUserCocktailsMap.values)
    }

    fun addUserCocktail(newCocktail: Cocktail){
        mUserCocktailsMap[newCocktail.name]=newCocktail
    }

}