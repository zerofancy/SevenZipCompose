package top.ntutn.sevenzip.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import sevenzip.composeapp.generated.resources.Res
import sevenzip.composeapp.generated.resources.new_window_add_file_button
import sevenzip.composeapp.generated.resources.new_window_add_folder_button
import sevenzip.composeapp.generated.resources.new_window_create_archive
import sevenzip.composeapp.generated.resources.new_window_empty_tip
import sevenzip.composeapp.generated.resources.new_window_upward_button
import top.ntutn.sevenzip.zip.ArchiveNode

@Composable
fun AddPage(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        val rootArchiveNode = remember {
            ArchiveNode().also { // 这是个根节点，不会打到压缩包里
                it.name = "ROOT_NODE"
                it.isDir = true
            }
        }
        var currentNode by remember { mutableStateOf(rootArchiveNode) }
        Box(modifier = Modifier.fillMaxHeight().weight(1f), contentAlignment = Alignment.Center) { // 左边布局，文件区域
            if (currentNode == rootArchiveNode) {
                Text(stringResource(Res.string.new_window_empty_tip))
            } else {
                ContentArea(
                    currentNode = currentNode,
                    tryUseSystemIcon = true,
                    onAccessNode = {
                        if (it.isDir) {
                            currentNode = it
                        }
                    }
                )
            }
        }
        Box(modifier = Modifier.width(256.dp).fillMaxHeight()) { // 右边布局，按钮和操作区域
            Surface(
                modifier = Modifier.fillMaxSize(),
                shadowElevation = 4.dp,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
                    OutlinedButton(
                        onClick = { currentNode = currentNode.parent ?: rootArchiveNode },
                        enabled = currentNode != rootArchiveNode,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.new_window_upward_button))
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedButton(onClick = { TODO() }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.new_window_add_file_button))
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedButton(onClick = { TODO() }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.new_window_add_folder_button))
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedButton(onClick = { TODO() }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.new_window_add_folder_button))
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                    Button(
                        onClick = { TODO() },
                        enabled = currentNode != rootArchiveNode || currentNode.children.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.new_window_create_archive))
                    }
                }
            }
        }
    }
}