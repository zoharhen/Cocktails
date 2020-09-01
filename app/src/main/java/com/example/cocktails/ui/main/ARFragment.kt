package com.example.cocktails.ui.main

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.cocktails.Cocktail
import com.example.cocktails.Cocktails
import com.example.cocktails.R
import com.example.cocktails.ScrollingActivity


class ARFragment(context: Context) : Fragment() {

    private var TAG: String = "COMPATIBILITY"
    private var MIN_OPENGL_VERSION: Double = 3.0

//    private lateinit var pageViewModel: PageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).
//        apply {setIndex(arguments?.getInt(PlaceholderFragment.ARG_SECTION_NUMBER) ?: 1)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Check compatibility to AR. if none, just return.
        if (!checkCompatibility(requireActivity())) {
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        // Create the AR layout and return it
        val root = inflater.inflate(R.layout.ar_layout, container, false)
        val textView: TextView = root.findViewById(R.id.section_label)
//        pageViewModel.text.observe(viewLifecycleOwner, Observer<String> {
//            textView.text = it
//        })

        return root
    }

    override fun onResume() {
        super.onResume()
    }

    fun checkCompatibility(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        val openGlVersionString =
            (activity.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

}