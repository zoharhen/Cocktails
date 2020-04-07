package com.example.cocktails

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.example.cocktails.ui.main.SectionsPagerAdapter
import kotlinx.android.synthetic.main.activity_item_details.*

class Item_details_activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        val cocktail = intent.getParcelableExtra<Cocktail>("cocktail")

        val activityTitle: TextView = findViewById(R.id.title)
        activityTitle.setText(cocktail?.name)
    }
}