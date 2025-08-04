package top.ntutn.sevenzip.util

import java.io.Closeable

class Closeables: Closeable {
    private val closeables = mutableListOf<Closeable>()
    private var closed = false

    fun addCloseable(closeable: Closeable) = synchronized(closeables) {
        require(!closed) { "Collection closed!" }
        closeables.add(closeable)
    }

    override fun close() = synchronized(closeables) {
        closed = true
        closeables.forEach(Closeable::close)
    }
}