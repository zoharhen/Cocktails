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
import java.util.*


class ARFragment(private val cocktail: Cocktail) : Fragment() {

    private var TAG: String = "COMPATIBILITY"
    private var MIN_OPENGL_VERSION: Double = 3.0

    private var arFragment: ArFragment? = null
    private var glassPlaced: Boolean = false

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cocktail.glass?.let {
            Glass.loadProperties(it)
        }
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

        // Hide the dots indicating a viable surface
        // arFragment!!.planeDiscoveryController.hide()

        // Build and add the rendered 3D model to the scene.
        // the scene is where the 3D objects are rendered.
        // HitResult is a ray-cast on the object.
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
            .exceptionally { throwable: Throwable ->
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
        MaterialFactory.makeOpaqueWithColor(requireActivity(), Color(Color.RED))
            .thenAccept { material ->
                val lineNode = Node().apply {
                    setParent(glassNode)
                    renderable = ShapeFactory.makeCylinder(
                        0.0005f,
                        Glass.bottomSize,
                        Vector3(),
                        material)

                    localPosition = Glass.bottomPos

                    // Rotate the line by 90 degrees around the Z axis
                    localRotation = Quaternion.axisAngle(Vector3(0f, 0f, 1f), 90f)
                }
            }
    }

    /**
     * Constructs a line (as a cylinder of radius width 0.01f and very thin height and depth)
     * at position 0.0f, 0.15f, 0.0f and with TEXTURE
     * @param hitResult - If the hit result is a plane
     * @param color - a color
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun makeLine(hitResult: HitResult, color: Int) {
        MaterialFactory.makeOpaqueWithColor(requireActivity(), Color(color))
            .thenAccept { material ->
                addNodeToScene(arFragment!!, hitResult.createAnchor(),
                    ShapeFactory.makeCylinder(
                        0.01f,
                        0.3f,
                        Vector3(0.0f, 0.15f, 0.0f),
                        material)
                )

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

    override fun onResume() {
        super.onResume()
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

}

/**
 * Just a blank selection class for an object, to remove the selection ring.
 */
class BlankSelectionVisualizer: SelectionVisualizer {
    override fun applySelectionVisual(var1: BaseTransformableNode) { }
    override fun removeSelectionVisual(var1: BaseTransformableNode) { }
}