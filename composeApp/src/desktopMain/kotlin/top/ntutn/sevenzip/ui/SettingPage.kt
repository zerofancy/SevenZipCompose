package top.ntutn.sevenzip.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skiko.hostOs
import sevenzip.composeapp.generated.resources.Res
import sevenzip.composeapp.generated.resources.setting_window_density
import sevenzip.composeapp.generated.resources.setting_window_font_scale
import sevenzip.composeapp.generated.resources.setting_window_try_use_system_icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(
    customDensity: Density,
    useSystemIcon: Boolean,
    modifier: Modifier = Modifier.Companion,
    onDensityChange: (Density) -> Unit = {},
    onUseSystemIconChange: (Boolean) -> Unit = {}
) {
    Column(modifier = modifier) {
        val densitySliderState = remember {
            SliderState(
                value = 1f,
                steps = 0,
                onValueChangeFinished = {},
                valueRange = 0.5f..5f
            )
        }
        val fontScaleSliderState = remember {
            SliderState(
                value = 1f,
                steps = 0,
                onValueChangeFinished = {},
                valueRange = 0.5f..5f
            )
        }
        densitySliderState.onValueChangeFinished = {
            onDensityChange(Density(densitySliderState.value, fontScaleSliderState.value))
        }
        densitySliderState.value = customDensity.density

        fontScaleSliderState.onValueChangeFinished = {
            onDensityChange(Density(densitySliderState.value, fontScaleSliderState.value))
        }
        fontScaleSliderState.value = customDensity.fontScale

        Row {
            Text(stringResource(Res.string.setting_window_density))
            Slider(densitySliderState)
        }
        Row {
            Text(stringResource(Res.string.setting_window_font_scale))
            Slider(fontScaleSliderState)
        }
        if (hostOs.isWindows || hostOs.isLinux) {
            Row {
                Checkbox(checked = useSystemIcon, onCheckedChange = onUseSystemIconChange)
                Text(stringResource(Res.string.setting_window_try_use_system_icon))
            }
        }
    }
}