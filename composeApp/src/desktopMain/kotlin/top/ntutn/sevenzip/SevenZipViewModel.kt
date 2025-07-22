package top.ntutn.sevenzip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import java.io.File
import java.io.RandomAccessFile

class SevenZipViewModel : ViewModel() {
    private var archive: IInArchive? = null
    private val _tip = MutableStateFlow("")
    val tip: StateFlow<String> get() = _tip

    fun openArchive(file: File) = viewModelScope.launch(Dispatchers.Default) {
        RandomAccessFile(file, "r").use { file ->
            val archive = try {
                SevenZip.openInArchive(null, RandomAccessFileInStream(file))
            } catch (e: SevenZipException) {
                e.printStackTrace()
                return@launch
            }.also {
                closeCurrentArchive()
                this@SevenZipViewModel.archive = it
            }
            var tip = "Current file: ${file}, itemCount: ${archive.numberOfItems}"
            val simpleArchive = archive.simpleInterface
            simpleArchive.archiveItems.joinToString("\n", transform = {
                "${it.path}\t${it.size}"
            }).let {
                tip += "\n" + it
            }
            simpleArchive.close()
            _tip.value = tip
        }
    }

    override fun onCleared() {
        super.onCleared()
        closeCurrentArchive()
    }

    private fun closeCurrentArchive() {
        val closingArchive = archive
        archive = null
        closingArchive?.close()
    }
}