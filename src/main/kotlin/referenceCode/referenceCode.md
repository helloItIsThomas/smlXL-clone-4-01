
# RENDER TARGET
    val rt = renderTarget(width, height) {
    colorBuffer()
    }

    drawer.isolatedWithTarget(rt) {
    drawer.clear(ColorRGBa.TRANSPARENT)
    drawer.fill = ColorRGBa.GREEN
    drawer.stroke = null
    drawer.fontMap = typeFace.second[0]
    drawer.text("H", 10.0, 70.0)
    }
# END

# FROM THIS CODE
            writer {
                typeFace.first.forEachIndexed { i, e ->
                    drawer.fontMap = e
                    charArr.forEachIndexed { ii, ee ->
                        drawer.pushTransforms()
                        drawer.translate(0.0, animArr[ii].pathSlider * 20.0 + 70.0)
                        text(ee.toString())
                        drawer.popTransforms()
                    }
                }
            }
# END

# CUSTOM GUI
    extend(ControlManager()) {
        styleSheet(has type "xy-pad") {
            background = Color.RGBa(ColorRGBa.GREEN)
            position = Position.FIXED
            width = 100.px
            top = 20.px
            left = 100.px
            color = Color.RGBa(ColorRGBa.GREEN)
        }
        layout {
            button {
                label = "Pick a color"
                clicked {}
            }
            xyPad {
                events.valueChanged.listen { event ->
                    val newValue: Vector2 = event.newValue
                    rad = newValue.x * 100.0
                }
            }
        }
    }
# END

# QUICK GUI
    val gui = GUI()
    extend(gui)

https://guide.openrndr.org/ORX/quickUIs.html
# END

# SCREEN RECORDER
    extend(ScreenRecorder()) {
        contentScale = 1.0
        frameRate = 60
        maximumDuration =  8.0
    }
# END

# TIMEOUT TRACKER ONCE
        val tracker = ADSRTracker(this)
        tracker.attack = 0.2
        tracker.decay = 0.2
        tracker.sustain = 0.8
        tracker.release = 0.1
        mouse.buttonDown.listen {
            println(it.position)
            tracker.triggerOn()
            GlobalScope.launch {
                delay(500)  // Replace with your desired time
                tracker.triggerOff()
            }
        }
# END

# TRACKER with LISTENER
        val tracker = ADSRTracker(this)
        tracker.attack = 0.2
        tracker.decay = 0.2
        tracker.sustain = 0.8
        tracker.release = 0.1
        mouse.buttonDown.listen {
            println(it.position)
            tracker.triggerOn()
        }
        mouse.buttonUp.listen {
            tracker.triggerOff()
        }
# END

# POINT A to B ( interactive animations )
        val anim = object : Animatable() {
            var x = 0.0
            var y = 0.0
        }

        mouse.buttonDown.listen {
            anim.apply {
                ::x.cancel()  // Cancel any ongoing animations
                ::y.cancel()

                val targetX = mouse.position.x
                val targetY = mouse.position.y
                
                ::x.animate(targetX, 500, Easing.CubicInOut)
                ::y.animate(targetY, 500, Easing.CubicInOut)
            }
        }


        extend {
            anim.updateAnimation()
        ...etc
# END

# TYPE STRETCH STUFF
        val faceSize = 64.0
        val face = loadFace("data/fonts/default.otf")
        val characters = "STARS"
        val faceShapes = characters.map { char ->
            face.glyphForCharacter(char).shape(faceSize)
        }
        var scaleMatrix = Matrix44.scale(1.0, 1.0, 1.0)
        var faceContours = faceShapes.map { n ->
            n.contours[0].contour.transform(scaleMatrix)
        }
# END

# CHECK PERFORMANCE / SPEED
    val elapsedTime = measureTimeMillis {
    }
    println(elapsedTime)
# END


# TYPE ON PATH / CIRCLE
        fun Drawer.textOnAPathReg(
            text: String,
            path: RectifiedContour,
            fimReg: CFontImageMapDrawer,
            offsetX: Double = 0.0,
            offsetY: Double = 0.0,
            tracking: Double = 0.0,
            scale: Double = 1.0
        ) {
            fimReg.drawTextOnPath(path, context, drawStyle, text, offsetX, offsetY, tracking, scale)
        }
        val fimReg = CFontImageMapDrawer()
        val upperPathCirc = Circle(drawer.bounds.center, 100.0)
    
    // THEN, IN EXTEND,

            drawer.fill = ColorRGBa.fromHex(0x6F131D)
            drawer.fontMap = typeFace.first[0]
            drawer.textOnAPathReg(
                "THE WAY",
                path = upperPathCirc.contour.sub(0.0, 0.75).rectified(),
                fimReg,
                0.0,
                tracking = 0.0
            )
# END