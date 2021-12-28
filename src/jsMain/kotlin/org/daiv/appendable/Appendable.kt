package org.daiv.appendable

import kotlinx.browser.document
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.dom.create
import kotlinx.html.js.div
import mu.KotlinLogging
import org.w3c.dom.Node

interface Appendable {
    val node: Node
    fun letAppend(node: Node) {
        node.appendChild(this.node)
    }

    fun div(classes: String? = null, block: DIV.() -> Unit) = document.create.div(classes = classes, block = block)

    fun remove(node: Node) {
        if (this.node.parentNode == node) {
            node.removeChild(this.node)
        } else {
            logger.warn { "node ${this.node} is not a child of $node" }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger("org.daiv.appendable.Appendable")
    }
}

interface SiteHolder<T : Appendable> {
    fun setNewSubNode(appendable: T)
    fun deleteLastNodes()
    fun resetToLast()
}

interface SiteHolderExt<T : Appendable> : SiteHolder<T> {
    var currentSubNode: T?
    val node: Node

    val lastNodes: MutableList<T>

    override fun setNewSubNode(appendable: T) {
        if (appendable != currentSubNode) {
            currentSubNode?.let {
                it.remove(node)
                lastNodes.add(it)
            }
            appendable.letAppend(node)
            currentSubNode = appendable
        }
    }

    override fun resetToLast() {
        if (lastNodes.isEmpty()) {
            return
        }
        currentSubNode?.remove(node)
        val last = lastNodes.last()
        last.letAppend(node)
        currentSubNode = last
        lastNodes.removeAt(lastNodes.size - 1)
    }

    override fun deleteLastNodes() {
        lastNodes.clear()
    }
}

class SiteHolderImpl<T : Appendable>(override val node: Node) : SiteHolderExt<T>, Appendable {
    constructor(css: String) : this(document.create.div { classes = setOf(css) })

    override var currentSubNode: T? = null
    override val lastNodes: MutableList<T> = mutableListOf()
}

interface SiteReseter : SiteHolder<Appendable> {
    fun reset()
}
