package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun AdvisorChatScreen(
    viewModel: SecurityViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val chatLoading by viewModel.isAssistantResponding.collectAsState()
    val selectedStrategy by viewModel.activeChatMode.collectAsState()

    var inputFieldText by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()

    // Automatically scroll to bottom on entry/updates
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestedQuestions = listOf(
        "Is public hotel Wi-Fi safe with WPA2?",
        "Check Pegasus zero-day protection criteria.",
        "How to detect malware memory buffers?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // High-Tech Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "WEAVE_SHIELD AI INTELLIGENCE",
                color = CyberPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Interactive Cyber Advisor",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Strategy Selector Block (Low-latency vs. Search Grounded vs. Deep Thinking!)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = BorderStroke(1.dp, CyberGrid),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "SELECT GEN-AI CHANNELS STRATEGY",
                    color = CyberMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StrategyOptionCard(
                        label = "LITE-CORE",
                        desc = "Low-Latency",
                        strategyKey = ChatMode.LITE.name,
                        selectedKey = selectedStrategy.name,
                        onClick = { viewModel.selectChatMode(ChatMode.LITE) },
                        modifier = Modifier.weight(1f)
                    )
                    StrategyOptionCard(
                        label = "SEARCH GROUND",
                        desc = "Live Data",
                        strategyKey = ChatMode.STANDARD_GROUNDING.name,
                        selectedKey = selectedStrategy.name,
                        onClick = { viewModel.selectChatMode(ChatMode.STANDARD_GROUNDING) },
                        modifier = Modifier.weight(1f)
                    )
                    StrategyOptionCard(
                        label = "DEEP THINK",
                        desc = "High Thinking",
                        strategyKey = ChatMode.PRO_THINKING.name,
                        selectedKey = selectedStrategy.name,
                        onClick = { viewModel.selectChatMode(ChatMode.PRO_THINKING) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Active thread log or suggestions
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(CyberGrid, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Chat,
                            contentDescription = null,
                            tint = CyberPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "WeaveShield Diagnostic Crypt-Engine Standby.",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = "Select a prebuilt core safeguard query below to audit active configurations",
                        color = CyberMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestedQuestions.forEach { prompt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        inputFieldText = prompt
                                        viewModel.sendChatMessage(prompt)
                                        inputFieldText = ""
                                    }
                                    .testTag("suggest_chip_${prompt.hashCode()}"),
                                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                                border = BorderStroke(1.dp, CyberGrid),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lightbulb,
                                        contentDescription = null,
                                        tint = CyberPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = prompt,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTIVE CHANNELS LOGHISTORY",
                                color = CyberMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "CLEAR LOGS",
                                color = CyberAccent,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { viewModel.clearChat() }
                                    .testTag("clear_chat_history")
                            )
                        }
                    }

                    items(messages, key = { it.id }) { msg ->
                        ChatBubbleRow(msg)
                    }

                    if (chatLoading) {
                        item {
                            ChatIndicatorBubble()
                        }
                    }
                }
            }
        }

        // Input Field Station
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = BorderStroke(1.dp, CyberGrid),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputFieldText,
                    onValueChange = { inputFieldText = it },
                    placeholder = { Text("Consult WeaveShield secure advice...", color = CyberMuted, fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text_field"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                IconButton(
                    onClick = {
                        if (inputFieldText.isNotBlank()) {
                            viewModel.sendChatMessage(inputFieldText)
                            inputFieldText = ""
                        }
                    },
                    modifier = Modifier
                        .background(CyberPrimary, CircleShape)
                        .size(40.dp)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send advice query",
                        tint = CyberBg,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StrategyOptionCard(
    label: String,
    desc: String,
    strategyKey: String,
    selectedKey: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = strategyKey == selectedKey
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("strategy_card_$strategyKey"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberPrimary else CyberGrid
        ),
        border = BorderStroke(1.dp, if (isSelected) CyberPrimary else CyberGrid),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = if (isSelected) CyberBg else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = desc,
                color = if (isSelected) CyberBg.copy(alpha = 0.70f) else CyberMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ChatBubbleRow(message: ChatMessage) {
    val isUser = message.sender == "user"
    val bubbleBg = if (isUser) CyberPrimary else CyberGrid
    val bubbleTextColor = if (isUser) CyberBg else Color.White

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = bubbleBg),
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (isUser) 12.dp else 2.dp,
                    bottomEnd = if (isUser) 2.dp else 12.dp
                ),
                border = if (!isUser) BorderStroke(1.dp, CyberGrid) else null
            ) {
                Text(
                    text = message.content,
                    color = bubbleTextColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Text(
                text = "Engine: ${message.modelUsed.uppercase()}",
                color = CyberMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
fun ChatIndicatorBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberGrid),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = 2.dp,
                bottomEnd = 12.dp
            ),
            border = BorderStroke(1.dp, CyberGrid)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = CyberPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WeaveShield advisory analyzing payload logic...",
                    color = CyberPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
