// FULL_JDK

import javax.swing.JList

class DrawableGrid : JList<String>()

class My {
    private val drawableGrid = createDrawableGrid()

    private var useAll = false
        set(value) {
            field = value
            drawableGrid.isEnabled = !value
            if (value) drawableGrid.clearSelection() else drawableGrid.selectedIndex = 0
        }

    private fun createDrawableGrid() = DrawableGrid().apply {
        isOpaque = false
        visibleRowCount = 3
        addListSelectionListener { _ ->
        }
    }
}
