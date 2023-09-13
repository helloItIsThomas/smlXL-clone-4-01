
import demos.classes.Animation
import kotlinx.coroutines.DelicateCoroutinesApi
import org.openrndr.WindowMultisample
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.camera.Orbital
import org.openrndr.extra.meshgenerators.boxMesh
import org.openrndr.extra.noise.random
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.mix
import org.openrndr.shape.Circle
import org.openrndr.writer
import java.awt.SystemColor.window
import java.io.File
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.measureTimeMillis


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
        var palette = listOf(ColorRGBa.fromHex(0xF1934B), ColorRGBa.fromHex(0x0E8847), ColorRGBa.fromHex(0xD73E1C), ColorRGBa.fromHex(0xF4ECDF), ColorRGBa.fromHex(0x552F20))
        val animation = Animation()
        val loopDelay = 3.0
        val message = "hello"
        animation.loadFromJson(File("data/keyframes/keyframes-0.json"))
        val image = loadImage("data/images/cheeta.jpg")
        val scale: DoubleArray = typeScale(3, 100.0, 3)
        val typeFace: Pair<List<FontMap>, List<FontImageMap>> = defaultTypeSetup(scale, listOf("reg", "reg", "bold"))
        var rad = 10.0
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

//        val rt = renderTarget(width, height) {
//            colorBuffer()
//        }
//        val cam = Orbital()

//        extend(cam)

        val globalSpeed = 0.01
        val circStroke = 20.0
        val strokeGap = 2.0
        val circRadMult = 20.0
        val circCount = 8


        // ( height / circCount )
        var circArr = mutableListOf<Circle>()
        for(n in 1..circCount){
            circArr.add(Circle(
                drawer.bounds.center.x,
                drawer.bounds.center.y,
                (n) * circRadMult
            ))
        }

        extend {
            animArr.forEachIndexed { i, a ->
                a((randNums[i] * 0.3 + frameCount * globalSpeed) % loopDelay)
            }
            drawer.clear(ColorRGBa.TRANSPARENT)
//            drawer.pushStyle()
//            drawer.stroke = ColorRGBa.fromHex(0x252525)
//            drawer.strokeWeight = 1.0
//            drawer.fill = null
//            drawer.rectangle(drawer.bounds)
//            drawer.popStyle()
//            drawer.fill = ColorRGBa.WHITE

//            drawer.shadeStyle = shadeStyle {
//                fragmentTransform = """
//                vec2 texCoord = va_texCoord0.xy;
//                texCoord = vec2(texCoord.y, 1.0 - texCoord.x);
//                vec2 size = textureSize(p_image, 0);
//                vec2 aspect = vec2(size.x / size.y, 1);
//                texCoord = 0.5 + (texCoord - 0.5) / aspect;
//                x_fill = texture(p_image, texCoord);
//                """
//                parameter("image", image)
//            }

//            drawer.stroke = ColorRGBa.fromHex(0xf6f6f6)
//            drawer.strokeWeight = 1.0
//            drawer.rotate(Vector3(
//                0.0,
//                1.0,
//                0.0
//            ), animArr[0].pathSlider * -100.0)

//            drawer.vertexBuffer(cube, DrawPrimitive.TRIANGLES)
//            drawer.translate(-drawer.bounds.center)
//            drawer.fill = ColorRGBa.GREEN
            drawer.stroke = ColorRGBa.BLUE
            drawer.fill = null
            drawer.strokeWeight = circStroke - strokeGap
            circArr.forEach { c->
                drawer.contour(c.contour)
            }
        }
    }
}