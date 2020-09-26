package com.example.cocktails.ItemDetails

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.UriUtils.file2Uri
import com.bumptech.glide.Glide
import com.example.cocktails.BuildConfig
import com.example.cocktails.Cocktail
import com.example.cocktails.Cocktails
import com.example.cocktails.R
import developer.shivam.crescento.CrescentoImageView
import github.nisrulz.screenshott.ScreenShott
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import net.gotev.speech.Speech
import net.gotev.speech.TextToSpeechCallback
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class RecipeFragment : Fragment(), PreparationAdapter.ViewHolder.ClickListener {

    private var mAdapterPreparation: PreparationAdapter? = null
    private lateinit var rootView: View
    lateinit var cocktail: Cocktail
    private lateinit var longPressTooltipBuilder: SimpleTooltip.Builder
    lateinit var longPressTooltip: SimpleTooltip
    private var isTtsOn: Boolean = false

    companion object {
        fun newInstance(cocktail: Cocktail): RecipeFragment? {
            val args = Bundle()
            args.putParcelable("cocktail", cocktail)
            val f = RecipeFragment()
            f.arguments = args
            return f
        }

        const val PERMISSION_EXTERNAL_STORAGE = 222
    }

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cocktail = arguments?.getParcelable("cocktail")!!
        retainInstance = true

        Speech.init(context, context?.packageName);
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

    override fun onResume() {
        super.onResume()
        view?.visibility = View.VISIBLE
        this.initTooltipIfNeeded()
    }

    override fun onPause() {
        super.onPause()
        dismissTooltip()
//        view?.visibility = View.GONE
    }

    fun dismissTooltip() {
        if (this::longPressTooltip.isInitialized && longPressTooltip.isShowing) {
            val oldVal = (activity?.applicationContext as Cocktails).mFirstTimeModeSP.getBoolean("recipeTab", true)
            longPressTooltip.dismiss()
            (activity?.applicationContext as Cocktails).mFirstTimeModeSP.edit().putBoolean("recipeTab", oldVal).apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // prevent memory leaks when activity is destroyed
        Speech.getInstance().shutdown()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initViews(withTooltip: Boolean = true) {
        if(cocktail.isReview){
            val uploadImgUri= Uri.parse(cocktail.image)
            val bitmapUploadImg=getUploadUriToBitmap(cocktail.rotation,uploadImgUri)
            rootView.findViewById<CrescentoImageView>(R.id.iv_cocktail).setImageBitmap(bitmapUploadImg)
        }
        else {
            (activity?.applicationContext as Cocktails).mStorageRef.child("images/" + cocktail.image + ".jpg")
                .downloadUrl.addOnSuccessListener { img ->
                    context?.let { it ->
                        Glide.with(it)
                            .load(img)
                            .into(rootView.findViewById(R.id.iv_cocktail))
                    }
                }
        }
        rootView.findViewById<TextView>(R.id.tv_cocktailTitle).text = cocktail.name
        rootView.findViewById<TextView>(R.id.ingredientContent).text = cocktail.ingredients.joinToString(separator = "\n")

        this.initCustomCocktailButtons()
        this.initFavoriteButton()
        this.initPreparationSection()
        if (withTooltip) {
            this.initTooltipIfNeeded()
        }
        this.initTtsButton()
        this.initShareButtons()
    }

    private fun initTtsButton() {
        val ttsButton = rootView.findViewById<ImageButton>(R.id.tts)
        ttsButton.setOnClickListener {
            if (!isTtsOn) {
                Speech.getInstance().setLocale(Locale.US)
                Speech.getInstance().setTextToSpeechRate(0.65F)
                Speech.getInstance().say(getString(R.string.ingredients) + "\n\n\n\n" +
                        cocktail.ingredients.joinToString(separator = "\n\n") +
                        "\n\n\n" + getString(R.string.preparation) + "\n\n\n\n" +
                        "\n\n\n" + cocktail.steps.joinToString(separator = "\n\n"),
                    object : TextToSpeechCallback {
                        override fun onError() { }

                        override fun onStart() {
                            isTtsOn = true
                            ttsButton.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp)
                        }
                        override fun onCompleted() {
                            isTtsOn = false
                            ttsButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp)
                        }
                    })
            } else {
                Speech.getInstance().stopTextToSpeech()
                isTtsOn = false
                ttsButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp)
            }
        }
    }

    private fun initCustomCocktailButtons() {
        if (cocktail.isCustom) {
            rootView.findViewById<ImageButton>(R.id.editAction).visibility = ImageButton.VISIBLE
            rootView.findViewById<ImageButton>(R.id.deleteAction).visibility = ImageButton.VISIBLE
            rootView.findViewById<View>(R.id.editActionSeparator).visibility = View.VISIBLE
            rootView.findViewById<View>(R.id.deleteActionSeparator).visibility = View.VISIBLE

            rootView.findViewById<ImageButton>(R.id.favoriteAction).setPadding(22,0,22,0)
            rootView.findViewById<ImageButton>(R.id.editAction).setPadding(22,0,22,0)
            rootView.findViewById<ImageButton>(R.id.deleteAction).setPadding(22,0,22,0)
            rootView.findViewById<ImageButton>(R.id.tts).setPadding(22,0,22,0)
            rootView.findViewById<ImageButton>(R.id.share).setPadding(22,0,22,0)
        }

        //todo: Einav: add edit + delete 'onClick' methods
    }

    private fun initFavoriteButton() {
        val isFavorite = (activity?.applicationContext as Cocktails).mFavorites.getBoolean(cocktail.name, false)
        val favorite = rootView.findViewById<ImageButton>(R.id.favoriteAction)
        favorite.setImageResource(if (isFavorite) R.drawable.ic_favorite_full_black else R.drawable.ic_favorite_empty_black)

        favorite.setOnClickListener {
            val oldVal = (activity?.applicationContext as Cocktails).mFavorites.getBoolean(cocktail.name, false)
            (activity?.applicationContext as Cocktails).mFavorites.edit()
                .putBoolean(cocktail.name, !oldVal).apply()
            favorite.setImageResource(if (!oldVal) R.drawable.ic_favorite_full_black else R.drawable.ic_favorite_empty_black)
        }
    }

    private fun initTooltipIfNeeded() {
        if ((activity?.applicationContext as Cocktails).mFirstTimeModeSP.getBoolean("recipeTab", true)) {
            val preparationContext = rootView.findViewById<RecyclerView>(R.id.recyclerPreparation)
            preparationContext.setOnSystemUiVisibilityChangeListener {
                longPressTooltipBuilder = SimpleTooltip.Builder(context)
                    .anchorView(preparationContext)
                    .text("Press a long click in order to mark a step as done  X")
                    .gravity(Gravity.TOP)
                    .animated(true)
                    .transparentOverlay(true)
                    .dismissOnInsideTouch(true)
                    .onDismissListener {
                        (activity?.applicationContext as Cocktails).mFirstTimeModeSP.edit().putBoolean("recipeTab", false).apply()
                    }
                    .dismissOnOutsideTouch(false)
                longPressTooltip = longPressTooltipBuilder.build()
                longPressTooltip.show()
                return@setOnSystemUiVisibilityChangeListener
            }
        }
    }

    private fun initShareButtons() {

        val shareCocktail = rootView.findViewById<ImageButton>(R.id.share)
        shareCocktail.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23 && !checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_EXTERNAL_STORAGE, true)
            } else {
                val bitmap: Bitmap = getScreenShot()
                val screenshot : File = ScreenShott.getInstance().saveScreenshotToPicturesFolder(context, bitmap, cocktail.name)
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_STREAM, file2Uri(screenshot))
                sendIntent.type = "image/jpeg"
                sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(sendIntent)
            }
        }

        val shareApp = rootView.findViewById<ImageButton>(R.id.shareApp)
        shareApp.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out Cocktails app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
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

    ///////////////// PERMISSIONS /////////////////

    private fun checkPermission(permission: String): Boolean {
        return context?.let { ActivityCompat.checkSelfPermission(it, permission) } == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions(permission: Array<String>, permissionId: Int, dummyParam: Boolean = true) {
        // add dummyParam since it's not allowed to override requestPermissions inside a fragment
        requestPermissions(permission, permissionId)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_EXTERNAL_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                val shareCocktail = rootView.findViewById<ImageButton>(R.id.share)
                shareCocktail.performClick()
            }
            else {
                Toast.makeText(context, "Can't share a cocktail recipe without storage permission", Toast.LENGTH_LONG).show()
            }
        }
    }

    ///////////////// SCREENSHOT /////////////////

    private fun getScreenShot(): Bitmap {
        val scrollView = rootView.findViewById<ScrollView>(R.id.recipe_item)
        val returnedBitmap = Bitmap.createBitmap(scrollView.getChildAt(0).width*2, scrollView.getChildAt(0).height*2, Bitmap.Config.RGB_565)
        val canvas = Canvas(returnedBitmap)
        canvas.scale(2.0f, 2.0f)
        context?.let { ContextCompat.getColor(it, R.color.lightColorBg) }?.let { canvas.drawColor(it) }
        scrollView.getChildAt(0).draw(canvas);
        return returnedBitmap
    }

    private fun getUploadUriToBitmap(rotation:Float,uploadImgUri: Uri):Bitmap{
        val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uploadImgUri)
        val matrix= Matrix()
        matrix.setRotate(rotation)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

//        findViewById<ImageView>(R.id.new_upload_user_img_TV).setImageBitmap(bitmapNew)
    }
}