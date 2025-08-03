package top.ntutn.sevenzip.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import org.jetbrains.compose.ui.tooling.preview.Preview
import top.ntutn.sevenzip.SevenZipViewModel
import java.awt.Desktop


@Composable
@Preview
fun App(
    tryUseSystemIcon: Boolean,
    onOpenSetting: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenFileNameChange: (String?) -> Unit
) {
    MaterialTheme {
        Column {
            val viewModel = viewModel(SevenZipViewModel::class)
            val currentNode by viewModel.browsingNode.collectAsState()

            Surface(
                modifier = Modifier,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                ToolbarArea(viewModel,
                    currentNode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    onOpenFileNameChange = onOpenFileNameChange,
                    onOpenSetting = onOpenSetting,
                    onOpenAbout = onOpenAbout,
                )
            }
            val scope = rememberCoroutineScope()
            ContentArea(currentNode,
                modifier = Modifier.padding(16.dp),
                tryUseSystemIcon = tryUseSystemIcon,
                onAccessNode = { node ->
                    if (node.isDir) {
                        viewModel.enterFolder(node)
                    } else {
                        scope.launch {
                            val file = viewModel.extract2Temp(node) ?: return@launch
                            println(file)
                            if (Desktop.isDesktopSupported()) {
                                try {
                                    withContext(Dispatchers.IO) {
                                        Desktop.getDesktop().open(file)
                                    }
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}
