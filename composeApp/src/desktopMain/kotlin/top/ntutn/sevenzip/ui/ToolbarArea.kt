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
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.parent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import sevenzip.composeapp.generated.resources.Res
import sevenzip.composeapp.generated.resources.tool_about
import sevenzip.composeapp.generated.resources.tool_create
import sevenzip.composeapp.generated.resources.tool_open_file
import sevenzip.composeapp.generated.resources.tool_setting
import sevenzip.composeapp.generated.resources.tool_unarchive
import sevenzip.composeapp.generated.resources.tool_upward
import sevenzip.composeapp.generated.resources.toolbar_about
import sevenzip.composeapp.generated.resources.toolbar_create
import sevenzip.composeapp.generated.resources.toolbar_extract
import sevenzip.composeapp.generated.resources.toolbar_must_in_same_dir
import sevenzip.composeapp.generated.resources.toolbar_open
import sevenzip.composeapp.generated.resources.toolbar_open_failed
import sevenzip.composeapp.generated.resources.toolbar_setting
import sevenzip.composeapp.generated.resources.toolbar_upward
import top.ntutn.sevenzip.SevenZipViewModel
import top.ntutn.sevenzip.toast.LocalToastController
import top.ntutn.sevenzip.zip.ArchiveNode

@Composable
fun ToolbarArea(
    viewModel: SevenZipViewModel,
    currentNode: ArchiveNode?,
    modifier: Modifier = Modifier.Companion,
    onOpenFileNameChange: (String?) -> Unit = {},
    onOpenSetting: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenAdd: () -> Unit = {},
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
        TextButton(onClick = {
            scope.launch {
                val targetDir = FileKit.openDirectoryPicker()?.file
                viewModel.extractAll(targetDir ?: return@launch)
            }
        }, enabled = currentNode != null) {
            Icon(painterResource(Res.drawable.tool_unarchive), contentDescription = stringResource(Res.string.toolbar_extract))
            Spacer(modifier = Modifier.width(2.dp))
            Text(stringResource(Res.string.toolbar_extract))
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
        TextButton(onClick = {
            onOpenAdd()
            return@TextButton
            scope.launch {
                // todo 这个方式只能选择多个文件，不能选择文件夹
                val files = FileKit.openFilePicker(
                    mode = FileKitMode.Multiple()
                ) ?: return@launch
                if (files.isEmpty()) {
                    return@launch
                }
                val inSameDir = withContext(Dispatchers.IO) {
                    files.all { it.parent() == files.first().parent() }
                }
                if (!inSameDir) {
                    toastController.show(getString(Res.string.toolbar_must_in_same_dir))
                    return@launch
                }
                val baseFile = files.first().parent() ?: return@launch
                val targetFile = FileKit.openFileSaver(baseFile.name, extension = "zip") ?:return@launch
                viewModel.createArchive(baseFile.file, files.map { it.file }, targetFile.file)
            }
        }) {
            Icon(painterResource(Res.drawable.tool_create), contentDescription = stringResource(Res.string.toolbar_create))
            Spacer(modifier = Modifier.width(2.dp))
            Text(stringResource(Res.string.toolbar_create))
        }
        Spacer(modifier = Modifier.size(4.dp))

        TextButton(onClick = onOpenSetting) {
            Icon(painterResource(Res.drawable.tool_setting), contentDescription = stringResource(Res.string.toolbar_setting))
            Spacer(modifier = Modifier.width(2.dp))
            Text(stringResource(Res.string.toolbar_setting))
        }
        Spacer(modifier = Modifier.size(4.dp))
        TextButton(onClick = {
            onOpenAbout()
        }) {
            Icon(painterResource(Res.drawable.tool_about), contentDescription = stringResource(Res.string.toolbar_setting))
            Spacer(modifier = Modifier.width(2.dp))
            Text(stringResource(Res.string.toolbar_about))
        }
    }
}