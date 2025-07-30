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
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import sevenzip.composeapp.generated.resources.Res
import sevenzip.composeapp.generated.resources.toolbar_open
import sevenzip.composeapp.generated.resources.toolbar_setting
import sevenzip.composeapp.generated.resources.toolbar_upward
import top.ntutn.sevenzip.SevenZipViewModel


@Composable
@Preview
fun App(
    tryUseSystemIcon: Boolean,
    onOpenSetting: () -> Unit = {},
    onOpenFileNameChange: (String?) -> Unit
) {
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
                    Text(stringResource(Res.string.toolbar_upward))
                }
                Button(onClick = {
                    scope.launch {
                        val kitFile = FileKit.openFilePicker()?.file ?: return@launch
                        if (viewModel.openArchive(kitFile)) {
                            onOpenFileNameChange(kitFile.name)
                        }
                    }
                }) {
                    Text(stringResource(Res.string.toolbar_open))
                }
                Button(onClick = onOpenSetting) {
                    Text(stringResource(Res.string.toolbar_setting))
                }
            }
            // content
            Box {
                ContentArea(currentNode, tryUseSystemIcon = tryUseSystemIcon, onEnterDir = viewModel::enterFolder)
            }
        }
    }
}
