package com.espressodev.gptmap.feature.map

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import com.espressodev.gptmap.api.gemini.GeminiService
import com.espressodev.gptmap.api.unsplash.UnsplashService
import com.espressodev.gptmap.core.common.DataStoreService
import com.espressodev.gptmap.core.common.GmViewModel
import com.espressodev.gptmap.core.common.LogService
import com.espressodev.gptmap.core.common.snackbar.SnackbarManager
import com.espressodev.gptmap.core.data.FirestoreService
import com.espressodev.gptmap.core.designsystem.Constants.GENERIC_ERROR_MSG
import com.espressodev.gptmap.core.domain.AddDatabaseIfUserIsNewUseCase
import com.espressodev.gptmap.core.domain.GetCurrentLocationUseCase
import com.espressodev.gptmap.core.domain.ImageToAnalysisUseCase
import com.espressodev.gptmap.core.domain.SaveImageToFirebaseStorageUseCase
import com.espressodev.gptmap.core.model.Exceptions
import com.espressodev.gptmap.core.mongodb.RealmSyncService
import com.espressodev.gptmap.core.save_screenshot.SaveScreenshotService
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.espressodev.gptmap.core.designsystem.R.string as AppText

@HiltViewModel
class MapViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val unsplashService: UnsplashService,
    private val saveImageToFirebaseStorageUseCase: SaveImageToFirebaseStorageUseCase,
    private val addDatabaseIfUserIsNewUseCase: AddDatabaseIfUserIsNewUseCase,
    private val realmSyncService: RealmSyncService,
    private val firestoreService: FirestoreService,
    private val dataStoreService: DataStoreService,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val imageToAnalysisUseCase: ImageToAnalysisUseCase,
    @ApplicationContext private val applicationContext: Context,
    private val ioDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
    logService: LogService,
) : GmViewModel(logService) {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    private val myCurrentLocationState
        get() = uiState.value.myCurrentLocationState

    private val locationImages
        get() = uiState.value.location.locationImages

    private val favouriteId
        get() = uiState.value.location.favouriteId

    private val serviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                SaveScreenshotService.ACTION_SERVICE_STARTED -> {}
                SaveScreenshotService.ACTION_SERVICE_STOPPED -> {
                    if (uiState.value.screenshotState == ScreenshotState.STARTED) {
                        _uiState.update { it.copy(screenshotState = ScreenshotState.FINISHED) }
                        applicationContext.unregisterReceiver(this)
                    }
                }
            }
        }
    }

    init {
        launchCatching {
            launch {
                getUserFirstChar()
            }
            launch {
                savedStateHandle.observeFavouriteIdFromBackStack()
            }
            addDatabaseIfUserIsNewUseCase()
        }
    }

    private suspend fun SavedStateHandle.observeFavouriteIdFromBackStack() {
        getStateFlow(FavouriteId, "default")
            .collect {
                if (it != "default") {
                    loadLocationFromFavourite(it)
                }
            }
    }

    fun onEvent(event: MapUiEvent, navigate: (Pair<Float, Float>) -> Unit = {}) {
        when (event) {
            is MapUiEvent.OnSearchValueChanged -> _uiState.update { it.copy(searchValue = event.text) }
            is MapUiEvent.OnSearchClick -> onSearchClick()
            is MapUiEvent.OnImageDismiss -> {
                _uiState.update { it.copy(imageGalleryState = Pair(0, false)) }
            }

            is MapUiEvent.OnImageClick -> _uiState.update {
                it.copy(imageGalleryState = Pair(event.pos, true))
            }

            MapUiEvent.OnFavouriteClick -> onFavouriteClick()
            is MapUiEvent.OnStreetViewClick -> {
                onStreetViewClick(event.latLng, navigate)
            }

            MapUiEvent.OnExploreWithAiClick ->
                _uiState.update { it.copy(bottomSheetState = MapBottomSheetState.DETAIL_CARD) }

            MapUiEvent.OnDetailSheetBackClick ->
                _uiState.update { it.copy(bottomSheetState = MapBottomSheetState.SMALL_INFORMATION_CARD) }

            MapUiEvent.OnBackClick -> reset()

            MapUiEvent.OnScreenshotProcessStarted -> initializeScreenCaptureBroadcastReceiver()
            MapUiEvent.OnMyCurrentLocationClick -> getMyCurrentLocation()
            MapUiEvent.OnUnsetMyCurrentLocationState -> _uiState.update {
                it.copy(myCurrentLocationState = Pair(false, myCurrentLocationState.second))
            }

            MapUiEvent.OnScreenshotProcessCancelled -> reset()
        }
    }

    fun onChatAiClick(navigate: (String) -> Unit, navigateToGallery: () -> Unit) = launchCatching {
        val imageId = dataStoreService.getLatestImageIdForChat()
        if (imageId.isBlank()) {
            navigateToGallery()
        } else {
            navigate(imageId)
        }
    }

    fun onExploreWithAiClick(
        index: Int,
        navigate: (String) -> Unit,
        navigateToGallery: () -> Unit
    ) = launchCatching {
        _uiState.update {
            it.copy(isLoading = true, imageGalleryState = Pair(0, false))
        }

        val analysisId = locationImages[index].analysisId
        val imageId = dataStoreService.getLatestImageIdForChat()
        if (analysisId != "") {
            resetAfterExamineWithAiClick()
            delay(50L)
            if (imageId != analysisId) {
                dataStoreService.setLatestImageIdForChat(analysisId)
                navigateToGallery()
            } else {
                navigate(imageId)
            }
        } else {
            val imageAnalysisId =
                imageToAnalysisUseCase(imageUrl = locationImages[index].imageUrl).getOrThrow()
            resetAfterExamineWithAiClick()
            delay(50L)
            navigateToGallery()

            if (favouriteId.isNotEmpty()) {
                realmSyncService.updateImageAnalysisId(
                    favouriteId = favouriteId,
                    messageId = locationImages[index].id,
                    imageAnalysisId = imageAnalysisId
                ).getOrThrow()
            }
        }
    }

    private fun resetAfterExamineWithAiClick() {
        _uiState.update {
            it.copy(
                bottomSheetState = MapBottomSheetState.BOTTOM_SHEET_HIDDEN,
                isLoading = false,
                searchBarState = true
            )
        }
    }

    private fun getMyCurrentLocation() = launchCatching {
        if (myCurrentLocationState.second.first != 0.0) {
            _uiState.update {
                it.copy(
                    myCurrentLocationState = Pair(
                        true,
                        myCurrentLocationState.second
                    )
                )
            }
            return@launchCatching
        }

        _uiState.update {
            it.copy(
                isMyLocationButtonVisible = false,
                componentLoadingState = ComponentLoadingState.MY_LOCATION
            )
        }

        getCurrentLocationUseCase().collect { locationResult ->
            locationResult
                .onSuccess { location ->
                    _uiState.update { it.copy(myCurrentLocationState = Pair(true, location)) }
                    finishLoadingMyLocation()
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: GENERIC_ERROR_MSG
                    when (throwable) {
                        is Exceptions.GpsNotEnabledException -> {
                            SnackbarManager.showMessage(message)
                        }

                        is Exceptions.LocationNullException -> {
                            SnackbarManager.showMessage(message)
                        }

                        else -> {
                            SnackbarManager.showMessage(message)
                        }
                    }
                    finishLoadingMyLocation()
                }
        }
    }

    private fun finishLoadingMyLocation() {
        _uiState.update {
            it.copy(
                isMyLocationButtonVisible = true,
                componentLoadingState = ComponentLoadingState.NOTHING
            )
        }
    }

    private fun onSearchClick() = launchCatching {
        _uiState.update {
            it.copy(
                componentLoadingState = ComponentLoadingState.MAP,
                searchButtonEnabledState = false,
                searchTextFieldEnabledState = false,
            )
        }

        geminiService.getLocationInfo(uiState.value.searchValue)
            .onSuccess { location ->
                _uiState.update {
                    it.copy(
                        location = location,
                        componentLoadingState = ComponentLoadingState.NOTHING,
                        searchButtonEnabledState = true,
                        searchTextFieldEnabledState = true,
                        bottomSheetState = MapBottomSheetState.SMALL_INFORMATION_CARD,
                        searchBarState = false,
                        searchValue = ""
                    )
                }

                location.content.city.also { city ->
                    unsplashService.getTwoPhotos(city)
                        .onSuccess { locationImages ->
                            _uiState.update { it.copy(location = location.copy(locationImages = locationImages)) }
                        }
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        componentLoadingState = ComponentLoadingState.NOTHING,
                        searchButtonEnabledState = true,
                        searchTextFieldEnabledState = true,
                        searchBarState = true
                    )
                }
            }
    }

    private fun onFavouriteClick() = launchCatching {
        uiState.value.location.also { location ->
            _uiState.update { state ->
                state.copy(
                    location = state.location.copy(addToFavouriteButtonState = false),
                    isFavouriteButtonPlaying = true
                )
            }
            saveImageToFirebaseStorageUseCase(location)
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            location = state.location.copy(addToFavouriteButtonState = true),
                        )
                    }
                    throw it
                }
        }
    }

    private fun getUserFirstChar() = launchCatching {
        val fullName = dataStoreService.getUserFullName().takeIf { it.isNotEmpty() }
            ?: firestoreService.getUser().fullName.also { fullName ->
                launch {
                    dataStoreService.setUserFullName(fullName)
                }
            }

        _uiState.update { it.copy(userFirstChar = fullName.first()) }
    }

    fun reset() {
        _uiState.update {
            it.copy(
                isMapButtonsVisible = true,
                searchBarState = true,
                isMyLocationButtonVisible = true,
                screenshotState = ScreenshotState.IDLE,
                bottomSheetState = MapBottomSheetState.BOTTOM_SHEET_HIDDEN
            )
        }
    }

    private fun onStreetViewClick(
        latLng: Pair<Double, Double>,
        navigateToStreetView: (Pair<Float, Float>) -> Unit
    ) =
        launchCatching {
            val isStreetAvailable = withContext(ioDispatcher) {
                MapUtils.fetchStreetViewData(LatLng(latLng.first, latLng.second))
            }
            when (isStreetAvailable) {
                Status.OK -> {
                    delay(25L)
                    navigateToStreetView(Pair(latLng.first.toFloat(), latLng.second.toFloat()))
                }

                else -> SnackbarManager.showMessage(AppText.street_view_not_available)
            }
        }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun initializeScreenCaptureBroadcastReceiver() = launchCatching {
        updateUiBeforeProcess()

        val filter = IntentFilter().apply {
            addAction(SaveScreenshotService.ACTION_SERVICE_STARTED)
            addAction(SaveScreenshotService.ACTION_SERVICE_STOPPED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applicationContext.registerReceiver(
                serviceStateReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            applicationContext.registerReceiver(serviceStateReceiver, filter)
        }
    }

    private fun updateUiBeforeProcess() {
        _uiState.update {
            it.copy(
                isMapButtonsVisible = false,
                searchBarState = false,
                isMyLocationButtonVisible = false,
                screenshotState = ScreenshotState.STARTED
            )
        }
    }

    private fun loadLocationFromFavourite(favouriteId: String) = launchCatching {
        val location = withContext(ioDispatcher) {
            realmSyncService.getFavourite(favouriteId)
        }.toLocation()

        _uiState.update {
            it.copy(
                location = location,
                searchBarState = false,
                bottomSheetState = MapBottomSheetState.SMALL_INFORMATION_CARD,
                isMyLocationButtonVisible = false
            )
        }
    }

    override fun onCleared() {
        launchCatching {
            applicationContext.unregisterReceiver(serviceStateReceiver)
        }
        super.onCleared()
    }
}
