package com.example.cocktails

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class Cocktails : Application() {

    val FAVORITES = "FAVORITES"

    // shared data
    lateinit var mFavorites: SharedPreferences
    lateinit var mStorageRef: StorageReference

    override fun onCreate() {
        super.onCreate()
        mFavorites = this.getSharedPreferences(FAVORITES, Context.MODE_PRIVATE)
        mStorageRef = FirebaseStorage.getInstance().reference
    }
}