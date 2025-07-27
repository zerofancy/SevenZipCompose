package top.ntutn.sevenzip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(customDensity: Density, modifier: Modifier = Modifier.Companion, onDensityChange: (Density) -> Unit) {
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
            Text("Density")
            Slider(densitySliderState)
        }
        Row {
            Text("Font Scale")
            Slider(fontScaleSliderState)
        }
    }
}