package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MessageEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val preferredLanguage by viewModel.preferredLanguage.collectAsState()
    val inputText by viewModel.inputText.collectAsState()

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Auto-scroll to lowest message whenever list expands
    LaunchedEffect(chatHistory.size, isThinking) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "J.A.R.V.I.S.",
                            color = CyberBlue,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            fontSize = 20.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isThinking) CyberBlueDark else CyberBlue)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "System Diagnostics Reinitialization",
                            tint = StarkGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = HologramText
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding() // Auto adjust bottom gesture bar padding
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkBackground,
                            ElectricBlue
                        )
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Holographic Pulse Sphere Area
            Spacer(modifier = Modifier.height(16.dp))
            HolographicDisplay(
                isThinking = isThinking,
                onClick = {
                    viewModel.setInputText("Sir, run a comprehensive system scan.")
                    viewModel.sendMessage()
                }
            )

            // Select Language Configuration Controller
            Spacer(modifier = Modifier.height(12.dp))
            LanguageConfigRow(
                selectedLang = preferredLanguage,
                onSelected = { viewModel.setPreferredLanguage(it) }
            )

            // Dynamic Live Chat Logs Console
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TranslucentSteel)
                    .border(1.dp, CyberMuted, RoundedCornerShape(16.dp))
            ) {
                if (chatHistory.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Awaiting secure network packets...",
                            color = HologramDim,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .testTag("chat_logs_list"),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(chatHistory) { message ->
                            MessageBubble(message = message)
                        }

                        if (isThinking) {
                            item {
                                ThinkingIndicatorBubble()
                            }
                        }
                    }
                }
            }

            // Quick Direct Template Suggestions (Display on zero-input states to avoid clutter)
            if (inputText.isEmpty() && !isThinking) {
                QuickSuggestionsPanel(
                    onSelect = {
                        viewModel.setInputText(it)
                    }
                )
            }

            // Command Mainframe Input Dock
            CommandInputDock(
                inputText = inputText,
                isThinking = isThinking,
                onTextChange = { viewModel.setInputText(it) },
                onSend = {
                    viewModel.sendMessage()
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            )

            // Sophisticated Legal/Design Compliance Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = "Accuracy Notice",
                    tint = StarkGold.copy(alpha = 0.7f),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Gemini is AI and can make mistakes.",
                    color = HologramDim.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Animated custom holographic display representing J.A.R.V.I.S.'s cognitive core
 */
@Composable
fun HolographicDisplay(
    isThinking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "holograms")

    // Primary Rotation representing processor tracking
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 2500 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Inner reverse rotation
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 1800 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    // Pulse size representing live resonance
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsing"
    )

    Box(
        modifier = modifier
            .size(160.dp)
            .testTag("jarvis_hologram_core")
            .clickable(onClick = onClick)
            .drawBehind {
                drawCircle(
                    color = CyberBlue.copy(alpha = 0.05f),
                    radius = size.minDimension / 1.9f * pulseScale
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.3f

            // 1. Draw glowing background outer boundary
            drawCircle(
                color = PulseCircle.copy(alpha = 0.15f),
                radius = radius * pulseScale,
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Draw outer dashed rotating selector ring
            rotate(rotation, pivot = center) {
                drawCircle(
                    color = CyberBlue,
                    radius = radius * 0.95f,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(30f, 20f, 10f, 20f),
                            0f
                        )
                    )
                )
            }

            // 3. Draw inner reverse segmented telemetry ring
            rotate(innerRotation, pivot = center) {
                drawCircle(
                    color = CyberBlueDark.copy(alpha = 0.8f),
                    radius = radius * 0.8f,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(15f, 15f),
                            0f
                        )
                    )
                )
            }

            // 4. Draw Tony Stark Arc Reactor geometric triangular spokes pointing inwards
            val spokesCount = 6
            for (i in 0 until spokesCount) {
                val angleRad = Math.toRadians((i * (360 / spokesCount) + rotateAngleFromThinking(isThinking, rotation)).toDouble())
                val startX = center.x + (radius * 0.7f) * cos(angleRad).toFloat()
                val startY = center.y + (radius * 0.7f) * sin(angleRad).toFloat()
                val endX = center.x + (radius * 0.5f) * cos(angleRad).toFloat()
                val endY = center.y + (radius * 0.5f) * sin(angleRad).toFloat()

                drawLine(
                    color = if (isThinking) StarkGold else CyberBlue,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // 5. Draw processing core node glowing orb
            drawCircle(
                color = if (isThinking) StarkGold.copy(alpha = 0.2f) else CyberBlue.copy(alpha = 0.2f),
                radius = radius * 0.4f * pulseScale
            )

            drawCircle(
                color = if (isThinking) StarkGold else CyberBlue,
                radius = radius * 0.3f,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Distinct central typographical/branding mark for the personal hologram
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isThinking) "SCAN" else "J.A.R.V.I.S",
                color = if (isThinking) StarkGold else CyberBlue,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isThinking) "..." else "ACTIVE",
                color = if (isThinking) StarkGold.copy(alpha = 0.8f) else CyberBlueDark.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun rotateAngleFromThinking(isThinking: Boolean, defaultAngle: Float): Float {
    return if (isThinking) defaultAngle * 1.5f else defaultAngle
}

/**
 * Clean glassmorphic selection controller bar for preferred cognitive language
 */
@Composable
fun LanguageConfigRow(
    selectedLang: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CyberMuted.copy(alpha = 0.6f))
            .border(1.dp, CyberMuted, RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val options = listOf(
            "AUTO" to "AUTO ENG/አማ",
            "EN" to "ENG",
            "AM" to "አማርኛ"
        )

        options.forEach { (code, label) ->
            val isSelected = selectedLang == code
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) CyberBlue else Color.Transparent)
                    .clickable { onSelected(code) }
                    .padding(vertical = 6.dp, horizontal = 12.dp)
                    .testTag("lang_chip_$code"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) DarkBackground else HologramDim,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Futuristic glassmorphic text input panel and send triggers
 */
@Composable
fun CommandInputDock(
    inputText: String,
    isThinking: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .testTag("command_text_input"),
            placeholder = {
                Text(
                    text = "Transmit system command, sir...",
                    color = HologramDim.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            },
            singleLine = false,
            maxLines = 3,
            textStyle = LocalTextStyle.current.copy(
                color = HologramText,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = { onSend() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CyberMuted.copy(alpha = 0.5f),
                unfocusedContainerColor = CyberMuted.copy(alpha = 0.2f),
                focusedBorderColor = CyberBlue,
                unfocusedBorderColor = CyberMuted,
                cursorColor = CyberBlue
            ),
            shape = RoundedCornerShape(24.dp)
        )

        IconButton(
            onClick = onSend,
            enabled = inputText.isNotBlank() && !isThinking,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (inputText.isNotBlank() && !isThinking) CyberBlue else CyberMuted.copy(
                        alpha = 0.5f
                    )
                )
                .testTag("send_command_button")
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Transmit Command Signal",
                tint = if (inputText.isNotBlank() && !isThinking) DarkBackground else HologramDim.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * Intelligent diagnostic suggested templates panel
 */
@Composable
fun QuickSuggestionsPanel(
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val commands = listOf(
            "State your diagnostic capability in Amharic.",
            "Translate Stark Industries' motto to Ge'ez/Amharic.",
            "Diagnose potential logical database latency."
        )

        Text(
            text = "QUICK TELEMETRY CODES:",
            color = StarkGold.copy(alpha = 0.8f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            commands.forEach { command ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberMuted.copy(alpha = 0.4f))
                        .border(0.5.dp, CyberBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable { onSelect(command) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = command,
                        color = HologramDim,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/**
 * Highly styled persistent chat log bubbles
 */
@Composable
fun MessageBubble(message: MessageEntity) {
    val isJarvis = message.sender == "jarvis"
    val alignment = if (isJarvis) Alignment.Start else Alignment.End
    val bubbleColor = if (isJarvis) CyberMuted.copy(alpha = 0.6f) else CyberBlue.copy(alpha = 0.15f)
    val borderStrokeColor = if (isJarvis) CyberMuted else CyberBlue.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isJarvis) {
                // Mini-arc reactor hologram emblem tag
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(CyberBlue)
                        .border(1.dp, HologramText, CircleShape)
                )
                Text(
                    text = "J.A.R.V.I.S.",
                    color = CyberBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Text(
                    text = "SIR",
                    color = StarkGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isJarvis) 4.dp else 16.dp,
                        topEnd = if (isJarvis) 16.dp else 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(bubbleColor)
                .border(
                    1.dp,
                    borderStrokeColor,
                    RoundedCornerShape(
                        topStart = if (isJarvis) 4.dp else 16.dp,
                        topEnd = if (isJarvis) 16.dp else 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (isJarvis) HologramText else Color.White,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Animated "Thinking" state indicator representing quantum compiling
 */
@Composable
fun ThinkingIndicatorBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "indicator")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(StarkGold)
            )
            Text(
                text = "J.A.R.V.I.S.",
                color = StarkGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(CyberMuted.copy(alpha = 0.4f))
                .border(1.dp, StarkGold.copy(alpha = 0.4f), RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Sir, compiling cognitive analysis...",
                    color = StarkGold.copy(alpha = alphaAnim),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
