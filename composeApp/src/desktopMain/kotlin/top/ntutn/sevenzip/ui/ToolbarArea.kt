package top.ntutn.sevenzip.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import sevenzip.composeapp.generated.resources.Res
import sevenzip.composeapp.generated.resources.toolbar_about
import sevenzip.composeapp.generated.resources.toolbar_open
import sevenzip.composeapp.generated.resources.toolbar_setting
import sevenzip.composeapp.generated.resources.toolbar_upward
import top.ntutn.sevenzip.ArchiveNode
import top.ntutn.sevenzip.SevenZipViewModel
import java.awt.Desktop
import java.net.URI

@Composable
fun ToolbarArea(
    viewModel: SevenZipViewModel,
    currentNode: ArchiveNode?,
    modifier: Modifier = Modifier.Companion,
    onOpenFileNameChange: (String?) -> Unit = {},
    onOpenSetting: () -> Unit = {}
) {
    Row(modifier) {
        val scope = rememberCoroutineScope()
        Button(onClick = {
            viewModel.moveBack()
        }, enabled = currentNode?.parent != null) {
            Text(stringResource(Res.string.toolbar_upward))
        }
        Spacer(modifier = Modifier.size(4.dp))
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
        Spacer(modifier = Modifier.size(4.dp))
        Button(onClick = onOpenSetting) {
            Text(stringResource(Res.string.toolbar_setting))
        }
        Spacer(modifier = Modifier.size(4.dp))
        Button(onClick = {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI("https://github.com/zerofancy/SevenZipCompose"))
            }
        }) {
            Text(stringResource(Res.string.toolbar_about))
        }
    }
}