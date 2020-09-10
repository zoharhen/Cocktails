package com.example.cocktails.ItemDetails

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.cocktails.Cocktail
import com.example.cocktails.R

private val TAB_TITLES = arrayOf(
    R.string.tab_text_2,
    R.string.tab_text_1
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager, private val cocktail: Cocktail) :
    FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    lateinit var recipeFragmentInstance: Fragment
    private val arFragmentInstance: Fragment = ARFragment.newInstance(cocktail, this) as Fragment

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        when (position) {
            // Recipe Tab
            0 -> {
                recipeFragmentInstance = RecipeFragment.newInstance(cocktail) as Fragment
                return recipeFragmentInstance
            }
            // AR tab
            1 -> return arFragmentInstance// ARFragment.newInstance(cocktail, this) as Fragment
        }

        // Return a PlaceholderFragment (defined as a static inner class below).
        return PlaceholderFragment.newInstance(position + 1)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return TAB_TITLES.size
    }
}