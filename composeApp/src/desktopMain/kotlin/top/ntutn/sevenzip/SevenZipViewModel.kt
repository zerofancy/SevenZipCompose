package top.ntutn.sevenzip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.IProgress
import net.sf.sevenzipjbinding.PropID
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import top.ntutn.sevenzip.util.ReferenceCounted
import top.ntutn.sevenzip.util.rememberClose
import top.ntutn.sevenzip.util.rememberCloseSuspend
import top.ntutn.sevenzip.util.toReferenceCounted
import top.ntutn.sevenzip.zip.ArchiveNode
import top.ntutn.sevenzip.zip.ArchiveNodeExtractCallback
import top.ntutn.sevenzip.zip.CreateZipCallback
import java.io.File
import java.io.RandomAccessFile
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class SevenZipViewModel : ViewModel() {
    private val archiveRef = AtomicReference<ReferenceCounted<IInArchive>?>(null)

    private var archiveTree = ArchiveNode()

    // 当前正在浏览的结点。这里不需要释放资源，但需要一个包装类来让Compose识别到引用变化
    private val _browsingNode = MutableStateFlow<ReferenceCounted<ArchiveNode>?>(null)
    val browsingNode: StateFlow<ReferenceCounted<ArchiveNode>?> get() = _browsingNode

    suspend fun openArchive(file: File): Boolean = viewModelScope.async(Dispatchers.Default) {
        val randomAccessFile = RandomAccessFile(file, "r") // fixme filenotfound e.g. in trash
        val archive = try {
            SevenZip.openInArchive(null, RandomAccessFileInStream(randomAccessFile))
        } catch (e: SevenZipException) {
            e.printStackTrace()
            randomAccessFile.close() // archive没有成功打开，手动把文件关闭
            return@async false
        }
        val closeableArchive = archive.toReferenceCounted {
            println("close invoke $it")
            randomAccessFile.close()
            it.close()
        }.also {
            val old = this@SevenZipViewModel.archiveRef.exchange(it.clone())
            println("close invoke ${old?.get()}")
            old?.close()
        }
        closeableArchive.rememberClose { archive ->
            val itemCount = archive.numberOfItems
            val archiveTree: ArchiveNode = ArchiveNode().also {
                it.name = "ROOT"
                it.isDir = true
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
            this@SevenZipViewModel._browsingNode.value = archiveTree.toReferenceCounted {  }
        }
        return@async true
    }.await()

    fun createArchive(baseFile: File, files: List<File>, targetFile: File) = viewModelScope.launch {
        val files = withContext(Dispatchers.IO) {
            files.flatMap { file -> file.walkTopDown() }
                .filter { it.isFile }
                .filter { it.canRead() }
        }
        try {
            val raf = RandomAccessFile(targetFile, "rw")
            val rafs = RandomAccessFileOutStream(raf)
            val outArchiveRef = SevenZip.openOutArchiveZip().toReferenceCounted {
                it.close()
                rafs.close()
            }
            outArchiveRef.rememberCloseSuspend { outArchive ->
                withContext(Dispatchers.IO) {
                    outArchive.createArchive(rafs, files.size, CreateZipCallback(baseFile, files))
                }
            }
            openArchive(targetFile)
        } catch (e: SevenZipException) {
            e.printStackTraceExtended()
        }
    }

    fun enterFolder(node: ArchiveNode) {
        _browsingNode.value = node.toReferenceCounted {  }
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

    suspend fun extractAll(targetDir: File) = withContext(Dispatchers.IO) {
        assert(targetDir.isDirectory) { "target path should be a directory" }
        val archiveCounted = archiveRef.load()?.clone() ?: return@withContext

        val callback = ArchiveNodeExtractCallback(archiveTree, targetDir, object : IProgress {
            override fun setTotal(total: Long) {
            }

            override fun setCompleted(complete: Long) {
            }
        })
        archiveCounted.rememberClose { archive ->
            archive.extract((0 until archive.numberOfItems).toList().toIntArray(), false, callback)
        }
        callback.close()
    }

    fun moveBack() {
        val parentNode = _browsingNode.value?.get()?.parent?.toReferenceCounted {  }
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