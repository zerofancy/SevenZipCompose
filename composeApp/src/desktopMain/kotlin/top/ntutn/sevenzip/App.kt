package top.ntutn.sevenzip

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column {
            val viewModel = viewModel(SevenZipViewModel::class)

            // toolbar
            Row {
                val scope = rememberCoroutineScope()
                Button(onClick = {
                    scope.launch {
                        val kitFile = FileKit.openFilePicker()?.file ?: return@launch
                        viewModel.openArchive(kitFile)
                    }
                }) {
                    Text("Open")
                }
            }
            // content
            Box {
                val currentNode by viewModel.browsingNode.collectAsState()
                Column {
                    val node = currentNode
                    if (node == null) {
                        Text("No open file now")
                    } else {
                        Button(onClick = {
                            viewModel.moveBack()
                        }) {
                            Text("Back")
                        }
                        LazyColumn {
                            items(node.children) { childrenNode ->
                                Row {
                                    val resource by derivedStateOf {
                                        FileIconUtils.getIconPath(childrenNode.isDir, childrenNode.name)
                                    }
                                    Image(
                                        painter = painterResource(resource),
                                        contentDescription = null,
                                    )
                                    Text(childrenNode.name)
                                    if (childrenNode.isDir) {
                                        Button(onClick = {
                                            viewModel.enterFolder(childrenNode)
                                        }) {
                                            Text("Browse")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}