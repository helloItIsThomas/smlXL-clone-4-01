import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap
import org.openrndr.draw.FontMap
import org.openrndr.draw.loadFont
import java.io.File

fun typeScale(size: Int, base: Double, scaleIndex: Int): DoubleArray {
    val scales = listOf(1.067, 1.125, 1.200, 1.250, 1.333, 1.414, 1.500, 1.618)
    val result: DoubleArray = DoubleArray(size)
    result[0] = base

    val scale = scales.getOrNull(scaleIndex) ?: return DoubleArray(0)

    for (i in 1 until size) {
        result[i] = result[i - 1] * scale
    }
    return result
}

fun defaultTypeSetup(refScale: DoubleArray, fontDistro: List<String>): Pair<List<FontMap>, List<FontImageMap>> {
    val division = (refScale.size / fontDistro.size).toInt()

    val styleToFile = mapOf(
        "black" to "Outfit-Black.otf",
        "bold" to "Outfit-Bold.otf",
        "extrabold" to "Outfit-ExtraBold.otf",
        "extralight" to "Outfit-ExtraLight.otf",
        "light" to "Outfit-Light.otf",
        "med" to "Outfit-Medium.otf",
        "reg" to "Outfit-Regular.otf",
        "semibold" to "Outfit-SemiBold.otf",
        "thin" to "Outfit-Thin.otf",
    )

    val mapList = mutableListOf<FontMap>()
    val imageMapList = mutableListOf<FontImageMap>()

    for (i in refScale.indices) {
        val styleIndex = i / division
        val safeIndex = if (styleIndex >= fontDistro.size) fontDistro.size - 1 else styleIndex
        val styleKey = fontDistro[safeIndex]
        val fileName = styleToFile[styleKey] ?: continue
        val file = File("data/fonts/outfit", fileName)

        if (file.exists()) {
            val fontMap = loadFont(file.absolutePath, refScale[i])
            mapList.add(fontMap)
            val myFontImageMap = FontImageMap.fromUrl(file.absolutePath, refScale[i])
            imageMapList.add(myFontImageMap)
        }
    }
    return Pair(mapList, imageMapList)
}

class onceObj {
    var current = 1
    var new = 0
    fun check(_checked: Int): Boolean {
        this.new = _checked
        if (this.new == this.current) {
            return false
        } else {
            this.current = this.new
            println("true")
            return true
        }
    }
}
