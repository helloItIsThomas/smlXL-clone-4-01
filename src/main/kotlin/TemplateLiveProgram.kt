
import classes.CButton
import classes.CSlider
import demos.classes.Animation
import kotlinx.coroutines.DelicateCoroutinesApi
import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.draw.font.loadFace
import org.openrndr.extra.fx.edges.Contour
import org.openrndr.extra.noise.random
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.parameters.listParameters
import org.openrndr.extra.shapes.grid
import org.openrndr.extra.shapes.rectify.RectifiedContour
import org.openrndr.extra.shapes.rectify.rectified
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.math.*
import org.openrndr.math.transforms.scale
import org.openrndr.shape.*
import org.openrndr.svg.loadSVG
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


fun main() = application {
    configure {
        width = 608
        height = 342
        hideWindowDecorations = true
        windowAlwaysOnTop = true
        position = IntVector2(1185,110)
        windowTransparent = true
        multisample = WindowMultisample.SampleCount(4)
    }
    oliveProgram {
// MOUSE STUFF //////
        var mouseClick = false
        var mouseState = "up"
        mouse.dragged.listen { mouseState = "drag" }
        mouse.exited.listen { mouseState = "up" }
        mouse.buttonUp.listen { mouseState = "up"; mouseClick = true }
        mouse.buttonDown.listen { mouseState = "down" }
        mouse.moved.listen { mouseState = "move" }
// END //////////////
        var columnCount = 30
        var rowCount = 2
        var marginX = 10.0
        var marginY = 10.0
        var gutterX = 3.0
        var gutterY = 20.0
        var grid = drawer.bounds.grid(columnCount, rowCount, marginX, marginY, gutterX, gutterY)
        var flatGrid = grid.flatten()

        val incremCheck = onceObj()
        var palette = listOf(ColorRGBa.fromHex(0xF1934B), ColorRGBa.fromHex(0x0E8847), ColorRGBa.fromHex(0xD73E1C), ColorRGBa.fromHex(0xF4ECDF), ColorRGBa.fromHex(0x552F20))
        val white = ColorRGBa.WHITE
        val black = ColorRGBa.BLACK
        val animation = Animation()
        val loopDelay = 1.0
        val message = "hello"
        animation.loadFromJson(File("data/keyframes/keyframes-0.json"))
        val svgA: Composition = loadSVG(File("data/fonts/a.svg"))
        val firstShape: ShapeNode = svgA.root.findShapes()[0]
        val firstContour = firstShape.shape.contours[0]
        val image = loadImage("data/images/cheeta.jpg")
        val scale: DoubleArray = typeScale(3, 100.0, 3)
        val typeFace: Pair<List<FontMap>, List<FontImageMap>> = defaultTypeSetup(scale, listOf("reg", "reg", "bold"))
        val animArr = mutableListOf<Animation>()
        val randNums = mutableListOf<Double>()
        val charArr = message.toCharArray()
        charArr.forEach { e ->
            randNums.add(random(0.0, 1.0))
        }

        flatGrid.forEach { g->
            animArr.add(Animation())
        }

        animArr.forEach { a ->
            a.loadFromJson(File("data/keyframes/keyframes-0.json"))
        }

        fun createScaleMatrix(scaleX: Double, scaleY: Double): Matrix44 {
            return Matrix44(
                c0r0 = scaleX, c1r1 = scaleY,
                c2r2 = 1.0, c3r3 = 1.0
            )
        }

        val globalSpeed = 0.025
        val baseFrequency = (2 * PI) / columnCount
        val frequency = baseFrequency / columnCount.toDouble()

//        extend(ScreenRecorder()) {
//            frameRate = 30
//        }
        extend {
            animArr.forEachIndexed { i, a ->
//                a((randNums[i] * 0.3 + frameCount * globalSpeed) % loopDelay)
                a((i * 0.005 + frameCount * globalSpeed) % loopDelay)
            }
            drawer.clear(ColorRGBa.BLACK)
            drawer.fill = null
            drawer.stroke = ColorRGBa.PINK
            drawer.rectangle(drawer.bounds)


            drawer.fill = ColorRGBa.PINK
            flatGrid.forEachIndexed { i, r ->
                drawer.pushTransforms()
                val shapeBounds = firstShape.shape.bounds
                val scaleX = r.width / shapeBounds.width
                var scaleY = 0.0
                var translateY = r.y // Default Y-translation

                if (r.y < height * 0.33) {
                    // TOP //
                    scaleY = (r.height / shapeBounds.height) * sin(
                        (i*baseFrequency) + frameCount * globalSpeed
                    ).map(
                        -1.0,
                        1.0,
                        0.0,
                        2.0
                    )
                } else {
                    scaleY = (r.height / shapeBounds.height) * sin(
                        (i*baseFrequency) + frameCount * globalSpeed
                    ).map(
                        -1.0,
                        1.0,
                        2.0,
                        0.0
                    )
                    translateY = r.y + r.height - (shapeBounds.height * scaleY) // Adjust Y-translation for bottom row
                }

                val myMatrix = createScaleMatrix(scaleX, scaleY)
                drawer.translate(r.x, translateY) // Use adjusted Y-translation
                drawer.shape(firstShape.shape.transform(myMatrix))
                drawer.popTransforms()
            }






            // THIS NEEDS TO STAY AT THE END //
            if (mouseClick) mouseClick = false
            // END END ////////////////////////
        }
    }
}