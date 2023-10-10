
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
        var rowCount = 4
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
//        charArr.forEach { e ->
//            randNums.add(random(0.0, 1.0))
//        }

        flatGrid.forEach { g->
            randNums.add(random(0.0, 1.0))
            animArr.add(Animation())
        }
        var uniqueY = flatGrid.map { it.y }.distinct().sorted()

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
        var songBPM = 133


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

        val tracker = ADSRTracker(this)
        tracker.attack = 0.00
        tracker.decay = 0.45
        tracker.sustain = 0.01
        tracker.release = 0.45
        var isTriggerOn = false
        val kickThresh = 0.85

        val targetLowBand = 0// 11
        val targetHighBand = fft.specSize() //23

        var scaleX = width / 50.0
        val scaleY = height * 0.05
        val bandHeightMap = mutableMapOf<Int, Double>() // Declare outside the loop but within the extend block
        val oldBandHeightMap = mutableMapOf<Int, Double>() // Declare outside the loop but within the extend block


        var clock = 0.0





//        extend(ScreenRecorder()) {
//            frameRate = 30
//        }
fun exponentialEaseOut(left: Double, right: Double, t: Double): Double {
    return if (t == 1.0) {
        right
    } else {
        (right - left) * (-Math.pow(2.0, -10 * t) + 1) + left
    }
}





        extend {

            animArr.forEachIndexed { i, a ->
//                a((randNums[i] * 0.3 + frameCount * globalSpeed) % loopDelay)
//                a(((i * 1) * 0.15 + frameCount * globalSpeed) % loopDelay)
//                a(((i * 1) * (tracker.value()) + frameCount * globalSpeed) % loopDelay)
                a(((randNums[i] * 0.3) * (tracker.value())) % loopDelay)
//                a((tracker.value()) % loopDelay)
            }
            drawer.clear(ColorRGBa.BLACK)
            fft.forward(lineIn.mix)

            drawer.fill = null


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
            drawer.clear(ColorRGBa.fromVector(Vector4(
                tracker.value().coerceIn(0.0, 1.0),
                tracker.value().coerceIn(0.0, 1.0),
                tracker.value().coerceIn(0.0, 1.0),
                1.0
            )))


            val stepSize = (targetHighBand - targetLowBand) / columnCount

            scaleX = (width / stepSize.toDouble()-gutterX*1.5)

            drawer.strokeWeight = 1.0

            // when this condition fires,
            // I want to update the bandHeight,
            // and store the previous bandHeight.

            clock = frameCount * 0.05


            flatGrid.forEachIndexed { i, r ->
                if((clock) % 1.0 == 0.0){
//                if(isTriggerOn){
                    val newBandHeight = fft.getBand((i % columnCount) * stepSize).toDouble()
                    oldBandHeightMap[i] = bandHeightMap.getOrDefault(i, 0.0)
                    bandHeightMap[i] = newBandHeight
                    if(i == 0){
//                        println("   ")
//                        println(oldBandHeightMap[i])
//                        println(bandHeightMap[i])
                    }
                }
//                var bandHeight2 = bandHeightMap.getOrDefault(i, 0.0)

                var bandHeight2 = exponentialEaseOut(
                    oldBandHeightMap.getOrDefault(i, 0.0),
                    bandHeightMap.getOrDefault(i, 0.0),
                    (clock) % 1.0
                )
//                bandHeight2 = bandHeightMap.getOrDefault(i, 0.0)

                drawer.pushTransforms()
                val scaleX = r.width / bounds.width

                val rowIndex = uniqueY.indexOf(r.y)
                val isTopPinned = rowIndex % 2 == 0
                val invertPeriod = rowIndex == 2 || rowIndex == 3
                val freqMultiplier = if (invertPeriod) -1 else 1
                val adjustIndex = if (rowIndex == 3) 0 else if (rowIndex == 2) 1 else rowIndex

                val commonSin = bandHeight2.coerceIn(0.0, 1.0) * freqMultiplier

                val scaleY: Double

                val translateY = if (isTopPinned) {
                    scaleY = (r.height / bounds.height) * commonSin.map(
                        -1.0,
                        1.0,
                        0.0,
                        1.5
                    )
                    r.y
                } else {
                    scaleY = (r.height / bounds.height) * commonSin.map(
                        -1.0,
                        1.0,
                        1.5,
                        0.0
                    )
                    r.y + r.height - (bounds.height * scaleY)
                }

                val commonColor = commonSin.map(-1.0, 1.0, 0.0, 1.0)
                drawer.translate(r.x, translateY)
                drawer.fill = ColorRGBa(commonColor, 0.0, 0.75)//, tracker.value().map(1.0,0.0, 0.0, 1.0))
                drawer.stroke = drawer.fill
                drawer.shape(firstShape.shape.transform(createScaleMatrix(scaleX, scaleY)))
                drawer.popTransforms()
            }



            // THIS NEEDS TO STAY AT THE END //
            if (mouseClick) mouseClick = false
            // END END ////////////////////////
        }
    }
}