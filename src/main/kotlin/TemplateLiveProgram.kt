
import classes.CButton
import classes.CSlider
import demos.classes.Animation
import kotlinx.coroutines.DelicateCoroutinesApi
import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.draw.font.loadFace
import org.openrndr.extra.noise.random
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.shapes.rectify.RectifiedContour
import org.openrndr.extra.shapes.rectify.rectified
import org.openrndr.math.IntVector2
import org.openrndr.math.Matrix44
import org.openrndr.math.transforms.scale
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.findShapes
import org.openrndr.svg.loadSVG
import java.io.File


@OptIn(DelicateCoroutinesApi::class)
fun main() = application {
    configure {
        width = 608
        height = 342
        hideWindowDecorations = true
        windowAlwaysOnTop = true
        position = IntVector2(1285,110)
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
        val incremCheck = onceObj()
        var palette = listOf(ColorRGBa.fromHex(0xF1934B), ColorRGBa.fromHex(0x0E8847), ColorRGBa.fromHex(0xD73E1C), ColorRGBa.fromHex(0xF4ECDF), ColorRGBa.fromHex(0x552F20))
        val white = ColorRGBa.WHITE
        val black = ColorRGBa.BLACK
        val animation = Animation()
        val loopDelay = 3.0
        val message = "hello"
        animation.loadFromJson(File("data/keyframes/keyframes-0.json"))
        val svgA = loadSVG(File("data/fonts/a.svg"))
        val firstShape = svgA.root.findShapes()[0]
        val firstContour = firstShape.shape.contours[0]

        val image = loadImage("data/images/cheeta.jpg")
        val scale: DoubleArray = typeScale(3, 100.0, 3)
        val typeFace: Pair<List<FontMap>, List<FontImageMap>> = defaultTypeSetup(scale, listOf("reg", "reg", "bold"))
        val animArr = mutableListOf<Animation>()
        val randNums = mutableListOf<Double>()
        val charArr = message.toCharArray()
        charArr.forEach { e ->
            animArr.add(Animation())
            randNums.add(random(0.0, 1.0))
        }
        animArr.forEach { a ->
            a.loadFromJson(File("data/keyframes/keyframes-0.json"))
        }
        val globalSpeed = 0.01



        extend {
            animArr.forEachIndexed { i, a ->
                a((randNums[i] * 0.3 + frameCount * globalSpeed) % loopDelay)
            }
            drawer.clear(ColorRGBa.TRANSPARENT)
            drawer.circle(drawer.bounds.center, 10.0)
            drawer.stroke = white

            // THIS NEEDS TO STAY AT THE END //
            if (mouseClick) mouseClick = false
            // END END ////////////////////////
        }
    }
}