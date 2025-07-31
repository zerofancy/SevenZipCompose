package top.ntutn.sevenzip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.PropID
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import top.ntutn.sevenzip.util.ReferenceCounted
import top.ntutn.sevenzip.util.rememberClose
import top.ntutn.sevenzip.util.toReferenceCounted
import java.io.File
import java.io.RandomAccessFile
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class SevenZipViewModel : ViewModel() {
    private val archiveRef = AtomicReference<ReferenceCounted<IInArchive>?>(null)

    private var archiveTree = ArchiveNode()
    private val _browsingNode = MutableStateFlow<ArchiveNode?>(null)
    val browsingNode: StateFlow<ArchiveNode?> get() = _browsingNode

    suspend fun openArchive(file: File): Boolean = viewModelScope.async(Dispatchers.Default) {
        val archive = RandomAccessFile(file, "r").use { file ->
            try {
                SevenZip.openInArchive(null, RandomAccessFileInStream(file))
            } catch (e: SevenZipException) {
                e.printStackTrace()
                return@async false
            }
        }
        val closeableArchive = archive.toReferenceCounted {
            it.close()
        }.also {
            val old = this@SevenZipViewModel.archiveRef.exchange(it.clone())
            old?.close()
        }
        closeableArchive.rememberClose { archive ->
            val itemCount = archive.numberOfItems
            val archiveTree: ArchiveNode = ArchiveNode().also {
                it.name = "ROOT"
            }
            // 构造归档文件树
            for (i in 0 until itemCount) {
                val path = archive.getProperty(i, PropID.PATH) as? String ?: ""
                val isDir = archive.getProperty(i, PropID.IS_FOLDER) as? Boolean ?: false
                val names = path.split(File.separator)
                var parentPtr = archiveTree
                var currentPtr: ArchiveNode? = null
                for (j in names.indices) {
                    val isCurrentNodeDir = j < names.size - 1 || isDir // 只要不是最后一级，那肯定是文件夹
                    currentPtr =
                        parentPtr.children.find { it.isDir == isCurrentNodeDir && it.name == names[j] }
                    currentPtr = currentPtr ?: ArchiveNode().also {
                        it.name = names[j]
                        it.isDir = isCurrentNodeDir
                        it.parent = parentPtr
                        parentPtr.children.add(it)
                    }
                    parentPtr = currentPtr
                }
                currentPtr?.index = i
            }
            print(archiveTree.printTree())
            this@SevenZipViewModel.archiveTree = archiveTree
            this@SevenZipViewModel._browsingNode.value = archiveTree
        }
        return@async true
    }.await()

    fun enterFolder(node: ArchiveNode) {
        _browsingNode.value = node
    }

    suspend fun extract2Temp(node: ArchiveNode): File? = withContext(Dispatchers.IO) {
        assert(!node.isDir) { "Directory is not supported now" }
        assert(node.index != ArchiveNode.EMPTY_INDEX)

        val archiveCounted = archiveRef.load()?.clone() ?: return@withContext null
        val outFile = File.createTempFile("7zc", "." + node.extension).also {
            it.deleteOnExit()
        }
        val randomAccessFile = RandomAccessFile(outFile, "rw")
        return@withContext archiveCounted.rememberClose { archive ->
            try {
                archive.extractSlow(node.index, RandomAccessFileOutStream(randomAccessFile))
                outFile
            } catch (e: SevenZipException) {
                e.printStackTraceExtended()
                null
            }
        }
    }

    fun moveBack() {
        val parentNode = _browsingNode.value?.parent
        if (parentNode != null) {
            _browsingNode.value = parentNode
        }
    }

    override fun onCleared() {
        super.onCleared()
        archiveTree = ArchiveNode()
        _browsingNode.value = null
        archiveRef.exchange(null)?.close()
    }
}