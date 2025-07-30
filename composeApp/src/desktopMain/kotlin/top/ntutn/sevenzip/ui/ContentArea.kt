package top.ntutn.sevenzip.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import sevenzip.composeapp.generated.resources.Res
import sevenzip.composeapp.generated.resources.content_area_no_open_file
import top.ntutn.sevenzip.ArchiveNode

@Composable
fun ContentArea(
    currentNode: ArchiveNode?,
    tryUseSystemIcon: Boolean,
    modifier: Modifier = Modifier,
    onEnterDir: (ArchiveNode) -> Unit = {}
) {
    Column(modifier = modifier) {
        if (currentNode == null) {
            Text(stringResource(Res.string.content_area_no_open_file))
        } else {
            FlowRow {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SingleFileIcon(
    childrenNode: ArchiveNode,
    tryUseSystemIcon: Boolean,
    modifier: Modifier = Modifier,
    onDoubleClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .width(160.dp)
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
                .align(Alignment.CenterHorizontally)
        )
        TooltipArea(
            tooltip = {
                Surface(
                    modifier = Modifier
                        .shadow(4.dp)
                ) {
                    Text(childrenNode.name)
                }
            }
        ) {
            Text(
                childrenNode.name,
                textAlign = TextAlign.Center,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}
