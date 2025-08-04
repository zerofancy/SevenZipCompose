package top.ntutn.sevenzip.zip

import java.io.File

class ArchiveNode {
    companion object {
        const val EMPTY_INDEX = -1
    }

    var parent: ArchiveNode? = null

    val children = mutableListOf<ArchiveNode>()

    var isDir = false

    var index = EMPTY_INDEX

    var name = ""
        set(value) {
            field = value
            _extension = null
        }

    private var _extension: String? = null

    val extension: String
        get() {
            return _extension ?: let {
                name.split(".").lastOrNull() ?: ""
            }.also { _extension = it }
        }

    fun relativePath(): String {
        val p = parent ?: return name
        p.parent ?: return name // 直接属于根节点
        return p.relativePath() + File.separator + name
    }

    fun printTree(): String {
        val sb = StringBuilder()
        sb.append("#$index")
        sb.append("[$name]")
        sb.append("($isDir)")
        sb.append("{\n")
        children.forEach {
            val childrenTree = it.printTree()
            childrenTree.split("\n").forEach {
                sb.append("    ")
                sb.append(it)
                sb.append("\n")
            }
        }
        sb.append("}")
        return sb.toString()
    }
}