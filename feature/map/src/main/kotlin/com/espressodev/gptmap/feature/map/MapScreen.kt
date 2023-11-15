package com.espressodev.gptmap.feature.map

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espressodev.gptmap.core.designsystem.Constants.MEDIUM_PADDING
import com.espressodev.gptmap.core.designsystem.Constants.VERY_HIGH_PADDING
import com.espressodev.gptmap.core.designsystem.component.MapSearchButton
import com.espressodev.gptmap.core.designsystem.component.MapTextField
import com.espressodev.gptmap.core.model.LoadingState
import com.espressodev.gptmap.core.model.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.espressodev.gptmap.core.designsystem.R.string as AppText


@Composable
fun MapRoute(viewModel: MapViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Log.d("MapRoute", "uiState: $uiState")
    MapScreen(
        uiState = uiState,
        onSearchValueChange = viewModel::onSearchValueChange,
        onSearchClick = viewModel::onSearchClick
    )
}

@Composable
private fun MapScreen(
    uiState: MapUiState,
    onSearchValueChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    with(uiState.location) {
        when (this) {
            is Response.Failure -> {}
            Response.Loading -> {}
            is Response.Success -> {
                val latLng: LatLng = data.content.coordinates.let {
                    LatLng(it.latitude, it.longitude)
                }
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(latLng, 15f)
                }
                LaunchedEffect(latLng) {
                    if (data.id != "default")
                        cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng))
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    MapSection(
                        cameraPositionState = cameraPositionState,
                        modifier = Modifier.weight(1f),
                        loadingState = uiState.loadingState
                    )
                    MapBottomBar(uiState.searchValue, onSearchValueChange, onSearchClick)
                }
            }
        }
    }
}


@Composable
private fun MapBottomBar(
    searchValue: String,
    onValueChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Surface {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(MEDIUM_PADDING)
        ) {
            MapTextField(
                value = searchValue,
                placeholder = AppText.map_textField_placeholder,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(MEDIUM_PADDING))
            MapSearchButton(onClick = onSearchClick)
        }
    }
}

@Composable
private fun MapSection(
    modifier: Modifier,
    cameraPositionState: CameraPositionState,
    loadingState: LoadingState
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = loadingState == LoadingState.Loading,
            modifier = Modifier
                .padding(top = VERY_HIGH_PADDING)
                .zIndex(1f)
                .align(Alignment.TopCenter)
        ) {
            CircularProgressIndicator()
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
        ) {
            // TODO: Make default marker, because this is not functionality
            Marker(
                state = MarkerState(position = cameraPositionState.position.target),
                title = "Singapore",
                snippet = "Marker in Singapore"
            )
        }
    }
}


@Composable
@Preview(showBackground = true)
fun MapPreview() {
    MapBottomBar(searchValue = "etiam", onValueChange = {}, onSearchClick = {})
}