package com.example.cocktails

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class Cocktails : Application() {

    val FAVORITES = "FAVORITES"
    val FIRST_TIME_MODE = "FIRSTMODE"

    // shared data
    lateinit var mAuth: FirebaseAuth
    lateinit var mFavorites: SharedPreferences
    lateinit var mStorageRef: StorageReference
    lateinit var mFirstTimeModeSP: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        mAuth = FirebaseAuth.getInstance()
        mFavorites = this.getSharedPreferences(FAVORITES, Context.MODE_PRIVATE)
        mStorageRef = FirebaseStorage.getInstance().reference
        mFirstTimeModeSP = this.getSharedPreferences(FIRST_TIME_MODE, Context.MODE_PRIVATE)
    }
}