package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel

@Composable
fun DashboardScreen(
    viewModel: SecurityViewModel,
    modifier: Modifier = Modifier
) {
    val isScanning by viewModel.isScanning.collectAsState()
    val scanningProgress by viewModel.scanningProgress.collectAsState()
    val stepDescription by viewModel.scanningStepDescription.collectAsState()
    val score by viewModel.calculatedScore.collectAsState()
    val isFirewallActive by viewModel.isFirewallActive.collectAsState()
    val isBrowserShieldActive by viewModel.isBrowserShieldActive.collectAsState()

    // Setup radar rotating sweep rotation state
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRotation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Futuristic Cyber Grid Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberGrid, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "SECURE WEAVE // CORE ENGINE",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyberPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "CYBERSPACE STATUS: ONLINE",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (score > 60) CyberSecondary.copy(alpha = 0.15f)
                                else CyberAccent.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (score > 60) CyberSecondary else CyberAccent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (score > 60) "SHIELD ACTIVE" else "DANGER // SCAN NOW",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (score > 60) CyberSecondary else CyberAccent,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Radar Scanning Sweep Panel
        item {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(CyberSurface)
                    .border(2.dp, CyberGrid, CircleShape)
                    .drawBehind {
                        // Drawing static grid lines in the background
                        drawCircle(
                            color = CyberGrid,
                            radius = size.width * 0.3f,
                            style = Stroke(1f)
                        )
                        drawCircle(
                            color = CyberGrid,
                            radius = size.width * 0.45f,
                            style = Stroke(1.dp.toPx())
                        )
                        drawLine(
                            color = CyberGrid,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = CyberGrid,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = 1f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Spinning Sweep Animation
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    if (isScanning) {
                        rotate(rotationAngle, center) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        CyberPrimary.copy(alpha = 0.4f),
                                        CyberPrimary
                                    ),
                                    center = center
                                ),
                                startAngle = 0f,
                                sweepAngle = 90f,
                                useCenter = true
                            )
                        }
                    }
                }

                // Inner Stats Circle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "SECURITY SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isScanning) "${(scanningProgress * 100).toInt()}%" else "$score",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (score > 75) CyberSecondary else if (score > 50) CyberPrimary else CyberAccent,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isScanning) "SCANNING" else if (score > 75) "OPTIMIZED" else "HARDENING REQ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (score > 75) CyberSecondary else if (score > 50) CyberPrimary else CyberAccent,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // scanning log description
        item {
            Text(
                text = stepDescription,
                style = MaterialTheme.typography.bodySmall,
                color = if (isScanning) CyberPrimary else CyberMuted,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Glowing Core Audit Action Button
        item {
            Button(
                onClick = { viewModel.runVulnerabilityScan() },
                enabled = !isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("scan_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberPrimary,
                    contentColor = CyberBg,
                    disabledContainerColor = CyberSurface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = if (isScanning) Icons.Default.Refresh else Icons.Default.Shield,
                    contentDescription = "Scan icon",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (isScanning) "PROCESSING SHIELD AUDIT" else "INITIATE CYBER THREAT AUDIT",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        // Active Diagnostics Shield Controls (Dashboard configuration cards)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberGrid, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "VULNERABILITY SHEATHS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    // Card 1: Local Firewall
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Firewall",
                                tint = if (isFirewallActive) CyberSecondary else CyberMuted
                            )
                            Column {
                                Text(
                                    text = "Active Firewall Shield",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Filters inbound dynamic port requests",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyberMuted
                                )
                            }
                        }
                        Switch(
                            checked = isFirewallActive,
                            onCheckedChange = { viewModel.toggleFirewall() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberSecondary,
                                checkedTrackColor = CyberSecondary.copy(alpha = 0.3f),
                                uncheckedThumbColor = CyberMuted,
                                uncheckedTrackColor = CyberGrid
                            )
                        )
                    }

                    Divider(color = CyberGrid, thickness = 1.dp)

                    // Card 2: Safe Browser Shield
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Safe browsing",
                                tint = if (isBrowserShieldActive) CyberPrimary else CyberMuted
                            )
                            Column {
                                Text(
                                    text = "Safe Domain Guard",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Realtime DNS block for active malware scripts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyberMuted
                                )
                            }
                        }
                        Switch(
                            checked = isBrowserShieldActive,
                            onCheckedChange = { viewModel.toggleBrowserShield() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberPrimary,
                                checkedTrackColor = CyberPrimary.copy(alpha = 0.3f),
                                uncheckedThumbColor = CyberMuted,
                                uncheckedTrackColor = CyberGrid
                            )
                        )
                    }
                }
            }
        }
    }
}
