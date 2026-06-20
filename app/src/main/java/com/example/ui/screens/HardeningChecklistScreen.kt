package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.model.ChecklistItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel

@Composable
fun HardeningChecklistScreen(
    viewModel: SecurityViewModel,
    modifier: Modifier = Modifier
) {
    val checklists by viewModel.checklistItems.collectAsState()
    val score by viewModel.calculatedScore.collectAsState()
    
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Device", "Network", "Privacy", "Account")

    val filteredItems = remember(checklists, selectedCategory) {
        if (selectedCategory == "All") checklists
        else checklists.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hardening Progress Bar Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberGrid, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HARDENING INTEGRATION PROGRESS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$score/100 SECURE",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (score > 75) CyberSecondary else CyberPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = score / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (score > 75) CyberSecondary else CyberPrimary,
                    trackColor = CyberGrid
                )

                Text(
                    text = "Complete mobile system checks to reach 100% encryption status.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberMuted
                )
            }
        }

        // Horizontal Category Tabs
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            containerColor = Color.Transparent,
            contentColor = CyberPrimary,
            edgePadding = 0.dp,
            divider = {},
            indicator = { tabPositions ->
                val tabIdx = categories.indexOf(selectedCategory)
                if (tabIdx in tabPositions.indices) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabIdx]),
                        color = CyberPrimary
                    )
                }
            }
        ) {
            categories.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = {
                        Text(
                            text = category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    selectedContentColor = CyberPrimary,
                    unselectedContentColor = CyberMuted
                )
            }
        }

        // List of Hardening Steps
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No checklist parameters found.",
                            color = CyberMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    HardeningItemCard(
                        item = item,
                        onToggle = { viewModel.toggleChecklistItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun HardeningItemCard(
    item: ChecklistItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (item.isCompleted) CyberSecondary.copy(alpha = 0.4f) else CyberGrid,
                RoundedCornerShape(12.dp)
            )
            .testTag("checklist_item_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) CyberSurface.copy(alpha = 0.6f) else CyberSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberGrid)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = CyberPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (item.riskWeight >= 20) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberAccent.copy(alpha = 0.15f))
                                .border(1.dp, CyberAccent.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CRITICAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = CyberAccent,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isCompleted) CyberSecondary else Color.White
                )

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberMuted,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = CyberSecondary,
                    uncheckedColor = CyberPrimary,
                    checkmarkColor = CyberBg
                )
            )
        }
    }
}
