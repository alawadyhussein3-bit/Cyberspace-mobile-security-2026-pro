package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel

@Composable
fun ShieldInspectScreen(
    viewModel: SecurityViewModel,
    modifier: Modifier = Modifier
) {
    val isScanningDomain by viewModel.isScanningDomain.collectAsState()
    val scanResult by viewModel.domainScanResult.collectAsState()
    var urlInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Futuristic Card description
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
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Safe browsing shield",
                    tint = CyberPrimary,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        text = "SECUREWEAVE // DOMAIN SHIELD",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyberPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Audit dynamic domains for active phishing registers, cryptographic leaks, and scam behaviors using live Web Grounding.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // URL domain input box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberGrid, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SPECIFY DOMAIN TO AUDIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                TextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = {
                        Text(
                            text = "https://e.g.-compromised-login-portal.com",
                            color = CyberMuted,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("domain_input"),
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
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (urlInput.isNotBlank()) {
                            viewModel.scanDomainShieldUrl(urlInput)
                        }
                    },
                    enabled = urlInput.isNotBlank() && !isScanningDomain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("domain_scan_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberPrimary,
                        contentColor = CyberBg,
                        disabledContainerColor = CyberGrid
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isScanningDomain) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CyberBg,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "RUNNING WEB GROUNDING INSPECT...",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "INITIATE SHIELD WEAVE SCAN",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Result display block
        if (scanResult != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (scanResult!!.contains("phishing", ignoreCase = true) || scanResult!!.contains("suspicious", ignoreCase = true)) CyberAccent else CyberSecondary,
                        RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (scanResult!!.contains("phishing", ignoreCase = true) || scanResult!!.contains("suspicious", ignoreCase = true))
                                    Icons.Default.Warning else Icons.Default.Shield,
                                contentDescription = "Shield State",
                                tint = if (scanResult!!.contains("phishing", ignoreCase = true) || scanResult!!.contains("suspicious", ignoreCase = true)) CyberAccent else CyberSecondary
                            )
                            Text(
                                text = "SANDBOX DIALECT REVENUE LOG",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (scanResult!!.contains("phishing", ignoreCase = true) || scanResult!!.contains("suspicious", ignoreCase = true)) CyberAccent else CyberSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.clearDomainResult() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGrid),
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "CLEAR",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                    }

                    Divider(color = CyberGrid, thickness = 1.dp)

                    Text(
                        text = scanResult!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        lineHeight = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
