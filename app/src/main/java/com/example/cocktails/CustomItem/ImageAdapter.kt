package com.example.cocktails.CustomItem

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.cocktails.Cocktails
import com.example.cocktails.R
import kotlinx.android.parcel.Parcelize

@Parcelize
class IconItem(val name: String, val url: String) : Parcelable

class ImageAdapter internal constructor(
    context: Context,
    data: List<IconItem>,
    private val clickListener: ItemClickListener
) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    private val mContext: Context = context
    private val mIcons: List<IconItem> = data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.icon_selected_item, parent, false)
        return ImageViewHolder(v);
    }

    override fun getItemCount(): Int {
        return mIcons.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val curIcon: IconItem = mIcons[position]
        val applicationContext = (mContext.applicationContext as Cocktails)
        val ref = applicationContext.mStorageRef.child("cliparts/" + curIcon.name)
        ref.downloadUrl.addOnSuccessListener {
            Glide.with(applicationContext)
                .load(it)
                .apply(RequestOptions().placeholder(null).dontAnimate().fitCenter())
                .into(holder.imageView)
                .clearOnDetach()
        }
    }

    inner class ImageViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView),View
    .OnClickListener {
        var imageView: ImageView = itemView.findViewById(R.id.selected_img_image_view)
        override fun onClick(v: View?) {
            val position:Int=adapterPosition
            if(position!=RecyclerView.NO_POSITION){
                clickListener.onItemClicked(position)
            }
        }

        init {
            itemView.setOnClickListener(this)
        }

    }

    interface ItemClickListener {
        fun onItemClicked(position: Int)
    }

}

