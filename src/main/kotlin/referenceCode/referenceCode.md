


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
