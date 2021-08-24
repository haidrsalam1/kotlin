// FULL_JDK

import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.TreeCellRenderer

class TreeCellRendererImpl : TreeCellRenderer {
    override fun getTreeCellRendererComponent(
        tree: JTree?,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {
        return PanelRenderer()
    }

    private class PanelRenderer : JPanel(BorderLayout()) {
        private fun getLeftOffset(tree: JTree): Int {
            val ui = tree.ui as BasicTreeUI
            return ui.leftChildIndent
        }
    }
}
