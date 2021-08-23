class DrawableGrid {
    fun clearSelection() {}

    var selectedIndex = 0

    var isEnabled = false
}

class My {
    private val drawableGrid = createDrawableGrid()

    private var useAll = false
        set(value) {
            field = value
            drawableGrid.isEnabled = !value
            if (value) drawableGrid.clearSelection() else drawableGrid.selectedIndex = 0
        }

    private fun createDrawableGrid(): DrawableGrid = DrawableGrid()
}
