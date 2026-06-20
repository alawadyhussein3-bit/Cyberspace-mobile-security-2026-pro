package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.FirebaseManager
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel

@Composable
fun ProfileScreen(
    viewModel: SecurityViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isSyncing by viewModel.isFirebaseSyncing.collectAsState()
    val isFirebaseAvailable = remember { firebaseAvailable() }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorType by remember { mutableStateOf(false) }

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
        // Firebase Status Alert
        if (!isFirebaseAvailable) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = CyberAccent
                    )
                    Column {
                        Text(
                            text = "OFFLINE SYNC MODE ONLY",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyberAccent,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "The Firestore remote sync is bypassed because google-services.json is not configured yet. Checking list details will save locally.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Active State Display / Sign-in form
        if (currentUser != null) {
            // Logged In User Block
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberSecondary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SECURE SYNC ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = CyberSecondary,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Text(
                        text = "UID: " + currentUser!!.uid,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )

                    if (currentUser!!.email != null) {
                        Text(
                            text = "EMAIL: " + currentUser!!.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberMuted
                        )
                    } else {
                        Text(
                            text = "ANONYMOUS SHIELD SESSION",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberMuted
                        )
                    }

                    Button(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "DEACTIVATE SESSION SYNC",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Sign in / Sign up Panel
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
                        text = if (isRegistering) "PROMPT NEW REGISTRATION" else "INITIATE SECURE SESSION",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    TextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = { Text("operator@company.com") },
                        label = { Text("Secure Email Input") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CyberBg,
                            unfocusedContainerColor = CyberBg,
                            focusedIndicatorColor = CyberPrimary,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    TextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Encryption Cipher") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CyberBg,
                            unfocusedContainerColor = CyberBg,
                            focusedIndicatorColor = CyberPrimary,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Execute credential flow buttons
                    Button(
                        onClick = {
                            if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                                if (isRegistering) {
                                    viewModel.registerWithCredentials(
                                        emailInput, passwordInput,
                                        onSuccess = {
                                            statusMessage = "Operator registered successfully."
                                            isErrorType = false
                                        },
                                        onError = {
                                            statusMessage = it
                                            isErrorType = true
                                        }
                                    )
                                } else {
                                    viewModel.loginWithCredentials(
                                        emailInput, passwordInput,
                                        onSuccess = {
                                            statusMessage = "Authenticated."
                                            isErrorType = false
                                        },
                                        onError = {
                                            statusMessage = it
                                            isErrorType = true
                                        }
                                    )
                                }
                            }
                        },
                        enabled = isFirebaseAvailable && emailInput.isNotBlank() && passwordInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = CyberBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isRegistering) "PROVISION ACCOUNT" else "DECRYPT ACCESS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Anonymous login fallback option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { isRegistering = !isRegistering }) {
                            Text(
                                text = if (isRegistering) "Switch to Login" else "Create Shield Operator",
                                color = CyberPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        TextButton(
                            onClick = {
                                viewModel.loginAnonymously(
                                    onSuccess = {
                                        statusMessage = "Logged in Anonymously"
                                        isErrorType = false
                                    },
                                    onError = {
                                        statusMessage = it
                                        isErrorType = true
                                    }
                                )
                            },
                            enabled = isFirebaseAvailable
                        ) {
                            Text(
                                "ANONYMOUS LOGIN",
                                color = CyberSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Live Status notifications
                    if (statusMessage != null) {
                        Text(
                            text = statusMessage!!,
                            color = if (isErrorType) CyberAccent else CyberSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Real System Telemetry specifications card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, CyberGrid, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "HARDWARE PLATFORM SPECTRAL REVENUE",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Divider(color = CyberGrid, thickness = 1.dp)

                SystemSpecRow("DEVICE MANUFACTURER", Build.MANUFACTURER.uppercase())
                SystemSpecRow("HARDWARE MODEL", Build.MODEL.uppercase())
                SystemSpecRow("ANDROID ARCH LAYER", Build.CPU_ABI.uppercase())
                SystemSpecRow("OS VERSION (API)", "OS BUILD LEVEL " + Build.VERSION.SDK_INT)
                SystemSpecRow("SYSTEM BOARD NAME", Build.BOARD.uppercase())
                SystemSpecRow("BUILD PROFILE TAG", Build.TAGS.uppercase())
            }
        }
    }
}

@Composable
fun SystemSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = CyberMuted,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )
    }
}

fun firebaseAvailable(): Boolean {
    return FirebaseManager.isAvailable()
}
