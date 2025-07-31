package top.ntutn.sevenzip

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