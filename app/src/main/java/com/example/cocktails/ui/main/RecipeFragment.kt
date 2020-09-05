package com.example.cocktails.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cocktails.Cocktail
import com.example.cocktails.Cocktails
import com.example.cocktails.R


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
    }

    @Override
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.recipe_item, container, false)
        this.initViews()
        return rootView
    }

    private fun initViews() {
        (activity?.applicationContext as Cocktails).mStorageRef.child("cliparts/" + cocktail.clipart + ".png")
            .downloadUrl.addOnSuccessListener { img ->
                context?.let { it ->
                    Glide.with(it)
                        .load(img)
                        .into(rootView.findViewById(R.id.iv_cocktail))
                }
            }
        val ingredients = rootView.findViewById<TextView>(R.id.ingredientContent)
        ingredients.text = cocktail.ingredients.joinToString(separator = "\n")
        this.initPreparationSection()

        val preparation = rootView.findViewById<TextView>(R.id.preparationSetion)
        preparation.setOnHoverListener { v, _ ->
            TooltipCompat.setTooltipText(v,"Click a step in order to play the text.\n Press a long click in order to mark a step as done"
            )
            return@setOnHoverListener true
        }

        //        val image: ImageView = rootView.findViewById(R.id.image) as ImageView
//        Glide.with(this)
//            .load(Uri.parse("https://images.pexels.com/photos/140831/pexels-photo-140831.jpeg?w=1260&h=750&auto=compress&cs=tinysrgb"))
//            .into(image)
    }

    private fun initPreparationSection() {
        val recyclerViewPreparation =  rootView.findViewById(R.id.recyclerPreparation) as RecyclerView
        mAdapterPreparation = context?.let { PreparationAdapter(it, generatePreparation()!!, this) }
        val mLayoutManagerPreparation = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerViewPreparation.layoutManager = mLayoutManagerPreparation
        recyclerViewPreparation.itemAnimator = DefaultItemAnimator()
        recyclerViewPreparation.adapter = mAdapterPreparation
    }

    override fun onItemClicked(position: Int) {
        TODO("Not yet implemented")
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