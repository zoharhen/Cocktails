package com.example.cocktails.ui.main

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.cocktails.R
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.*
import java.util.function.Consumer


class ARFragment(context: Context) : Fragment() {

    private var TAG: String = "COMPATIBILITY"
    private var MIN_OPENGL_VERSION: Double = 3.0

    var arFragment: ArFragment? = null
    var glassRenderable: ModelRenderable? = null

    @RequiresApi(Build.VERSION_CODES.N)
    @Override
//    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        // Find the AR fragment
        arFragment = childFragmentManager.findFragmentById(R.id.ux_fragment)
                as ArFragment

        // Build the 3D model. Load it from the sceneform binary file (sfb)
        buildModel(activity, "Water Glass.sfb")

        // Add the rendered model to the scene.
        // the scene is where the 3D objects are rendered.
        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, _: Plane?, _: MotionEvent? ->
            renderGlass(hitResult)
        }

        // Make blank selection visuals (no ring)
        arFragment!!.transformationSystem
            .selectionVisualizer = BlankSelectionVisualizer()

        return root
    }

    /**
     * Asynchronously build a glass model based on 'toBuild'.
     * @param toBuild An asset path for .sfb file
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun buildModel(activity: Activity, toBuild: String) {
        ModelRenderable.builder()
            .setSource(activity, Uri.parse(toBuild))
            .build()
            // Once it's built set our renderable
            .thenAccept { renderable ->
                run {
                    glassRenderable = renderable
                    glassRenderable?.apply {
                        renderPriority = Renderable.RENDER_PRIORITY_LAST
                        isShadowCaster = false
                        isShadowReceiver = false
                    }
                }
            }
            // Error handling
            .exceptionally {
                renderError(activity)
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
     * Actually render the glass object
     * HitResult is a ray-cast on the object.
     * Anchor is a fixed point in the 3D space.
     * AnchorNode positions itself in the world. This is the first node to be set.
     */
    private fun renderGlass(hitResult: HitResult) {
        if (glassRenderable == null) {
            return
        }

        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment!!.arSceneView.scene)
        val glass =
            TransformableNode(arFragment!!.transformationSystem)
        glass.setParent(anchorNode)
        glass.renderable = glassRenderable
//        glass.select()  // Not needed.
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