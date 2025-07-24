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
            var tip = "Current file: ${file}, itemCount: ${archive.numberOfItems}"
            try {
                val simpleArchive = archive.simpleInterface
                simpleArchive.archiveItems.joinToString("\n", transform = {
                    "${it.path}\t${it.size}"
                }).let {
                    tip += "\n" + it
                }
                simpleArchive.close()
            } finally {
                closeableArchive.close()
            }
            _tip.value = tip
        }
    }

    override fun onCleared() {
        super.onCleared()
        archiveRef.exchange(null)?.close()
    }
}