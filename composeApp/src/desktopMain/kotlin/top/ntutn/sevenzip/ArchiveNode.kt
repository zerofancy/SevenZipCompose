package top.ntutn.sevenzip

class ArchiveNode {
    var parent: ArchiveNode? = null

    val children = mutableListOf<ArchiveNode>()

    var isDir = false

    var index = -1

    var name = ""

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