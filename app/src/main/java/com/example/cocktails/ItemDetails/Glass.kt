package com.example.cocktails.ItemDetails

import com.google.ar.sceneform.math.Vector3

/**
 * Static class for glass properties.
 */
class Glass {

    companion object {
        var capacity: Float = 0f  // ml
        var bottomPos: Vector3 = Vector3(0f, 0f, 0f)
        var topPos: Vector3 = Vector3(0f, 0f, 0f)
        var bottomSize: Float = 0f
        var topSize: Float = 0f
        var maxWidth: Float = 0f

        fun loadProperties(glass: String) {
            if (glass == "cocktail glass.sfb") {
                capacity = 220f
                bottomPos = Vector3(0f, 0.0235f, 0f)
                topPos = Vector3(0f, 0.149f, 0f)
                bottomSize = 0.05f
                topSize = 0.06f
                maxWidth = 0.075f

            } else if (glass == "martini.sfb") {
                capacity = 110f
                bottomPos = Vector3(0f, 0.086f, 0f)
                topPos = Vector3(0f, 0.132f, 0f)
                bottomSize = 0.008f
                topSize = 0.06f
                maxWidth = 0.08f

            } else if (glass == "Water Glass.sfb") {
                capacity = 170f
                bottomPos = Vector3(0f, 0.0065f, 0f)
                topPos = Vector3(0f, 0.09f, 0f)
                bottomSize = 0.03f
                topSize = 0.09f
                maxWidth = 0.09f

            } else if (glass == "whiskey1.sfb") {
                capacity = 200f
                bottomPos = Vector3(0f, 0.015f, 0f)
                topPos = Vector3(0f, 0.085f, 0f)
                bottomSize = 0.07f
                topSize = 0.09f
                maxWidth = 0.09f

            } else if (glass == "whiskey2.sfb") {
                capacity = 210f
                bottomPos = Vector3(0f, 0.02f, 0f)
                topPos = Vector3(0f, 0.09f, 0f)
                bottomSize = 0.07f
                topSize = 0.1f
                maxWidth = 0.1f

            } else if (glass == "whiskey3.sfb") {
                capacity = 200f
                bottomPos = Vector3(0f, 0.015f, 0f)
                topPos = Vector3(0f, 0.095f, 0f)
                bottomSize = 0.08f
                topSize = 0.085f
                maxWidth = 0.085f

            } else if (glass == "Wine_Glass.sfb") {
                capacity = 170f
                bottomPos = Vector3(0f, 0.08f, 0f)
                topPos = Vector3(0f, 0.14f, 0f)
                bottomSize = 0.01f
                topSize = 0.05f
                maxWidth = 0.05f

            } else if (glass == "wine2.sfb") {
                capacity = 160f
                bottomPos = Vector3(0f, 0.08f, 0f)
                topPos = Vector3(0f, 0.16f, 0f)
                bottomSize = 0.02f
                topSize = 0.06f
                maxWidth = 0.07f

            }
        }
    }
}