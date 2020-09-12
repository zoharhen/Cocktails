package com.example.cocktails.ItemDetails

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context.ACTIVITY_SERVICE
import android.content.pm.PackageManager
import android.graphics.Color.parseColor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.fragment.app.Fragment
import com.example.cocktails.Cocktail
import com.example.cocktails.R
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseTransformableNode
import com.google.ar.sceneform.ux.SelectionVisualizer
import com.google.ar.sceneform.ux.TransformableNode
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream


class ARFragment(val parent: SectionsPagerAdapter) : Fragment() {

    private var TAG: String = "COMPATIBILITY"
    private var MIN_OPENGL_VERSION: Double = 3.0
    private val REQUEST_CODE_PERMISSION_CAMERA = 1111

    private lateinit var arFragment: ArFragment
    private var glassPlaced: Boolean = false
    private var arColor: String = ""

    private var ingredientsPairs: MutableList<Pair<Float, String>> = ArrayList()
    private var ingredients: MutableList<String> = ArrayList()
    private var leftSide: Boolean = true

    private lateinit var rootView: View
    lateinit var cocktail: Cocktail

    private var inflated: Boolean = false
    private var permissionRequested: Boolean = false
    var firstRun: Boolean = true
    var firstPause: Boolean = false

    companion object {
        fun newInstance(cocktail: Cocktail, parent: SectionsPagerAdapter): ARFragment? {
            val args = Bundle()
            args.putParcelable("cocktail", cocktail)
            val f = ARFragment(parent)
            f.arguments = args
            return f
        }
    }

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cocktail = arguments?.getParcelable("cocktail")!!

        // Load the current cocktail's glass properties
        cocktail.glass?.let {
            Glass.loadProperties(it)
        }

        // Extract lists of the ingredients and quantities
        getIngredients()

        try {
            val jsonObject = JSONObject(loadJSONFromAsset("clipartColors.json"))
            arColor = jsonObject.getString(cocktail.clipart)
        } catch (e: JSONException) {
            // thrown when:
            // - the json string is malformed (can't be parsed)
            // - there's no value for a requested key
            // - the value for the requested key can't be coerced to String
            // THIS SHOULDN'T HAPPEN
            renderError(requireActivity())
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Check compatibility to AR. if none, just return.
        if (!checkCompatibility(requireActivity())) {
            renderError(requireActivity())
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        rootView = inflater.inflate(R.layout.ar_layout_wrapper, container, false)
        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        this.view?.visibility = View.VISIBLE
        (parent.recipeFragmentInstance as RecipeFragment).view?.visibility = View.GONE

        if (!inflated  && !permissionRequested) {
            // Check for CAMERA permissions
            val hasCameraPermission = ActivityCompat
                .checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
            if (!hasCameraPermission) {
                // Request if no permission
                requestPermissions(arrayOf(
                    Manifest.permission.CAMERA),
                    REQUEST_CODE_PERMISSION_CAMERA)

            } else {
                this.initAr()
            }
        }

        permissionRequested = false
    }

    /**
     * Although ArFragment deals with permission, it does not give a rationale.
     * @param requestCode The request code
     * @param permissions The permissions requested
     * @param grantResults The results, indices correspond to 'permissions'
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.initAr()

        } else {
            // Request denied
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            ) {
                // Explain the necessity of the camera permission
//                val toast: Toast =
//                    Toast.makeText(requireActivity(), "This feature requires camera permission.", Toast.LENGTH_LONG)
//                toast.setGravity(Gravity.BOTTOM, 0, 0)
//                toast.show()

                initError()
            }

            permissionRequested = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initError() {
//        inflated = true

        val inflater = LayoutInflater.from(context)
        // This will also attempt to request permission, but we won't get here without it.
        val inflatedView: View = inflater.inflate(R.layout.ar_error_layout, (rootView as ViewGroup), false)
        (rootView as ViewGroup).addView(inflatedView)

        val permButton: Button = rootView.findViewById(R.id.perm_button)
        permButton.setOnClickListener {

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPause() {
        super.onPause()
        view?.visibility = View.GONE

        if (firstPause) {
            (parent.recipeFragmentInstance as RecipeFragment).initViews(false)
            firstPause = false
        }
    }

    /**
     * Initialize the AR scene
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initAr() {
        inflated = true

        val inflater = LayoutInflater.from(context)
        // This will also attempt to request permission, but we won't get here without it.
        val inflatedView: View = inflater.inflate(R.layout.ar_layout, (rootView as ViewGroup), false)
        (rootView as ViewGroup).addView(inflatedView)

        // Find the AR fragment
        arFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment

        arFragment.arSceneView.scene.addOnUpdateListener {
            //get the frame from the scene for shorthand
            if (firstRun) {
                val frame = arFragment.arSceneView.arFrame
                if (frame != null) {
                    //get the trackables to ensure planes are detected
                    val var3 = frame.getUpdatedTrackables(Plane::class.java).iterator()
                    while(var3.hasNext()) {
                        val plane = var3.next() as Plane

                        //If a plane has been detected & is being tracked by ARCore
                        if (plane.trackingState == TrackingState.TRACKING) {
//                            (parent.recipeFragmentInstance as RecipeFragment).reInflate(this)
                            firstPause = true
                            firstRun = false
                        }
                    }
                }
            }
        }

        // Make blank selection visuals (no ring)
        arFragment.transformationSystem
            .selectionVisualizer = BlankSelectionVisualizer()

        // Build and add the rendered 3D model to the scene.
        // The scene is where the 3D objects are rendered. HitResult is a ray-cast on the object.
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?,
                                               _: MotionEvent? ->
            // Don't allow placing on walls / ceiling
            if (plane?.type == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                // Don't allow more than 1 glass
                if (!glassPlaced) {
                    // Hide the dots indicating a viable surface
//                    arFragment.arSceneView.planeRenderer.isVisible = false

                    glassPlaced = true
                    placeObject(
                        arFragment,
                        hitResult.createAnchor(),
                        Uri.parse(cocktail.glass)
                    )
                }
            }
        }
    }

    /**
     * Asynchronously place a 'model' at 'anchor' in 'fragment'
     * Anchor is a fixed point in the 3D space.
     * AnchorNode positions itself in the world. This is the first node to be set.
     *
     * Add new nodes of the given renderable to the given fragment's scene, at the given anchor.
     * The render tree is as follows:
     *                  scene
     *                    |
     *                  anchor
     *                    |
     *                  glass
     *                /   |   \
     *             line ..... line
     *               | ....... |
     *        ingredient ... ingredient
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri) {
        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenAccept { renderableGlass: ModelRenderable ->
                // ------ Create the glass
                val glassNode = addGlassNode(fragment, anchor, renderableGlass)

                // ------ Create text and lines
                placeIngredients(glassNode)
            }
            .exceptionally {
                renderError(requireActivity())
                null
            }
    }

    /**
     * Place all the lines and text related to ingredients in the scene.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun placeIngredients(glassNode: TransformableNode) {
        // ------ Get gaps
        val capacity = calculateCapacity(glassNode)
        val gaps = calculateGaps(capacity)

        var nqPrevZPos = 0.05f  // For non quantified text positioning
        var prevGap = 0f  // For line positioning
        for ((index, gap) in gaps.withIndex()) {
            val newGap = prevGap + gap

            if (gap == 0f) {  // This is a non-quantified ingredient
                nqPrevZPos += 0.01f
                // ------ Add this ingredient to the non quantified area
                addNonQuantifiedTextNode(glassNode, nqPrevZPos, index)

            } else {  // This is a quantified ingredient
                // Just make sure the text don't overlap by switching sides when necessary
                leftSide = if (newGap - prevGap < 0.008f) {
                    !leftSide
                } else {
                    true
                }

                // Make sure we don't spill anything.... This shouldn't actually happen.
                val yPos = if (Glass.bottomPos.y + newGap > Glass.topPos.y) {
                    Glass.topPos.y
                } else {
                    Glass.bottomPos.y + newGap
                }

                // Adjust the line size to fit the position in the glass
                var lineSize = Glass.topSize * ((yPos - Glass.bottomPos.y) / capacity)
                if (lineSize < Glass.bottomSize) {
                    lineSize = Glass.bottomSize
                }

                // ------ Create a line node
                val lineNode = addLineNode(
                    glassNode,
                    lineSize, //Glass.bottomSize,
                    Vector3(
                        Glass.bottomPos.x,
                        Glass.bottomPos.y + newGap,
                        Glass.bottomPos.z
                    )
                )

                // ------ Create a text node
                addTextNode(lineNode, index)
            }

            prevGap = newGap
        }
    }

    /**
     * Add a text node to the non-quantified area (for small measurements), located on the Z axis.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun addNonQuantifiedTextNode(
        glassNode: TransformableNode,
        prevZPos: Float,
        index: Int
    ) {
        // Create a text node
        val textNode = Node().apply {
            setParent(glassNode)
            isEnabled = false

            localPosition = Vector3(
                0f,
                0f,
                prevZPos
            )

            // Rotate the text by -90 degrees around the X axis
            localRotation = Quaternion.axisAngle(
                Vector3(1f, 0f, 0f),
                -90f
            )

            // Scale the text down
            localScale = Vector3(
                localScale.x / 7,
                localScale.y / 7,
                localScale.z / 7
            )
        }

        // Set the text
        val arText = TextView(requireActivity())
        arText.apply {
            typeface = getFont(requireContext(), R.font.raleway_light)
            text = ingredients[index]
            setTextColor(parseColor(arColor))
//            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.arBackTextColor))
        }

        // Render the text to the node
        ViewRenderable.builder()
            .setView(requireContext(), arText)
            .build()
            .thenAccept { renderableText ->
                textNode.apply {
                    renderable = renderableText.apply {
                        // Applies to the renderable
                        renderPriority = Renderable.RENDER_PRIORITY_LAST
                        isShadowCaster = false
                        isShadowReceiver = false
                    }
                    isEnabled = true
                }
            }
            .exceptionally {
                renderError(requireActivity())
                null
            }
    }

    /**
     * Add a glass transformable node.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun addGlassNode(fragment: ArFragment,
                               anchor: Anchor,
                               renderableGlass: Renderable)
            : TransformableNode {
        val anchorNode = AnchorNode(anchor).apply {
            setParent(fragment.arSceneView.scene)
        }

        // Add the glass node
        val glassNode = TransformableNode(fragment.transformationSystem).apply {
            setParent(anchorNode)
            renderable = renderableGlass.apply {
                renderPriority = Renderable.RENDER_PRIORITY_LAST
                isShadowCaster = false
                isShadowReceiver = false
            }
        }

        return glassNode
    }

    /**
     * Add a line node under anchor.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun addLineNode(
        anchor: TransformableNode,
        lineSize: Float,
        linePos: Vector3)
            : Node {
        val lineNode = Node()
        MaterialFactory.makeOpaqueWithColor(
            requireActivity(),
            Color(parseColor(arColor))
        )
            .thenAccept { material ->
                // Set the node with a new line (cylinder) with the material.
                lineNode.apply {
                    setParent(anchor)

                    // Render the actual model
                    renderable = ShapeFactory.makeCylinder(
                        0.0005f,
                        lineSize,
                        Vector3(),
                        material
                    ).apply {
                        // Applies to the renderable
                        renderPriority = Renderable.RENDER_PRIORITY_LAST
                        isShadowCaster = false
                        isShadowReceiver = false
                    }

                    // Position according to the ingredient and the glass's capacity.
                    localPosition = linePos

                    // Rotate the line by 90 degrees around the Z axis
                    localRotation = Quaternion.axisAngle(
                        Vector3(0f, 0f, 1f),
                        90f
                    )
                }
            }
        return lineNode
    }

    /**
     * Add a text node next to the anchor given
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun addTextNode(anchor: Node, index: Int) {
        // Create a text node
        val ingredientNode = Node().apply {
            setParent(anchor)
            isEnabled = false

            if (leftSide) {
                // Set the position to the left of the line
                localPosition = Vector3(
                    - 0.004f,
                    Glass.maxWidth + 0.003f,
                    0f
                )
            } else {
                // Set the position to the right of the line
                localPosition = Vector3(
                    - 0.004f,
                    - Glass.maxWidth - 0.003f,
                    0f
                )
            }

            // Rotate the text by -90 degrees around the Z axis
            localRotation = Quaternion.axisAngle(
                Vector3(0f, 0f, 1f),
                -90f
            )

            // Scale the text down
            localScale = Vector3(
                localScale.x / 8,
                localScale.y / 8,
                localScale.z / 8
            )
        }

        // Set the text
        val arText = TextView(requireActivity())
        arText.apply {
            typeface = getFont(requireContext(), R.font.raleway_light)
            text = ingredients[index]
            setTextColor(parseColor(arColor))
        }

        // Render the text to the node
        ViewRenderable.builder()
            .setView(requireContext(), arText)
            .build()
            .thenAccept { renderableText ->
                ingredientNode.apply {
                    renderable = renderableText.apply {
                        // Applies to the renderable
                        renderPriority = Renderable.RENDER_PRIORITY_LAST
                        isShadowCaster = false
                        isShadowReceiver = false
                    }
                    isEnabled = true
                }
            }
            .exceptionally {
                renderError(requireActivity())
                null
            }
    }

    /**
     * Handle rendering errors
     */
    private fun renderError(activity: Activity) {
        val toast: Toast =
            Toast.makeText(activity, "Sorry, Something went wrong!", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()  // Unable to load renderer
    }

    /**
     * Check the device's compatibility with AR
     */
    fun checkCompatibility(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(activity,
                "This feature requires Android " + Build.VERSION_CODES.N + "or later",
                Toast.LENGTH_LONG)
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
            Toast.makeText(activity, "This feature requires Android 4.3 or higher",
                Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

    /**
     * Parse the ingredients for the cocktail
     */
    private fun getIngredients() {
        val curIngredients: Array<String> = cocktail.ingredients
        for (i in curIngredients) {
            // For example, i = "1 1/2 oz Vodka"
            val regex: Regex = Regex("[0-9/]*\\s([0-9/]*\\s)?\\w*\\s")
            val match = regex.find(i)

            var quantity = 0f
            var unit: String = ""
            // If no match was found, just take this ingredient as is, with 0 quantity.
            if (match != null) {
                val matchString: String = match.value

                // Now matchString should be "1 1/2 oz "
                val quantityRegex: Regex = Regex("[0-9/]+")
                // quantities should be ["1", "1/2"]
                var quantities: MatchResult? = quantityRegex.find(matchString)

                // If no quantity found, this is a Garnish or something else. Continue and save 0.
                if (quantities != null) {
                    quantity = parseFraction(quantities.value)

                    // Get next quantity if available
                    quantities = quantities.next()
                    if (quantities != null) {
                        quantity += parseFraction(quantities.value)
                    }

                    val unitRegex: Regex = Regex("[^0-9/\\s]\\w*")
                    unit = unitRegex.find(matchString)?.value.toString()
                    // unit should be "oz"
                }
            }

            // The following is used for AR display.
            // Add the pairs to a list.
            ingredientsPairs.add(Pair(quantity, unit))
            // Add the whole ingredient to a list.
            ingredients.add(i)
        }
    }

    /**
     * Take a String of the form "x/y" and turn it into a decimal point Float number.
     */
    private fun parseFraction(ratio: String): Float {
        return if (ratio.contains("/")) {
            val rat = ratio.split("/".toRegex()).toTypedArray()
            rat[0].toFloat() / rat[1].toFloat()
        } else {
            ratio.toFloat()
        }
    }

    /**
     * Calculate the given glass's capacity (in AR terms)
     */
    private fun calculateCapacity(glassNode: TransformableNode): Float {
        // Add an anchor for top position
        val top = Node().apply {
            setParent(glassNode)
            localPosition = Glass.topPos
        }
        // Add an anchor for bottom position
        val bottom = Node().apply {
            setParent(glassNode)
            localPosition = Glass.bottomPos
        }
        // Calculate the glass's capacity.
        val capacity = getDistanceBetweenVectorsInMeters(top.localPosition, bottom.localPosition)
        return capacity
    }

    /**
     * Calculate the gaps for each ingredient (in bottom-up order).
     * Uncomment a measurement unit to make it appear on the glass instead of the "under-box".
     */
    private fun calculateGaps(capacity: Float)
            : MutableList<Float> {

        val gaps: MutableList<Float> = ArrayList()
        for (value in ingredientsPairs) {
            val gap: Float = when (value.second) {
                "cup", "cups" -> value.first * capacity
                // 1 oz is roughly 30ml
                "oz", "oz.", "ounce", "ounces" -> (capacity / (Glass.capacity / 30)) * value.first//0.2f * value.first * capacity
                // A dash is merely a "sprinkle" of the ingredient, which have almost no effect on volume. About 0.62ml
//                "dash", "dashes" -> (capacity / (Glass.capacity / 0.62f)) * value.first//0.01f * value.first * capacity
                // 1 tsp is roughly 5ml
//                "tsp", "teaspoon", "tea spoon" -> (capacity / (Glass.capacity / 5)) * value.first //0.025f * value.first * capacity
                // 1 tbsp is roughly 15ml
                "tbsp" -> (capacity / (Glass.capacity / 15)) * value.first//0.07f * value.first * capacity
                // Leaves usually don't really take up space in terms of volume since the liquids can surround them. Could usually be compressed to a tbsp worth.
//                "leaves", "leave" -> (capacity / (Glass.capacity / 15)) * value.first//0.005f * value.first * capacity
                // Slices take a bit more, about 1/8 of a standard 200ml cup
//                "slice", "slices" -> (capacity / 8) * value.first// 0.01f * value.first * capacity
                // More or less the same as dash.
//                "pinch" -> (capacity / (Glass.capacity / 0.62f)) * value.first//0.005f * value.first * capacity
                // If all else fails, this is a garnish or something else
                else -> 0f
            }
            gaps.add(gap)
        }

        return gaps
    }

    // ********************************************************* //
    // *********************** UTILITIES *********************** //
    // ********************************************************* //

    /**
     * Get the meter-distance between two vectors
     */
    private fun getDistanceBetweenVectorsInMeters(to: Vector3, from: Vector3): Float {
        // Compute the difference vector between the two hit locations.
        val dx = to.x - from.x
        val dy = to.y - from.y
        val dz = to.z - from.z

        // Compute the straight-line distance (distanceMeters)
        return Math.sqrt(dx * dx + dy * dy + (dz * dz).toDouble()).toFloat()
    }

    /**
     * Load a json from asset .json file
     */
    private fun loadJSONFromAsset(fileName: String): String {
        return try {
            val `is`: InputStream = requireActivity().assets.open(fileName)
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            renderError(requireActivity())
            return ""
        }
    }

}

/**
 * Just a blank selection class for an object, to remove the selection ring.
 */
class BlankSelectionVisualizer: SelectionVisualizer {
    override fun applySelectionVisual(var1: BaseTransformableNode) { }
    override fun removeSelectionVisual(var1: BaseTransformableNode) { }
}