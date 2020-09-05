package com.example.cocktails.ui.main

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cocktails.Cocktail
import com.example.cocktails.Cocktails
import com.example.cocktails.R
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip


class RecipeFragment : Fragment(), PreparationAdapter.ViewHolder.ClickListener {

    private var mAdapterPreparation: PreparationAdapter? = null
    lateinit var rootView: View
    lateinit var cocktail: Cocktail

    companion object {
        fun newInstance(cocktail: Cocktail): RecipeFragment? {
            val args = Bundle()
            args.putParcelable("cocktail", cocktail)
            val f = RecipeFragment()
            f.arguments = args
            return f
        }
    }

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cocktail = arguments?.getParcelable<Cocktail>("cocktail")!!
        retainInstance = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Override
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.recipe_item, container, false)
        this.initViews()
        rootView.findViewById<ScrollView>(R.id.recipe_item).post {
            rootView.findViewById<ScrollView>(R.id.recipe_item).fullScroll(View.FOCUS_UP) // workaround, as tabs hide the ScrollView
        }

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initViews() {
        (activity?.applicationContext as Cocktails).mStorageRef.child("images/" + cocktail.image + ".jpg")
            .downloadUrl.addOnSuccessListener { img ->
                context?.let { it ->
                    Glide.with(it)
                        .load(img)
                        .into(rootView.findViewById(R.id.iv_cocktail))
                }
            }

        if (cocktail.isCustom) {
            rootView.findViewById<ImageButton>(R.id.editAction).visibility = ImageButton.VISIBLE
            rootView.findViewById<ImageButton>(R.id.deleteAction).visibility = ImageButton.VISIBLE
            rootView.findViewById<View>(R.id.editActionSeparator).visibility = View.VISIBLE
            rootView.findViewById<View>(R.id.deleteActionSeparator).visibility = View.VISIBLE
        }

        val ingredients = rootView.findViewById<TextView>(R.id.ingredientContent)
        ingredients.text = cocktail.ingredients.joinToString(separator = "\n")

        val isFavorite = (activity?.applicationContext as Cocktails).mFavorites.getBoolean(cocktail.name, false)
        val favorite = rootView.findViewById<ImageButton>(R.id.favoriteAction)
        favorite.setImageResource(if (isFavorite) R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp)

        favorite.setOnClickListener { v: View ->
            val oldVal = (activity?.applicationContext as Cocktails).mFavorites.getBoolean(cocktail.name, false)
            (activity?.applicationContext as Cocktails).mFavorites.edit()
                .putBoolean(cocktail.name, !oldVal).apply()
            favorite.setImageResource(if (!oldVal) R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp)
        }

        this.initPreparationSection()
        this.initTooltipIfNeeded()

        //todo: Einav: add edit + delete 'onClick' methods
    }

    private fun initTooltipIfNeeded() {
        if ((activity?.applicationContext as Cocktails).mFirstTimeModeSP.getBoolean("firstMode", true)) {
            (activity?.applicationContext as Cocktails).mFirstTimeModeSP.edit().putBoolean("firstMode", false).apply()

            val preparationContext = rootView.findViewById<RecyclerView>(R.id.recyclerPreparation)
            preparationContext.setOnSystemUiVisibilityChangeListener {
                SimpleTooltip.Builder(context)
                    .anchorView(preparationContext)
                    .text("Press a long click in order to mark a step as done  X")
                    .gravity(Gravity.TOP)
                    .animated(true)
                    .transparentOverlay(true)
                    .dismissOnInsideTouch(true)
                    .dismissOnOutsideTouch(false)
                    .build()
                    .show()
                return@setOnSystemUiVisibilityChangeListener
            }
        }
    }

    private fun initPreparationSection() {
        val recyclerViewPreparation =  rootView.findViewById(R.id.recyclerPreparation) as RecyclerView
        mAdapterPreparation = context?.let { PreparationAdapter(it, generatePreparation()!!, this) }
        val mLayoutManagerPreparation = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerViewPreparation.layoutManager = mLayoutManagerPreparation
        recyclerViewPreparation.itemAnimator = DefaultItemAnimator()
        recyclerViewPreparation.adapter = mAdapterPreparation
    }

    override fun onItemLongClicked(position: Int): Boolean {
        toggleSelection(position)
        return true
    }

    private fun generatePreparation(): List<ItemPreparation>? {
        val itemList: MutableList<ItemPreparation> = ArrayList()
        for (i in cocktail.steps.indices) {
            itemList.add(ItemPreparation(cocktail.steps[i], i+1))
        }
        return itemList
    }

    private fun toggleSelection(position: Int) {
        mAdapterPreparation!!.toggleSelection(position)
        val rlShare = rootView.findViewById(R.id.rl_share) as RelativeLayout
        if (position == mAdapterPreparation!!.itemCount - 1) {
            rlShare.visibility = View.VISIBLE
        }
    }
}