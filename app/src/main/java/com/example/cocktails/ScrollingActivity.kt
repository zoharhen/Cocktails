package com.example.cocktails

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.example.cocktails.CustomItem.UserItemLevel1
import com.example.cocktails.R.*
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_scrolling.*
import kotlinx.android.synthetic.main.content_scrolling.*
import kotlinx.android.synthetic.main.filter_dialog.view.*
import ru.nikartm.support.ImageBadgeView


@Parcelize
data class Cocktail(val name: String, val type: String, val steps: ArrayList<String>,
                    val ingredients: ArrayList<String>, val clipart: String, val image: String,
                    val isCustom: Boolean = false, val glass: String?, val isReview:Boolean=false,val rotation:Float=0F): Parcelable

class ScrollingActivity : AppCompatActivity() {

    private var gridViewAdapter: CocktailItemAdapter? = null
    private lateinit var recyclerView: RecyclerView
    private var filterDialog : AlertDialog? = null
    private var filterIcon: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_scrolling)
        setSupportActionBar(toolbar)

        initAdapter()
        initRecyclerview()
        initUserItemButton()

        gridViewAdapter?.filter?.filter("filterDialog") // update view by current selected filters

    }

    public override fun onStart() {
        super.onStart()
        val mAuth = (applicationContext.applicationContext as Cocktails).mAuth
//        mAuth.signInAnonymously()
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val user: FirebaseUser? = mAuth.currentUser
//                    // todo: einav - get user custom cocktails objects? (remember to save the object once being created). maybe use SP instead?
//                } else {
//                    Toast.makeText(applicationContext, "Error loading data", Toast.LENGTH_SHORT).show()
//                }
//            }
    }

    private fun initRecyclerview() {
        recyclerView = findViewById(id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, getColumnNum())
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = gridViewAdapter
        recyclerView.itemAnimator = null
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() { })
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recyclerView.layoutManager = GridLayoutManager(this, getColumnNum())
        if (filterDialog != null) {
            setFilterDialogLayoutSize()
        }
    }

    private fun getColumnNum() : Int {
        //        val columns = resources.getInteger(R.integer.gridColumnNum)
        val screenWidthDp: Float = resources.displayMetrics.widthPixels / resources.displayMetrics.density
       return ((screenWidthDp / 202 + 0.5).toInt()) // +0.5 for correct rounding to int.
    }

    private fun initAdapter() {
        val jsonString = applicationContext.assets.open("predefined.json").bufferedReader().use { it.readText() }
        val listCocktailType = object : TypeToken<List<Cocktail>>() {}.type
        val items = Gson().fromJson<List<Cocktail>>(jsonString, listCocktailType)

        gridViewAdapter = CocktailItemAdapter(this, items)

        gridViewAdapter?.onItemClick = { cocktail ->
            val intent = Intent(applicationContext, ItemDetailsActivity::class.java)
            intent.putExtra("cocktail", cocktail)
            startActivity(intent)
        }

        gridViewAdapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                no_data_textView.visibility = if (gridViewAdapter!!.itemCount == 0) View.VISIBLE else View.INVISIBLE
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)

        filterIcon = menu.findItem(id.action_filter).actionView
        menu.findItem(id.action_filter).actionView.setOnClickListener { openFilterDialog() }
        refreshFilterBadge()
        initSearchView(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun refreshFilterBadge() {
        if (filterIcon != null) {
            val cnt = (applicationContext as Cocktails)
            val isFilterOff = cnt.mActiveTypeFilters.all.keys.isEmpty() && cnt.mActiveIngredientsFilters.all.keys.isEmpty()
            filterIcon?.findViewById<ImageBadgeView>(id.filter_icon)?.badgeValue = if(isFilterOff) 0 else 1
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initSearchView(menu: Menu) {
        val searchView: SearchView = menu.findItem(id.action_search).actionView as SearchView
        searchView.findViewById<AutoCompleteTextView>(id.search_src_text).threshold = 1
        val searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        searchPlate.hint = "Search"
        searchPlate.setTextAppearance(style.chipText)
        searchPlate.background = resources.getDrawable(drawable.rounded_corner, theme)
        searchPlate.setHintTextColor(Color.parseColor("#D5D3D3"))
        searchPlate.setTextColor(Color.parseColor("#D5D3D3"))
        val searchPlateView: View = searchView.findViewById(androidx.appcompat.R.id.search_plate)
        searchPlateView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                gridViewAdapter?.filter?.filter(newText)
                return false
            }
        })
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchPlate.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            id.action_filter -> openFilterDialog()
            id.action_favorites -> openFilteredActivityView("favorites")
            id.action_myCocktails -> openFilteredActivityView("custom")
            id.action_help -> openContactUsActivity()
            android.R.id.home -> {
                val options: ActivityOptions = ActivityOptions.makeCustomAnimation(this, 0,0)
                finish()
                this.startActivity(intent, options.toBundle())
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openFilteredActivityView(filter: String) : Boolean {
        val filteredActivity = Intent(this, ScrollingActivity::class.java)
        filteredActivity.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        filteredActivity.putExtra("FilteredActivity", filter)

        gridViewAdapter?.filter?.filter(filter)
        startActivity(filteredActivity)

        toolbar.menu.setGroupVisible(id.menuGroup, false)
        toolbar_layout.title = if (filter == "favorites") resources.getString(string.favorites) else resources.getString(
            string.my_cocktails)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("InflateParams")
    private fun openFilterDialog(): Boolean {
        val typeFiltersSP = (applicationContext as Cocktails).mActiveTypeFilters
        val ingredientsFiltersSP = (applicationContext as Cocktails).mActiveIngredientsFilters

        val layout = layoutInflater.inflate(layout.filter_dialog, null) as ScrollView
        resources.getStringArray(array.cocktailTypes_array).forEach { addChip(it, layout.CategoryChipGroup, typeFiltersSP) }
        resources.getStringArray(array.ingredients).forEach { addChip(it, layout.IngredientChipGroup, ingredientsFiltersSP) }

        layout.findViewById<Button>(R.id.apply_btn).setOnClickListener {
            val typeFiltersSpEditor = typeFiltersSP.edit()
            typeFiltersSpEditor.clear()
            layout.CategoryChipGroup.checkedChipIds.forEach {
                val selectedChip = layout.CategoryChipGroup.findViewById<Chip>(it)
                typeFiltersSpEditor.putBoolean(selectedChip.text as String?, true)
            }
            typeFiltersSpEditor.apply()

            val ingredientsFiltersSpEditor = ingredientsFiltersSP.edit()
            ingredientsFiltersSpEditor.clear()
            layout.IngredientChipGroup.checkedChipIds.forEach {
                val selectedChip = layout.IngredientChipGroup.findViewById<Chip>(it)
                ingredientsFiltersSpEditor.putBoolean(selectedChip.text as String?, true)
            }
            ingredientsFiltersSpEditor.apply()
            gridViewAdapter?.filter?.filter("filterDialog")
            refreshFilterBadge()
            filterDialog?.dismiss()
        }

        layout.findViewById<ImageButton>(id.clear_btn).setOnClickListener {
            val cnt = (applicationContext as Cocktails)
            cnt.mActiveTypeFilters.edit().clear().apply()
            cnt.mActiveIngredientsFilters.edit().clear().apply()
            gridViewAdapter?.filter?.filter("filterDialog")
            refreshFilterBadge()
            filterDialog?.dismiss()
        }

        filterDialog = AlertDialog.Builder(this).setView(layout).create()
        setFilterDialogLayoutSize()
        filterDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        filterDialog!!.setOnDismissListener { filterDialog = null }

        return true
    }

    private fun setFilterDialogLayoutSize() {
        if (filterDialog != null) {
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(filterDialog!!.window?.attributes)
            lp.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            lp.height = (resources.displayMetrics.heightPixels * 0.7).toInt()
            filterDialog!!.show()
            filterDialog!!.window?.attributes = lp
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addChip(item: String, chipGroup: ChipGroup, sp: SharedPreferences) {
        val chip = Chip(chipGroup.context)
        chip.text = item
        chip.setTextAppearance(style.chipText)
        chip.isCheckable = true
        chip.isChecked = sp.getBoolean(item, false)
        chip.chipBackgroundColor = getColorStateList(color.chipColor)
        chipGroup.addView(chip)
    }

    private fun initUserItemButton(){
        val userItemButton:View= findViewById(id.userItemFab)
        userItemButton.setOnClickListener {
        val createNewItemIntent = Intent(applicationContext, UserItemLevel1::class.java)
        startActivity(createNewItemIntent) }
    }

    private fun openContactUsActivity(): Boolean {
        val intent = Intent(applicationContext, ContactUsActivity::class.java)
        startActivity(intent)
        return true
    }

}