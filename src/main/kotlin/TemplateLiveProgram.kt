
import classes.CButton
import classes.CSlider
import ddf.minim.Minim
import ddf.minim.analysis.FFT
import demos.classes.Animation
import kotlinx.coroutines.DelicateCoroutinesApi
import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.draw.font.loadFace
import org.openrndr.extra.envelopes.ADSRTracker
import org.openrndr.extra.fx.blur.FrameBlur
import org.openrndr.extra.fx.blur.GaussianBloom
import org.openrndr.extra.fx.blur.HashBlur
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
import java.io.FileInputStream
import java.io.InputStream
import kotlin.math.*


fun main() = application {
    configure {
        width = 608
        height = 342
//        windowResizable = true
        hideWindowDecorations = true
        windowAlwaysOnTop = true
//        position = IntVector2(1184,110)
        position = IntVector2(1300,110)
        windowTransparent = true
//        windowResizable = true
        multisample = WindowMultisample.SampleCount(4)
        hideCursor = true
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
        var columnCount = 30//16
        var rowCount = 2
        var marginX = 10.0
        var marginY = 10.0
        var gutterX = 10.0
        var gutterY = height * 0.025
        var grid = drawer.bounds.grid(columnCount, rowCount, marginX, marginY, gutterX, gutterY)
        var flatGrid = grid.flatten()

        val incremCheck = onceObj()
        var palette = listOf(ColorRGBa.fromHex(0xF1934B), ColorRGBa.fromHex(0x0E8847), ColorRGBa.fromHex(0xD73E1C), ColorRGBa.fromHex(0xF4ECDF), ColorRGBa.fromHex(0x552F20))
        val white = ColorRGBa.WHITE
        val black = ColorRGBa.BLACK
        val animation = Animation()
        val loopDelay = 2.0
        val message = "hello"
        animation.loadFromJson(File("data/keyframes/keyframes-0.json"))
        val svgA: Composition = loadSVG(File("data/fonts/a.svg"))
        val firstShape: ShapeNode = svgA.root.findShapes()[0]
        val bounds = firstShape.shape.bounds
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
        val uniqueY = flatGrid.map { it.y }.distinct().sorted()

        animArr.forEach { a ->
            a.loadFromJson(File("data/keyframes/keyframes-0.json"))
        }

        fun createScaleMatrix(scaleX: Double, scaleY: Double): Matrix44 {
            return Matrix44(
                c0r0 = scaleX, c1r1 = scaleY,
                c2r2 = 1.0, c3r3 = 1.0
            )
        }

        val globalSpeed = 0.0085
        var baseFrequency = (2 * PI) / columnCount
        var frequency = baseFrequency / columnCount.toDouble()

        // AUDIO STUFF

        val minim = Minim(object : Object() {
            fun sketchPath(fileName: String): String {
                return fileName
            }
            fun createInput(fileName: String): InputStream {
                return FileInputStream(File(fileName))
            }
        })
        val lineIn = minim.lineIn

        val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate())
        var maxKickAverage = Double.MIN_VALUE
        var minKickAverage = Double.MAX_VALUE
        var normalizedKickAverage = 0.0

        var minSynthAverage = Double.MAX_VALUE
        var maxSynthAverage = Double.MIN_VALUE
        var normalizedSynthAverage = 0.0

        val tracker = ADSRTracker(this)
        tracker.attack = 0.00
        tracker.decay = 0.55
        tracker.sustain = 0.0
        tracker.release = 0.4
        var isTriggerOn = false
        val kickThresh = 0.75

        val targetLowBand = 0// 11
        val targetHighBand = fft.specSize() //23

        val scaleX = width / 50.0
        val scaleY = height * 0.05


//        extend(ScreenRecorder()) {
//            frameRate = 30
//        }
        extend {
            animArr.forEachIndexed { i, a ->
//                a((randNums[i] * 0.3 + frameCount * globalSpeed) % loopDelay)
                a(((i * baseFrequency) * 0.15 + frameCount * globalSpeed) % loopDelay)
            }
            drawer.clear(ColorRGBa.BLACK)
            fft.forward(lineIn.mix)

            drawer.fill = null

            baseFrequency = (2 * PI) / columnCount
            frequency = baseFrequency / columnCount.toDouble()
            // Existing code for kick detection
            var kickSum = 0.0
            var kickCount = 0
            for (i in 0 until (fft.specSize() * 0.01).toInt()) {
                val realBandHeight = fft.getBand(i) * 4
                kickSum += realBandHeight
                kickCount++
            }
            val kickAverage = if (kickCount > 0) kickSum / kickCount else 0.0

            // Normalize kickAverage
            minKickAverage = min(minKickAverage, kickAverage)
            maxKickAverage = max(maxKickAverage, kickAverage)
            if (maxKickAverage > minKickAverage) {
                normalizedKickAverage = (kickAverage - minKickAverage) / (maxKickAverage - minKickAverage)
            }

            if (normalizedKickAverage > kickThresh && !isTriggerOn) {
                tracker.triggerOn()
                isTriggerOn = true
            } else if (normalizedKickAverage <= kickThresh && isTriggerOn) {
                tracker.triggerOff()
                isTriggerOn = false
            }

// ... (your existing code for kick detection continues)


            flatGrid.forEachIndexed { i, r ->
                drawer.pushTransforms()
                val scaleX = r.width / bounds.width

                val rowIndex = uniqueY.indexOf(r.y)
                val isTopPinned = rowIndex % 2 == 0
                val invertPeriod = rowIndex == 2 || rowIndex == 3
                val freqMultiplier = if (invertPeriod) -1 else 1
                val adjustIndex = if (rowIndex == 3) 0 else if (rowIndex == 2) 1 else rowIndex

                val commonSin = sin(abs(i * baseFrequency * freqMultiplier) + frameCount * globalSpeed)

                val scaleY: Double
                val translateY = if (isTopPinned) {
                    scaleY = (r.height / bounds.height) * commonSin.map(-1.0, 1.0, 0.0, 2.0)
                    r.y
                } else {
                    scaleY = (r.height / bounds.height) * commonSin.map(-1.0, 1.0, 2.0, 0.0)
                    r.y + r.height - (bounds.height * scaleY)
                }

                val commonColor = commonSin.map(-1.0, 1.0, 0.0, 1.0)
                drawer.translate(r.x, translateY)
                drawer.fill = ColorRGBa(commonColor, 0.0, 0.75)
                drawer.stroke = drawer.fill
                drawer.shape(firstShape.shape.transform(createScaleMatrix(scaleX, scaleY)))
                drawer.popTransforms()
            }

            for (i in targetLowBand..targetHighBand) {
                val bandHeight = fft.getBand(i)
                // Draw lines according to bandHeight
                drawer.lineSegment(
                    x0 = i * scaleX, y0 = height.toDouble(),
                    x1 = i * scaleX, y1 = (height - bandHeight * scaleY)
                )
            }


            // THIS NEEDS TO STAY AT THE END //
            if (mouseClick) mouseClick = false
            // END END ////////////////////////
        }
    }
}