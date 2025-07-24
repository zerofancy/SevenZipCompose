package top.ntutn.sevenzip

import java.io.Closeable
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.jvm.Throws

/**
 * 优化后的引用计数实现，解决线程可见性问题
 */
@OptIn(ExperimentalAtomicApi::class)
class ReferenceCounted<T : Closeable>(
    private val resource: T,
    private val onFinalRelease: ((T) -> Unit)? = null
) : Closeable {
    private val refCount = AtomicInteger(1)
    private val isClosed = AtomicBoolean(false)

    @Throws(IllegalStateException::class)
    fun get(): T {
        check(!isClosed.load()) { "资源已被释放，无法获取" }
        return resource
    }

    @Throws(IllegalStateException::class)
    fun clone(): ReferenceCounted<T> {
        // 先检查状态再增加计数，减少锁竞争
        if (isClosed.load()) {
            throw IllegalStateException("无法克隆已释放的资源")
        }
        // 使用循环确保计数增加成功（防止并发情况下的竞态条件）
        while (true) {
            val current = refCount.get()
            if (current <= 0) {
                throw IllegalStateException("无法克隆已释放的资源")
            }
            if (refCount.compareAndSet(current, current + 1)) {
                break
            }
        }
        return this
    }

    override fun close() {
        if (isClosed.load()) return

        val currentCount = refCount.decrementAndGet()
        if (currentCount == 0) {
            if (!isClosed.compareAndSet(expectedValue = false, newValue = true)) {
                resource.close()
                onFinalRelease?.invoke(resource)
            }
        } else if (currentCount < 0) {
            throw IllegalStateException("引用计数异常，可能重复释放资源")
        }
    }

    fun isClosed(): Boolean = isClosed.load()

    fun getRefCount(): Int = refCount.get()
}

fun <T : Closeable> T.toReferenceCounted(onFinalRelease: ((T) -> Unit)? = null): ReferenceCounted<T> {
    return ReferenceCounted(this, onFinalRelease)
}
