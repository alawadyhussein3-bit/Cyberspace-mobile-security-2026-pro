package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChecklistItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel

@Composable
fun ChecklistScreen(
    viewModel: SecurityViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.checklistItems.collectAsState()
    val securityScore by viewModel.calculatedScore.collectAsState()

    var selectedTabCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Device", "Network", "Privacy", "Account")

    val filteredItems = if (selectedTabCategory == "All") {
        items
    } else {
        items.filter { it.category == selectedTabCategory }
    }

    val completedCount = items.count { it.isCompleted }
    val totalCount = items.size

    val animatedProgress by animateFloatAsState(
        targetValue = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f,
        label = "ChecklistProgress"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-Tech Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SAFEGUARD COMPLIANCE",
                    color = CyberPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Vulnerability Remedies checklist",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Summary Metric card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberGrid),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SAFEGUARD DISPATCH RATING",
                                color = CyberPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${securityScore}% Secure Core Rating",
                                color = if (securityScore < 60) CyberAccent else CyberSecondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "$completedCount / $totalCount DONE",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .border(1.dp, CyberGrid, RoundedCornerShape(3.dp)),
                        color = if (securityScore < 60) CyberAccent else CyberSecondary,
                        trackColor = CyberGrid,
                    )

                    Text(
                        text = "Activating these checks mitigates cyber risks. These choices immediately sync to local SQL and cloud Firestore.",
                        color = CyberMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Horizontal Category Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedTabCategory),
                containerColor = Color.Transparent,
                divider = {},
                indicator = {},
                edgePadding = 0.dp
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedTabCategory == cat
                    Card(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { selectedTabCategory = cat }
                            .testTag("checklist_tab_$cat"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyberPrimary else CyberSurface
                        ),
                        border = BorderStroke(1.dp, if (isSelected) CyberPrimary else CyberGrid),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.uppercase(),
                                color = if (isSelected) CyberBg else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Checklist Items
        if (filteredItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No parameters mapped to this category", color = CyberMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                ChecklistItemRow(
                    item = item,
                    onToggle = { viewModel.toggleChecklistItem(item) }
                )
            }
        }
    }
}

@Composable
fun ChecklistItemRow(
    item: ChecklistItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("checklist_item_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) CyberSurface.copy(alpha = 0.60f) else CyberSurface
        ),
        border = BorderStroke(
            1.dp,
            if (item.isCompleted) CyberGrid else CyberPrimary.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("item_check_${item.id}"),
                colors = CheckboxDefaults.colors(
                    checkedColor = CyberSecondary,
                    checkmarkColor = CyberBg,
                    uncheckedColor = CyberPrimary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.title,
                        color = if (item.isCompleted) CyberMuted else Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Box(
                        modifier = Modifier
                            .background(CyberGrid, RoundedCornerShape(4.dp))
                            .border(1.dp, CyberGrid, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${item.riskWeight} PTS",
                            color = CyberPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                
                Text(
                    text = item.description,
                    color = CyberMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                
                Text(
                    text = "TAG: #${item.category.uppercase()}",
                    color = CyberPrimary.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
