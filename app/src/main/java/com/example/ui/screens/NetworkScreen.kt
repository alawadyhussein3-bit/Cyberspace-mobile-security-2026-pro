package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NetworkScreen(
    modifier: Modifier = Modifier
) {
    var isShieldEnabled by remember { mutableStateOf(true) }
    var runningDiagnosis by remember { mutableStateOf(false) }
    var diagnosisProgress by remember { mutableStateOf(0f) }
    var currentCheckStage by remember { mutableStateOf("Ready to initiate packet intercept scan") }
    val diagnosticsLogs = remember { mutableStateListOf<String>() }

    val scope = rememberCoroutineScope()

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
                    text = "NETWORK AUDIT STATION",
                    color = CyberPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Wi-Fi Shield & DNS Integrity",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // DNS Crypt & Shield Status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberGrid),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.75f)) {
                        Text(
                            text = "DNS Crypt Shield Tunnel",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isShieldEnabled) "Routing traffic via encrypted Cloud flare DNS standard tunnel" else "Outbound data un-encrypted, vulnerable to MITM attacks",
                            color = if (isShieldEnabled) CyberSecondary else CyberAccent,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Switch(
                        checked = isShieldEnabled,
                        onCheckedChange = { isShieldEnabled = it },
                        modifier = Modifier.testTag("dns_shield_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberBg,
                            checkedTrackColor = CyberPrimary,
                            uncheckedThumbColor = CyberMuted,
                            uncheckedTrackColor = CyberGrid
                        )
                    )
                }
            }
        }

        // Wi-Fi details Card
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "LINK PARAMETERS MATRIX",
                        color = CyberPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )

                    NetworkParamRow(label = "Primary SSID Node", value = "SecureWeave_Mesh_5G", icon = Icons.Rounded.Wifi)
                    NetworkParamRow(label = "Local IP Address", value = "192.168.1.144", icon = Icons.Rounded.Dns)
                    NetworkParamRow(label = "Default gateway", value = "192.168.1.1", icon = Icons.Rounded.SettingsEthernet)
                    NetworkParamRow(label = "IP Lease Security", value = "WPA3 Personal (AES Certified)", icon = Icons.Rounded.Lock)
                    NetworkParamRow(label = "External Global Node", value = "172.56.241.98 (Ground-Zero Link)", icon = Icons.Rounded.Public)
                }
            }
        }

        // Diagnostic action card
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "INTEGRITY ORATOR",
                        color = CyberPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                    )

                    if (runningDiagnosis) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "STAGE: $currentCheckStage",
                                color = CyberPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { diagnosisProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .border(1.dp, CyberGrid, RoundedCornerShape(2.dp)),
                                color = CyberPrimary,
                                trackColor = CyberGrid
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                runningDiagnosis = true
                                diagnosticsLogs.clear()
                                scope.launch {
                                    val auditCheckpoints = listOf(
                                        "Initiating high-latency ping signals..." to "Latency response timing stable at 24ms.",
                                        "Checking local ARP translations mapping..." to "No fraudulent MAC redirects discovered.",
                                        "Validating active SSL certificates exchange..." to "Certificates verified successfully (SHA-256 matches).",
                                        "Scanning outbound connections boundaries..." to "0 hidden payload leaks detected.",
                                        "Auditing DNS integrity response records..." to "DNS record answers match authenticated cryptographic hashes."
                                    )
                                    val size = auditCheckpoints.size
                                    for (i in 0 until size) {
                                        val checkpoint = auditCheckpoints[i]
                                        currentCheckStage = checkpoint.first
                                        diagnosisProgress = i.toFloat() / size
                                        delay(1300)
                                        diagnosticsLogs.add("[SUCCESS] ${checkpoint.first}\n↳ RESULT: ${checkpoint.second}")
                                    }
                                    diagnosisProgress = 1.0f
                                    currentCheckStage = "Full networking link audited successfully."
                                    runningDiagnosis = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("run_diagnostics_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberGrid,
                                contentColor = CyberPrimary
                            ),
                            border = BorderStroke(1.dp, CyberPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.NetworkCheck, contentDescription = "Run Audit")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "RUN ARP & SSL HANDSHAKE DIAGNOSTICS", fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(visible = diagnosticsLogs.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HorizontalDivider(color = CyberGrid)
                            Text(
                                text = "SHELL OUTPUT LOGS:",
                                color = CyberMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            
                            diagnosticsLogs.forEach { logLine ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CyberBg),
                                    border = BorderStroke(1.dp, CyberGrid),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = logLine,
                                        color = CyberSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkParamRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyberPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = CyberMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
