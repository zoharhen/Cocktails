package com.example.cocktails

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import com.example.cocktails.ItemDetails.ARFragment
import com.example.cocktails.ItemDetails.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout


class ItemDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabbed_item)

        val cocktail = intent.getParcelableExtra<Cocktail>("cocktail")

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, cocktail!!)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar1)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val activityTitle: TextView = findViewById(R.id.title)
        activityTitle.text = cocktail.name

        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
}