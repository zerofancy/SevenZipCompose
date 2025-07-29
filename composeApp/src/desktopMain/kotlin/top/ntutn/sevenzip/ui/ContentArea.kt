package top.ntutn.sevenzip.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skiko.hostOs
import top.ntutn.sevenzip.ArchiveNode
import top.ntutn.sevenzip.util.FileIconFetcher
import top.ntutn.sevenzip.util.FileIconUtils
import top.ntutn.sevenzip.util.LinuxFileIconProvider
import java.io.File

@Composable
fun ContentArea(
    currentNode: ArchiveNode?,
    tryUseSystemIcon: Boolean,
    modifier: Modifier = Modifier,
    onEnterDir: (ArchiveNode) -> Unit = {}
) {
    Column(modifier = modifier) {
        if (currentNode == null) {
            Text("No open file now")
        } else {
            FlowColumn {
                currentNode.children.forEach { childrenNode ->
                    SingleFileIcon(childrenNode, tryUseSystemIcon, onDoubleClick = {
                        if (childrenNode.isDir) {
                            onEnterDir(childrenNode)
                        }
                    })
                }
            }
        }
    }
}

@Composable
private fun SingleFileIcon(
    childrenNode: ArchiveNode,
    tryUseSystemIcon: Boolean,
    modifier: Modifier = Modifier,
    onDoubleClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .combinedClickable(onDoubleClick = onDoubleClick) {}
    ) {
        var iconPainter by remember {
            mutableStateOf<Painter>(
                ColorPainter(Color.LightGray)
            )
        }
        NodeIconPainter(childrenNode, tryUseSystemIcon = tryUseSystemIcon) {
            if (it != null) {
                iconPainter = it
            }
        }
        Image(
            painter = iconPainter,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
        )
        Text(childrenNode.name)
    }
}
