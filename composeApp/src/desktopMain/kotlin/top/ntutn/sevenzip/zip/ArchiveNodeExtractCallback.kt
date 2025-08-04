package top.ntutn.sevenzip.zip

import net.sf.sevenzipjbinding.ExtractAskMode
import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.IArchiveExtractCallback
import net.sf.sevenzipjbinding.IProgress
import net.sf.sevenzipjbinding.ISequentialOutStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import top.ntutn.sevenzip.util.Closeables
import java.io.Closeable
import java.io.File
import java.io.RandomAccessFile
import java.util.ArrayDeque

class ArchiveNodeExtractCallback(rootNode: ArchiveNode, private val targetPath: File, processCallback: IProgress): IArchiveExtractCallback, IProgress by processCallback, Closeable {
    private val raFiles = Closeables()

    private val nodeMap = buildMap<Int, ArchiveNode> {
        val queue = ArrayDeque<ArchiveNode>()
        queue.add(rootNode)
        while (queue.isNotEmpty()) {
            val node = queue.poll()
            if (node.isDir) {
                node.children.forEach {
                    queue.add(it)
                }
                if (node.index != ArchiveNode.EMPTY_INDEX) {
                    put(node.index, node)
                }
            } else {
                put(node.index, node)
            }
        }
    }

    override fun getStream(
        index: Int,
        extractAskMode: ExtractAskMode?
    ): ISequentialOutStream? {
        val node = nodeMap[index] ?: return null
        val filePath = File(targetPath, node.relativePath())
        if (node.isDir) {
            filePath.mkdirs()
            return null
        }
        if (filePath.exists()) {
            filePath.deleteRecursively()
        }
        filePath.parentFile.mkdirs()
        filePath.createNewFile()
        val raFile = RandomAccessFile(filePath, "rw").also {
            raFiles.addCloseable(it)
        }
        return RandomAccessFileOutStream(raFile)
    }

    override fun prepareOperation(extractAskMode: ExtractAskMode?) {
    }

    override fun setOperationResult(extractOperationResult: ExtractOperationResult?) {
    }

    override fun close() {
        raFiles.close()
    }
}