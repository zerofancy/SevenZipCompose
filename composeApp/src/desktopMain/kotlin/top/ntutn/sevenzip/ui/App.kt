package top.ntutn.sevenzip.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import top.ntutn.sevenzip.SevenZipViewModel


@Composable
@Preview
fun App(onOpenSetting: () -> Unit = {}) {
    MaterialTheme {
        Column {
            val viewModel = viewModel(SevenZipViewModel::class)
            val currentNode by viewModel.browsingNode.collectAsState()

            // toolbar
            Row {
                val scope = rememberCoroutineScope()
                Button(onClick = {
                    viewModel.moveBack()
                }, enabled = currentNode?.parent != null) {
                    Text("Back")
                }
                Button(onClick = {
                    scope.launch {
                        val kitFile = FileKit.openFilePicker()?.file ?: return@launch
                        viewModel.openArchive(kitFile)
                    }
                }) {
                    Text("Open")
                }
                Button(onClick = onOpenSetting) {
                    Text("Setting")
                }
            }
            // content
            Box {
                ContentArea(currentNode, onEnterDir = viewModel::enterFolder)
            }
        }
    }
}
