package top.ntutn.sevenzip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.PropID
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import java.io.File
import java.io.RandomAccessFile
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class SevenZipViewModel : ViewModel() {
    private val archiveRef = AtomicReference<ReferenceCounted<IInArchive>?>(null)
    private val _tip = MutableStateFlow("")
    val tip: StateFlow<String> get() = _tip

    fun openArchive(file: File) = viewModelScope.launch(Dispatchers.Default) {
        RandomAccessFile(file, "r").use { file ->
            val archive = try {
                SevenZip.openInArchive(null, RandomAccessFileInStream(file))
            } catch (e: SevenZipException) {
                e.printStackTrace()
                return@launch
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
                    for(j in names.indices) {
                        val isCurrentNodeDir = j < names.size - 1 || isDir // 只要不是最后一级，那肯定是文件夹
                        currentPtr = parentPtr.children.find { it.isDir == isCurrentNodeDir && it.name == names[j] }
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
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        archiveRef.exchange(null)?.close()
    }
}