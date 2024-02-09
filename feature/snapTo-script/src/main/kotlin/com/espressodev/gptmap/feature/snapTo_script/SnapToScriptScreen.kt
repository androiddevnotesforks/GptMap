package com.espressodev.gptmap.feature.snapTo_script

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.espressodev.gptmap.core.designsystem.GmIcons
import com.espressodev.gptmap.core.designsystem.R.drawable.ai_icon
import com.espressodev.gptmap.core.designsystem.util.rememberKeyboardAsState
import com.espressodev.gptmap.core.model.AiResponseStatus
import com.espressodev.gptmap.core.model.ImageMessage
import com.espressodev.gptmap.feature.screenshot_gallery.InputSelector
import com.espressodev.gptmap.feature.screenshot_gallery.SnapToScriptUiEvent
import com.espressodev.gptmap.feature.screenshot_gallery.SnapToScriptUiState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import com.espressodev.gptmap.core.designsystem.R.string as AppText

@Composable
fun SnapToScriptRoute(viewModel: SnapToScriptViewModel = hiltViewModel()) {
    val uiState by viewModel.snapToScriptUiState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val onEvent by rememberUpdatedState(newValue = viewModel::onEvent)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        SnapToScriptScreen(
            uiState = uiState,
            rmsFlow = viewModel.rmsFlow,
            messages = messages,
            onEvent = onEvent
        )
    }

    if (!uiState.isPinned) {
        DraggableImage(
            imageUrl = uiState.imageUrl,
            onPinClick = { onEvent(SnapToScriptUiEvent.OnPinClick) },
        )
    }
}

@Composable
fun SnapToScriptScreen(
    uiState: SnapToScriptUiState,
    rmsFlow: SharedFlow<Int>,
    messages: List<ImageMessage>,
    onEvent: (SnapToScriptUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardState by rememberKeyboardAsState()
    val (keyboardHeight, setKeyboardHeight) = remember { mutableStateOf(300.dp) }

    LaunchedEffect(key1 = keyboardState) {
        if (keyboardState.isVisible) {
            setKeyboardHeight(keyboardState.keypadHeight)
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        Messages(
            messages = messages,
            userFirstChar = uiState.userFirstChar,
            imageUrl = uiState.imageUrl,
            aiResponseStatus = uiState.aiResponseStatus,
            isPinned = uiState.isPinned,
            onTypingEnd = { onEvent(SnapToScriptUiEvent.OnTypingEnd) },
            onPinClick = { onEvent(SnapToScriptUiEvent.OnPinClick) },
        )

        ChatTextSection(
            value = uiState.value,
            isTextFieldEnabled = uiState.isTextFieldEnabled,
            rmsFlow = rmsFlow,
            keyboardHeight = keyboardHeight,
            inputSelector = uiState.inputSelector,
            onValueChange = { onEvent(SnapToScriptUiEvent.OnValueChanged(it)) },
            onSendClick = { onEvent(SnapToScriptUiEvent.OnSendClick) },
            onMicClick = { onEvent(SnapToScriptUiEvent.OnMicClick) },
            onMicOffClick = { onEvent(SnapToScriptUiEvent.OnMicOffClick) },
            onKeyboardClick = { onEvent(SnapToScriptUiEvent.OnKeyboardClick) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .statusBarsPadding()
        )
    }
}

@Composable
fun Messages(
    messages: List<ImageMessage>,
    userFirstChar: Char,
    imageUrl: String,
    aiResponseStatus: AiResponseStatus,
    isPinned: Boolean,
    onTypingEnd: () -> Unit,
    onPinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    var isFullScreen by remember { mutableStateOf(value = false) }
    if (isFullScreen) {
        Dialog(onDismissRequest = { isFullScreen = false }) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                NaiveImage(imageUrl = imageUrl, size = 350.dp)
            }
        }
    }
    val isListEmpty = messages.isEmpty()

    Box(modifier = modifier.padding(8.dp)) {
        Column {
            if (isPinned) {
                DefaultImage(
                    imageUrl = imageUrl,
                    onPinClick = onPinClick,
                    onFullScreenClick = { isFullScreen = true },
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                reverseLayout = true,
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                item {
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
                }

                item {
                    Spacer(modifier = Modifier.padding(bottom = 72.dp))
                }

                if (!isListEmpty) {
                    messageList(messages, aiResponseStatus, onTypingEnd, userFirstChar)
                } else {
                    // TODO: Add
                }

                item {
                    Spacer(modifier = Modifier.padding(8.dp))
                }
            }
        }

        val jumpThreshold = with(LocalDensity.current) { 64.dp.toPx() }

        val jumpToBottomEnabled by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex > 1 ||
                        scrollState.firstVisibleItemScrollOffset > jumpThreshold
            }
        }

        JumpToBottom(
            enabled = jumpToBottomEnabled,
            onClick = {
                scope.launch {
                    scrollState.animateScrollToItem(0)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
        )
    }
}

private fun LazyListScope.messageList(
    messages: List<ImageMessage>,
    aiResponseStatus: AiResponseStatus,
    onTypingEnd: () -> Unit,
    userFirstChar: Char
) {
    itemsIndexed(
        items = messages,
        key = { _, message -> message.id }
    ) { index, imageMessage ->
        Spacer(modifier = Modifier.padding(8.dp))

        BotMessageSection(
            message = imageMessage.response,
            isLastItem = index == 0,
            aiResponseStatus = aiResponseStatus,
            onTypingEnd = onTypingEnd
        )

        Spacer(modifier = Modifier.padding(8.dp))
        UserMessageSection(message = imageMessage.request, firstChar = userFirstChar)
    }
}

@Composable
fun JumpToBottom(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = enabled,
        modifier = modifier.offset(y = (-72).dp),
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp
            )
        ) {
            Icon(imageVector = GmIcons.DoubleDownDefault, contentDescription = null)
        }
    }
}

@Composable
fun UserMessageSection(message: String, firstChar: Char) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .offset(y = 2.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = firstChar.toString(), style = MaterialTheme.typography.labelMedium)
        }
        Column {
            Text(
                text = stringResource(id = AppText.you),
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 0.8.sp
            )
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun BotMessageSection(
    message: String,
    isLastItem: Boolean,
    aiResponseStatus: AiResponseStatus,
    onTypingEnd: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BotImage()
        Column {
            Text(
                text = stringResource(id = AppText.gemini),
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 0.8.sp
            )
            when {
                aiResponseStatus == AiResponseStatus.Success && isLastItem -> {
                    TypeWriter(message = message, onTypingEnd = onTypingEnd)
                }

                aiResponseStatus == AiResponseStatus.Loading && isLastItem -> {
                    PulsingBox(
                        size = 16.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                    )
                }

                aiResponseStatus is AiResponseStatus.Error && isLastItem -> {
                    Text(
                        text = aiResponseStatus.t.message ?: "Something went wrong",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                else -> {
                    Text(text = message, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun BotImage() {
    Image(
        painter = painterResource(id = ai_icon),
        contentDescription = null,
        modifier = Modifier
            .size(20.dp)
            .offset(y = 4.dp)
    )
}

@Composable
fun TypeWriterRepeat(
    message: String,
    typeDelayMillis: Long = 10L,
    repeatEffect: Boolean = true,
    repeatDelayMillis: Long = 1000L
) {
    var textToDisplay by remember { mutableStateOf(value = "") }
    var currentIndex by remember { mutableIntStateOf(value = 0) }
    var isTypingForward by remember { mutableStateOf(value = true) }

    LaunchedEffect(message, repeatEffect) {
        while (repeatEffect) {
            if (isTypingForward && currentIndex > message.length) {
                // Once the message is fully typed, pause, then start erasing
                delay(repeatDelayMillis) // Pause at the end
                isTypingForward = false
            } else if (!isTypingForward && currentIndex < 0) {
                // Once the message is fully erased, pause, then start typing
                delay(repeatDelayMillis) // Pause at the start
                isTypingForward = true
            }

            textToDisplay = if (isTypingForward) {
                // Typing forward: add characters
                if (currentIndex >= 0 && currentIndex <= message.length) {
                    message.substring(0, currentIndex)
                } else ""
            } else {
                // Erasing: remove characters from the end
                if (currentIndex >= 0 && currentIndex <= message.length) {
                    message.substring(0, currentIndex)
                } else ""
            }

            currentIndex += if (isTypingForward) 1 else -1
            delay(typeDelayMillis)
        }
    }

    Text(
        text = textToDisplay,
        style = MaterialTheme.typography.bodyLarge
    )
}


@Composable
fun TypeWriter(
    message: String,
    typeDelayMillis: Long = 10L,
    onTypingEnd: () -> Unit
) {
    var textToDisplay by remember { mutableStateOf("") }
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(message) {
        textToDisplay = ""
        currentIndex = 0

        while (currentIndex < message.length) {
            textToDisplay += message[currentIndex]
            delay(typeDelayMillis)
            currentIndex++
        }
        if (message.isNotEmpty())
            onTypingEnd()
    }

    Text(
        text = textToDisplay,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun DefaultImage(
    imageUrl: String,
    onPinClick: () -> Unit,
    onFullScreenClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 175.dp
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box {
            Surface(
                modifier = Modifier
                    .size(size)
                    .shadow(2.dp, RoundedCornerShape(8.dp))
                    .clickable(onClick = onFullScreenClick),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = stringResource(id = AppText.selected_image),
                )
            }
            PinButton(
                onClick = onPinClick,
                icon = GmIcons.PushPinOutlined,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
private fun PinButton(onClick: () -> Unit, icon: ImageVector, modifier: Modifier = Modifier) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = AppText.pin)
        )
    }
}


@Composable
private fun DraggableImage(
    imageUrl: String,
    onPinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State to hold the parent size
    var parentSize by remember { mutableStateOf(IntSize.Zero) }
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }

    // Assuming the image size is known and constant
    val imageSize = with(LocalDensity.current) { 175.dp.roundToPx() }

    var isFullScreen by remember { mutableStateOf(value = false) }
    if (isFullScreen) {
        Dialog(onDismissRequest = { isFullScreen = false }) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                NaiveImage(imageUrl = imageUrl, size = 350.dp)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .systemBarsPadding()
            .padding(bottom = 72.dp)
            .onSizeChanged { size -> parentSize = size }
    ) {
        Box(
            modifier = Modifier
                .offset {
                    // Calculate the max offset to keep the image within the parent bounds
                    val maxX = parentSize.width - imageSize.toFloat()
                    val maxY = parentSize.height - imageSize.toFloat()

                    // Constrain the offset to the bounds
                    val constrainedX = offset.x.coerceIn(0f, maxX)
                    val constrainedY = offset.y.coerceIn(0f, maxY)

                    IntOffset(constrainedX.roundToInt(), constrainedY.roundToInt())
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        val proposedOffset = offset + dragAmount
                        val maxX = parentSize.width - imageSize.toFloat()
                        val maxY = parentSize.height - imageSize.toFloat()

                        // Update the offset with constraints
                        offset = Offset(
                            x = proposedOffset.x.coerceIn(0f, maxX),
                            y = proposedOffset.y.coerceIn(0f, maxY)
                        )
                        change.consume()
                    }
                }
                .clickable { isFullScreen = true }
        ) {
            NaiveImage(imageUrl)
            PinButton(
                onClick = onPinClick,
                icon = GmIcons.PushPinOutlined,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
private fun NaiveImage(imageUrl: String, size: Dp = 175.dp) {
    Surface(
        modifier = Modifier
            .size(size)
            .shadow(2.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(id = AppText.selected_image)
        )
    }
}


@Composable
fun ChatTextSection(
    value: String,
    isTextFieldEnabled: Boolean,
    rmsFlow: SharedFlow<Int>,
    keyboardHeight: Dp,
    inputSelector: InputSelector,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onMicClick: () -> Unit,
    onMicOffClick: () -> Unit,
    onKeyboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier) {
        Column {
            TextFieldSection(
                value = value,
                isTextFieldEnabled = isTextFieldEnabled,
                onValueChange = onValueChange,
                inputSelector = inputSelector,
                onSendClick = onSendClick,
                onMicClick = onMicClick,
                onKeyboardClick = onKeyboardClick
            )
            AnimatedVisibility(inputSelector == InputSelector.MicBox) {
                MicBox(
                    keyboardHeight = keyboardHeight,
                    rmsFlow = rmsFlow,
                    onClick = onMicOffClick
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TextFieldSection(
    value: String,
    isTextFieldEnabled: Boolean,
    onValueChange: (String) -> Unit,
    inputSelector: InputSelector,
    onSendClick: () -> Unit,
    onMicClick: () -> Unit,
    onKeyboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTextFieldEmpty by remember(value) {
        derivedStateOf { value.isBlank() }
    }

    val (textFieldValue) = remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardState by rememberKeyboardAsState()

    var triggerSendLambda by remember { mutableStateOf(value = false) }

    LaunchedEffect(key1 = keyboardState.isVisible) {
        if (triggerSendLambda and !keyboardState.isVisible) {
            onSendClick()
            triggerSendLambda = false
        }
    }

    LaunchedEffect(key1 = inputSelector) {
        if (inputSelector == InputSelector.Keyboard) {
            focusRequester.requestFocus()
        }
    }

    val permission = rememberPermissionState(permission = android.Manifest.permission.RECORD_AUDIO)
    val (shouldShowPermissionDialog, setShouldShowPermissionDialog) = remember {
        mutableStateOf(value = false)
    }

    LaunchedEffect(key1 = shouldShowPermissionDialog) {
        if (shouldShowPermissionDialog and !permission.status.isGranted) {
            permission.launchPermissionRequest()
        }
        setShouldShowPermissionDialog(false)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                onValueChange(it.text)
            },
            enabled = isTextFieldEnabled,
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            placeholder = { Text(stringResource(id = AppText.message)) },
            trailingIcon = {
                if (isTextFieldEmpty) {
                    if (inputSelector in setOf(InputSelector.None, InputSelector.Keyboard)) {
                        IconButton(
                            onClick = {
                                if (!permission.status.isGranted) {
                                    setShouldShowPermissionDialog(true)
                                } else {
                                    focusManager.clearFocus()
                                    onMicClick()
                                }
                            },
                        ) {
                            Icon(
                                GmIcons.MicOutlined,
                                contentDescription = stringResource(id = AppText.mic)
                            )
                        }
                    }
                } else {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            GmIcons.ClearDefault,
                            contentDescription = stringResource(id = AppText.clear)
                        )
                    }
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusEvent { state ->
                    if (state.isFocused) {
                        onKeyboardClick()
                    }
                }
        )

        FilledTonalIconButton(
            onClick = {
                triggerSendLambda = true
                keyboardController?.hide()
            },
            modifier = Modifier.padding(start = 8.dp),
            enabled = !isTextFieldEmpty
        ) {
            Icon(GmIcons.ArrowUpwardDefault, contentDescription = stringResource(id = AppText.send))
        }
    }
}

@Composable
fun PulsingBox(size: Dp, color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "Infinite Pulsing transition")
    val animation = infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "infinite pulsing animation"
    )
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = animation.value
                scaleY = animation.value
            }
            .clip(CircleShape)
            .background(color)

    )
}

@Composable
fun PulsingBoxWithAudio(size: Dp, color: Color, modifier: Modifier = Modifier, scale: Float = 1.0f) {
    val shouldAnimate = scale != 1.0f

    val infiniteTransition = rememberInfiniteTransition(label = "Infinite Pulsing transition 1")
    val animation = if (shouldAnimate) {
        infiniteTransition.animateFloat(
            initialValue = scale * 0.7f,
            targetValue = scale,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1500,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ), label = "infinite pulsing animation 2"
        )
    } else {
        remember { mutableFloatStateOf(scale) }
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = animation.value
                scaleY = animation.value
            }
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun MicBox(keyboardHeight: Dp, rmsFlow: SharedFlow<Int>, onClick: () -> Unit) {
    var duration by remember { mutableStateOf(Duration.ZERO) }
    LaunchedEffect("timer") {
        while (true) {
            delay(1000)
            duration += 1.seconds
        }
    }

    val flow by rmsFlow.collectAsStateWithLifecycle(initialValue = 0)

    val minRms = 0
    val maxRms = 10
    val scale = mapRmsToScale(flow, minRms, maxRms, 0.5f, 1.5f)

    Surface(
        modifier = Modifier
            .height(keyboardHeight)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PulsingBoxWithAudio(
                size = 100.dp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                scale = scale,
                modifier = Modifier.align(Alignment.Center)
            )

            Text(
                duration.toComponents { minutes, seconds, _ ->
                    val min = minutes.toString().padStart(2, '0')
                    val sec = seconds.toString().padStart(2, '0')
                    "$min:$sec"
                },
                Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = GmIcons.RecordDefault, contentDescription = null)
                Text(
                    text = stringResource(id = AppText.tap_to_stop_record),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

fun mapRmsToScale(rms: Int, minRms: Int, maxRms: Int, minScale: Float, maxScale: Float): Float {
    val clampedRms = rms.coerceIn(minRms, maxRms).toFloat()
    return minScale + (clampedRms - minRms) / (maxRms - minRms) * (maxScale - minScale)
}

@Preview(showBackground = true)
@Composable
fun ImageSectionPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        TypeWriterRepeat(message = "Hello World!", typeDelayMillis = 10)
    }
}