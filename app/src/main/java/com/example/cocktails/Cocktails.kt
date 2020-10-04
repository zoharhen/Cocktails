package com.example.cocktails

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


const val COLLECTION_PATH = "user_cocktails"
const val SUB_COLLECTION_PATH = "cocktails"
const val USER_ID_KEY_SP="user_id"

class Cocktails : Application() {

    private val INGREDIENTS_FILTERS = "INGREDIENTSFILTERS"
    private val TYPE_FILTERS = "TYPEFILTERS"
    private val FAVORITES = "FAVORITES"
    private val FIRST_TIME_MODE = "FIRSTMODE"
    private val USER_INPUT = "USER_INPUT"

    // shared data
    lateinit var mFAuth: FirebaseAuth
    lateinit var mFavorites: SharedPreferences
    lateinit var mActiveIngredientsFilters: SharedPreferences
    lateinit var mActiveTypeFilters: SharedPreferences
    lateinit var mStorageRef: StorageReference
    lateinit var mFirstTimeModeSP: SharedPreferences
    private var mUserId: String = ""
    var mUserCocktailsList = ArrayList<Cocktail>()
    lateinit var mCocktailsRef: CollectionReference
    lateinit var mUserInputs:SharedPreferences

    override fun onCreate() {
        super.onCreate()
        mFAuth = FirebaseAuth.getInstance()
        mFavorites = this.getSharedPreferences(FAVORITES, Context.MODE_PRIVATE)
        mActiveIngredientsFilters = this.getSharedPreferences(INGREDIENTS_FILTERS, Context.MODE_PRIVATE)
        mActiveTypeFilters = this.getSharedPreferences(TYPE_FILTERS, Context.MODE_PRIVATE)
        mStorageRef = FirebaseStorage.getInstance().reference
        mFirstTimeModeSP = this.getSharedPreferences(FIRST_TIME_MODE, Context.MODE_PRIVATE)
        mUserInputs=this.getSharedPreferences(USER_INPUT,Context.MODE_PRIVATE)

        if(mUserId.isEmpty()){
            val userId= mFirstTimeModeSP.getString(USER_ID_KEY_SP,"")
            if (userId.isNullOrEmpty()){
                mUserId = java.util.UUID.randomUUID().toString()
                while (checkIfUserIdAlreadyExist(mUserId)){
                    mUserId = java.util.UUID.randomUUID().toString()
                }
                mFirstTimeModeSP.edit().putString(USER_ID_KEY_SP, mUserId).apply()
            }
            else{
                mUserId = userId
            }
        }
        mCocktailsRef = FirebaseFirestore.getInstance().collection(COLLECTION_PATH).document(mUserId).collection(
            SUB_COLLECTION_PATH
        )
        loadUserCocktailData()
    }

    fun getUploadUserImgPath(imageName: String):String{
        return "usersImages/$mUserId/$imageName.jpg"
    }

    private fun loadUserCocktailData() {
        mUserCocktailsList.clear()
        val asyncGetCustomCocktails = mCocktailsRef.get()
        asyncGetCustomCocktails.addOnCompleteListener { task ->
            for (doc in task.result!!) {
                val cocktail= doc.toObject(Cocktail::class.java)
                mUserCocktailsList.add(cocktail)
            }
        }
    }

    fun addUserCocktail(newCocktail: Cocktail){
        mUserCocktailsList.add(newCocktail)
    }

    fun removeUserCocktail(delCocktail:Cocktail){
        mUserCocktailsList.remove(delCocktail)
    }


    private fun checkIfUserIdAlreadyExist(idUser:String):Boolean{ //todo check again
        var isExist=false
        val userIdRef= FirebaseFirestore.getInstance().collection(COLLECTION_PATH)
        userIdRef.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if(document.id==idUser) {
                        isExist = true
                    }
                }
            }
        return isExist
    }

}

