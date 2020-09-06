package com.example.cocktails.ui.main

import com.google.ar.sceneform.math.Vector3

/**
 * Static class for glass properties.
 */
class Glass {

    companion object {
        var bottomPos: Vector3 = Vector3(0f, 0f, 0f)
        var topPos: Vector3 = Vector3(0f, 0f, 0f)
        var bottomSize: Float = 0f
        var topSize: Float = 0f

        fun loadProperties(glass: String) {
            if (glass == "cocktail glass.sfb") {
                bottomPos = Vector3(0f, 0.0235f, 0f)
                topPos = Vector3(0f, 0.154f, 0f)
                bottomSize = 0.03f
                topSize = 0.05f

            } else if (glass == "martini.sfb") {
                bottomPos = Vector3(0f, 0.086f, 0f)
                topPos = Vector3(0f, 0.137f, 0f)
                bottomSize = 0.008f
                topSize = 0.06f

            } else if (glass == "Water Glass.sfb") {
                bottomPos = Vector3(0f, 0.0065f, 0f)
                topPos = Vector3(0f, 0.095f, 0f)
                bottomSize = 0.03f
                topSize = 0.09f

            } else if (glass == "whiskey1.sfb") {
                bottomPos = Vector3(0f, 0.015f, 0f)
                topPos = Vector3(0f, 0.1f, 0f)
                bottomSize = 0.08f
                topSize = 0.09f

            } else if (glass == "whiskey2.sfb") {
                bottomPos = Vector3(0f, 0.02f, 0f)
                topPos = Vector3(0f, 0.1f, 0f)
                bottomSize = 0.07f
                topSize = 0.1f

            } else if (glass == "whiskey3.sfb") {
                bottomPos = Vector3(0f, 0.015f, 0f)
                topPos = Vector3(0f, 0.1f, 0f)
                bottomSize = 0.08f
                topSize = 0.085f

            } else if (glass == "Wine_Glass.sfb") {
                bottomPos = Vector3(0f, 0.08f, 0f)
                topPos = Vector3(0f, 0.145f, 0f)
                bottomSize = 0.005f
                topSize = 0.04f

            }
        }
    }
}