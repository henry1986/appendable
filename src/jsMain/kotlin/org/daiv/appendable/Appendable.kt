package org.daiv.appendable

import mu.KotlinLogging
import org.w3c.dom.Node

interface Appendable {
    val node: Node
    fun letAppend(node: Node) {
        node.appendChild(this.node)
    }

    fun remove(node: Node) {
        if (this.node.parentNode == node) {
            node.removeChild(this.node)
        } else {
            logger.warn { "node ${this.node} is not a child of $node" }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger("org.daiv.ew.frontend.header.Appendable")
    }
}
