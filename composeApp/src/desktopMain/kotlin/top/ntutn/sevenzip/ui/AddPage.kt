package top.ntutn.sevenzip.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        Box(modifier = Modifier.fillMaxHeight().weight(1f)) { // 左边布局，文件区域
            if (currentNode == rootArchiveNode) {
                Text("在这里显示已经添加的文件")
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
                    OutlinedButton(onClick = { currentNode = currentNode.parent ?: rootArchiveNode }, enabled = currentNode != rootArchiveNode) {
                        Text("上级目录")
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedButton(onClick = { TODO() }) {
                        Text("添加文件")
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedButton(onClick = { TODO() }) {
                        Text("添加目录")
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedButton(onClick = { TODO() }) {
                        Text("创建目录")
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                    Button(onClick = { TODO() }, enabled = currentNode != rootArchiveNode || currentNode.children.isNotEmpty()) {
                        Text("创建归档")
                    }
                }
            }
        }
    }
}