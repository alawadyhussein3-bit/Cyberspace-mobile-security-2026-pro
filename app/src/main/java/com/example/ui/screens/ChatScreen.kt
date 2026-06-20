package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.remote.ChatMode
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: SecurityViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isResponding by viewModel.isAssistantResponding.collectAsState()
    val activeMode by viewModel.activeChatMode.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom whenever a new message is loaded
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestionPrompts = listOf(
        "Is public Wi-Fi safe with VPN?",
        "How to disable USB debugging?",
        "Explain Zero-Trust security.",
        "Check device vulnerability logs."
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Chat Header with config details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = CyberGrid,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isResponding) CyberAccent else CyberSecondary)
                    )
                    Text(
                        text = "SECURE WEAVE // AI LABS",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Chat logs",
                        tint = CyberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Mode Navigation Tab Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChatModeButton(
                    title = "FAST LITE",
                    isSelected = activeMode == ChatMode.LITE,
                    onClick = { viewModel.selectChatMode(ChatMode.LITE) },
                    modifier = Modifier.weight(1f)
                )
                ChatModeButton(
                    title = "GROUNDING",
                    isSelected = activeMode == ChatMode.STANDARD_GROUNDING,
                    onClick = { viewModel.selectChatMode(ChatMode.STANDARD_GROUNDING) },
                    modifier = Modifier.weight(1f)
                )
                ChatModeButton(
                    title = "THINKING",
                    isSelected = activeMode == ChatMode.PRO_THINKING,
                    onClick = { viewModel.selectChatMode(ChatMode.PRO_THINKING) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Messages Feed Block
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (messages.isEmpty() && !isResponding) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Core vulnerability assistant",
                        tint = CyberPrimary.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "CYBERSECURITY AI MONITOR",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Initiate multi-turn diagnostics. Choose Grounding to query live 2026 threats or Thinking for deep log audits.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = CyberMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageBubble(message)
                    }

                    if (isResponding) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = CyberPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "AI specialist is auditing core payload...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyberPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Suggestions block
        if (messages.isEmpty() && !isResponding) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestionPrompts) { chipPrompt ->
                    SuggestionChip(
                        onClick = {
                            inputPrompt = chipPrompt
                            viewModel.sendChatMessage(chipPrompt)
                            inputPrompt = ""
                        },
                        label = {
                            Text(
                                text = chipPrompt,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyberPrimary
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = CyberSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberGrid)
                    )
                }
            }
        }

        // Floating input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val y = strokeWidth / 2
                    drawLine(
                        color = CyberGrid,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = {
                    Text(
                        text = "Prompt system diagnosis...",
                        color = CyberMuted,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = CyberBg,
                    unfocusedContainerColor = CyberBg,
                    focusedIndicatorColor = CyberPrimary,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 3
            )

            IconButton(
                onClick = {
                    if (inputPrompt.isNotBlank() && !isResponding) {
                        viewModel.sendChatMessage(inputPrompt)
                        inputPrompt = ""
                    }
                },
                enabled = inputPrompt.isNotBlank() && !isResponding,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (inputPrompt.isNotBlank() && !isResponding) CyberPrimary else CyberGrid)
                    .testTag("send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send prompt",
                    tint = if (inputPrompt.isNotBlank() && !isResponding) CyberBg else CyberMuted
                )
            }
        }
    }
}

@Composable
fun ChatModeButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) CyberPrimary else CyberGrid,
            contentColor = if (isSelected) CyberBg else Color.White
        ),
        modifier = modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 12.dp
                    )
                )
                .background(if (isUser) CyberPrimary else CyberSurface)
                .border(
                    1.dp,
                    if (isUser) CyberPrimary else CyberGrid,
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 12.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Tiny Mode metadata tag
                if (!isUser) {
                    Text(
                        text = "SECUREWEAVE // " + message.modelUsed.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = CyberPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) CyberBg else Color.White
                )
            }
        }
    }
}
