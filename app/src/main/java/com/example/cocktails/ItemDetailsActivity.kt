package com.example.cocktails

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.cocktails.ui.main.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout


class ItemDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabbed_item)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar1)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val cocktail = intent.getParcelableExtra<Cocktail>("cocktail")
        val activityTitle: TextView = findViewById(R.id.title)
        activityTitle.text = cocktail?.name

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