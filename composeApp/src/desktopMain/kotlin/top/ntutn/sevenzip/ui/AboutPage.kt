package top.ntutn.sevenzip.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import sevenzip.composeapp.generated.resources.Res
import sevenzip.composeapp.generated.resources.about_window_button
import sevenzip.composeapp.generated.resources.app_name
import sevenzip.composeapp.generated.resources.toolbar_project_url
import java.awt.Desktop
import java.net.URI

@Composable
fun AboutPage() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        val scope = rememberCoroutineScope()

        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.size(16.dp))
        TextButton(onClick = {
            scope.launch {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                        .isSupported(Desktop.Action.BROWSE)
                ) {
                    Desktop.getDesktop().browse(URI(getString(Res.string.toolbar_project_url)))
                }

            }
        }) {
            Text(stringResource(Res.string.about_window_button))
        }
    }
}