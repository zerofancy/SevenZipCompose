package top.ntutn.sevenzip.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import sevenzip.composeapp.generated.resources.Res
import sevenzip.composeapp.generated.resources.tool_about
import sevenzip.composeapp.generated.resources.tool_open_file
import sevenzip.composeapp.generated.resources.tool_setting
import sevenzip.composeapp.generated.resources.tool_upward
import sevenzip.composeapp.generated.resources.toolbar_about
import sevenzip.composeapp.generated.resources.toolbar_open
import sevenzip.composeapp.generated.resources.toolbar_open_failed
import sevenzip.composeapp.generated.resources.toolbar_project_url
import sevenzip.composeapp.generated.resources.toolbar_setting
import sevenzip.composeapp.generated.resources.toolbar_upward
import top.ntutn.sevenzip.ArchiveNode
import top.ntutn.sevenzip.SevenZipViewModel
import top.ntutn.sevenzip.toast.LocalToastController
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
        val toastController = LocalToastController.current

        TextButton(onClick = {
            viewModel.moveBack()
        }, enabled = currentNode?.parent != null) {
            Icon(painterResource(Res.drawable.tool_upward), contentDescription = stringResource(Res.string.toolbar_upward))
            Spacer(modifier = Modifier.width(2.dp))
            Text(stringResource(Res.string.toolbar_upward))
        }
        Spacer(modifier = Modifier.size(4.dp))
        TextButton(onClick = {
            scope.launch {
                val kitFile = FileKit.openFilePicker()?.file ?: return@launch
                if (viewModel.openArchive(kitFile)) {
                    onOpenFileNameChange(kitFile.name)
                } else {
                    toastController.show(getString(Res.string.toolbar_open_failed))
                }
            }
        }) {
            Icon(painterResource(Res.drawable.tool_open_file), contentDescription = stringResource(Res.string.toolbar_open))
            Spacer(modifier = Modifier.width(2.dp))
            Text(stringResource(Res.string.toolbar_open))
        }

        Spacer(modifier = Modifier.size(4.dp))

        TextButton(onClick = onOpenSetting) {
            Icon(painterResource(Res.drawable.tool_setting), contentDescription = stringResource(Res.string.toolbar_setting))
            Spacer(modifier = Modifier.width(2.dp))
            Text(stringResource(Res.string.toolbar_setting))
        }
        Spacer(modifier = Modifier.size(4.dp))
        TextButton(onClick = {
            scope.launch {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(URI(getString(Res.string.toolbar_project_url)))
                } else {
                    toastController.show(getString(Res.string.toolbar_open_failed))
                }
            }
        }) {
            Icon(painterResource(Res.drawable.tool_about), contentDescription = stringResource(Res.string.toolbar_setting))
            Spacer(modifier = Modifier.width(2.dp))
            Text(stringResource(Res.string.toolbar_about))
        }
    }
}