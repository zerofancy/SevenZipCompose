package top.ntutn.sevenzip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.RandomAccessFile

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column {
            var tip by remember { mutableStateOf("") }

            // toolbar
            Row {
                val scope = rememberCoroutineScope()
                Button(onClick = {
                    scope.launch {
                        val kitFile = FileKit.openFilePicker()?.file ?: return@launch
                        RandomAccessFile(kitFile, "r").use { file ->
                            val archive = try {
                                SevenZip.openInArchive(null, RandomAccessFileInStream(file))
                            } catch (e: SevenZipException) {
                                e.printStackTrace()
                                return@launch
                            }
                            archive.use {
                                tip = "Current file: ${kitFile.name}, itemCount: ${it.numberOfItems}"
                                val simpleArchive = it.simpleInterface
                                simpleArchive.archiveItems.joinToString("\n", transform = {
                                    "${it.path}\t${it.size}"
                                }).let {
                                    tip += "\n" + it
                                }
                            }
                        }

                    }
                }) {
                    Text("Open")
                }
            }
            // content
            Box {
                Text(tip)
            }
        }
    }
}