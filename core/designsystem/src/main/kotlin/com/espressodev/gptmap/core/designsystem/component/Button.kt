package com.espressodev.gptmap.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.espressodev.gptmap.core.designsystem.Constants.BUTTON_SIZE
import com.espressodev.gptmap.core.designsystem.Constants.HIGH_PADDING
import com.espressodev.gptmap.core.designsystem.Constants.NO_PADDING
import com.espressodev.gptmap.core.designsystem.GmIcons
import com.espressodev.gptmap.core.designsystem.R.string as AppText


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapSearchButton(
    onClick: () -> Unit,
    icon: ImageVector = GmIcons.SearchDefault,
    shape: Shape = RoundedCornerShape(HIGH_PADDING),
    buttonEnabledState: Boolean,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Button(
        shape = shape,
        enabled = buttonEnabledState,
        onClick = {
            keyboardController?.hide()
            onClick()
        },
        modifier = Modifier.size(BUTTON_SIZE),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(NO_PADDING)
    ) {
        Icon(icon, stringResource(id = AppText.search))
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonPreview() {
    MapSearchButton(onClick = {}, buttonEnabledState = true)
}