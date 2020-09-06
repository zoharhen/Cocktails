package com.example.cocktails.ui.main

import android.app.Activity
import android.app.ActivityManager
import android.content.Context.ACTIVITY_SERVICE
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.cocktails.Cocktail
import com.example.cocktails.R
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseTransformableNode
import com.google.ar.sceneform.ux.SelectionVisualizer
import com.google.ar.sceneform.ux.TransformableNode


class ARFragment(private val cocktail: Cocktail) : Fragment() {

    private var TAG: String = "COMPATIBILITY"
    private var MIN_OPENGL_VERSION: Double = 3.0

    private var arFragment: ArFragment? = null
    private var glassPlaced: Boolean = false

    private var ingredientsPairs: MutableList<Pair<Float, String>> = ArrayList()
    private var ingredients: MutableList<String> = ArrayList()

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the current cocktail's glass properties
        cocktail.glass?.let {
            Glass.loadProperties(it)
        }
        // Extract lists of the ingredients and quantities
        getIngredients()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val activity: Activity = requireActivity()

        // Check compatibility to AR. if none, just return.
        if (!checkCompatibility(activity)) {
            return super.onCreateView(inflater, container, savedInstanceState)
        }

        // Create the AR layout and return it
        val root = inflater.inflate(R.layout.ar_layout, container, false)

        initAr()

        return root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initAr() {
        // Find the AR fragment
        arFragment = childFragmentManager.findFragmentById(R.id.ux_fragment)
                as ArFragment

        // Make blank selection visuals (no ring)
        arFragment!!.transformationSystem
            .selectionVisualizer = BlankSelectionVisualizer()

        //--- Hide the dots indicating a viable surface - NOT RECOMMENDED
        // arFragment!!.planeDiscoveryController.hide()

        // Build and add the rendered 3D model to the scene.
        // The scene is where the 3D objects are rendered. HitResult is a ray-cast on the object.
        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?,
                                               _: MotionEvent? ->
            // Don't allow placing on walls / ceiling
            if (plane?.type == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                // Don't allow more than 1 glass
                if (!glassPlaced) {
                    glassPlaced = true
                    placeObject(
                        arFragment!!,
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
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri) {
        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenAccept { renderable: ModelRenderable ->
                addNodeToScene(fragment, anchor, renderable)
            }
            .exceptionally {
                renderError(requireActivity())
                null
            }
    }

    /**
     * Add a new node of the given renderable to the given fragment's scene, at the given anchor.
     * The render tree is as follows:
     *                  scene
     *                    |
     *                  anchor
     *                    |
     *                  glass
     *                /  / \  \
     *             line ..... line
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun addNodeToScene(fragment: ArFragment,
                               anchor: Anchor,
                               renderableGlass: Renderable) {
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

        // Add a line node
        val lineNode = Node()
        MaterialFactory.makeOpaqueWithColor(requireActivity(), Color(Color.RED))
            .thenAccept { material ->
                // Set the node with a new line (cylinder) with the material.
                lineNode.apply {
                    setParent(glassNode)

                    // Render the actual model
                    renderable = ShapeFactory.makeCylinder(
                        0.0005f,
                        Glass.bottomSize,
                        Vector3(),
                        material).apply {
                            // Applies to the renderable
                            renderPriority = Renderable.RENDER_PRIORITY_LAST
                            isShadowCaster = false
                            isShadowReceiver = false
                        }

                    // Position according to the ingredient and the glass's capacity.
                    localPosition = Glass.bottomPos

                    // Rotate the line by 90 degrees around the Z axis
                    localRotation = Quaternion.axisAngle(Vector3(0f, 0f, 1f), 90f)
                }
            }

        // Create a text node
        val ingredientNode = Node().apply {
            setParent(lineNode)
            isEnabled = false

            // Set the position to the right of the line
            // TODO: set the Y axis in regard to the glass's max width.
            localPosition = Vector3(-0.009f, 0.08f, 0f)

            // Rotate the text by -90 degrees around the Z axis
            localRotation = Quaternion.axisAngle(Vector3(0f, 0f, 1f), -90f)

            // Scale the text down
            localScale = Vector3(
                localScale.x / 5,
                localScale.y / 5,
                localScale.z / 5)
        }
        // Render it from layout with a textView
        ViewRenderable.builder()
            .setView(requireContext(), R.layout.ar_text_layout)
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
            val matchString: String = match!!.value.toString()

            // Now matchString should be "1 1/2 oz "
            val quantityRegex: Regex = Regex("[0-9/]+")
            // quantities should be ["1", "1/2"]
            // If no quantity found, this is a Garnish or something else. Continue.
            var quantities: MatchResult? = quantityRegex.find(matchString) ?: continue

            var quantity = parseFraction(quantities!!.value)

            // Get next quantity if available
            quantities = quantities.next()
            if (quantities != null) {
                quantity += parseFraction(quantities.value)
            }

            val unitRegex: Regex = Regex("[^0-9/\\s]\\w*")
            var unit: String = unitRegex.find(matchString)?.value.toString()
            // unit should be "oz"

            // The following is used for AR display.
            // Add the pairs to a list.
            ingredientsPairs.add(Pair(quantity, unit))
            // Add the whole ingredient to a list. Only add the ones with quantity.
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

}

/**
 * Just a blank selection class for an object, to remove the selection ring.
 */
class BlankSelectionVisualizer: SelectionVisualizer {
    override fun applySelectionVisual(var1: BaseTransformableNode) { }
    override fun removeSelectionVisual(var1: BaseTransformableNode) { }
}