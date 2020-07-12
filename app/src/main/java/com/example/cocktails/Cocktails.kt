package com.example.cocktails

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class Cocktails : Application() {

    // shared data
    lateinit var mStorageRef: StorageReference

    override fun onCreate() {
        super.onCreate()
        mStorageRef = FirebaseStorage.getInstance().reference
    }
}