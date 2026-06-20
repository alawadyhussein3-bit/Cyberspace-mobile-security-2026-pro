package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SecurityDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.ChecklistItem
import com.example.data.model.SecurityScan
import com.example.data.remote.ChatMode
import com.example.data.remote.FirebaseManager
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiResponse
import com.example.data.repository.SecurityRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SecurityViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "SecurityViewModel"

    private val database = SecurityDatabase.getDatabase(application)
    private val repository = SecurityRepository(database.securityDao(), application)

    // --- Database State FLOWS ---
    val allScans: StateFlow<List<SecurityScan>> = repository.allScans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checklistItems: StateFlow<List<ChecklistItem>> = repository.checklistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic Security Metrics ---
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _scanningProgress = MutableStateFlow(0f)
    val scanningProgress: StateFlow<Float> = _scanningProgress

    private val _scanningStepDescription = MutableStateFlow("System Idle")
    val scanningStepDescription: StateFlow<String> = _scanningStepDescription

    // Local Configuration Toggles
    private val _isFirewallActive = MutableStateFlow(true)
    val isFirewallActive: StateFlow<Boolean> = _isFirewallActive

    private val _isBrowserShieldActive = MutableStateFlow(false)
    val isBrowserShieldActive: StateFlow<Boolean> = _isBrowserShieldActive

    // Compute dynamic security score combining latest scan and hardening checklist
    val calculatedScore: StateFlow<Int> = combine(allScans, checklistItems) { scans, checklists ->
        val latestScanScore = scans.firstOrNull()?.securityScore ?: 70
        var checklistProgress = 0
        var totalWeights = 0
        checklists.forEach {
            totalWeights += it.riskWeight
            if (it.isCompleted) {
                checklistProgress += it.riskWeight
            }
        }
        val checklistFactor = if (totalWeights > 0) (checklistProgress * 100) / totalWeights else 100
        
        // Take a balanced split between latest scan score and database checklists
        ((latestScanScore + checklistFactor) / 2).coerceIn(10, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 75)

    // --- Chat Room state ---
    private val _isAssistantResponding = MutableStateFlow(false)
    val isAssistantResponding: StateFlow<Boolean> = _isAssistantResponding

    private val _activeChatMode = MutableStateFlow(ChatMode.LITE)
    val activeChatMode: StateFlow<ChatMode> = _activeChatMode

    // --- Domain Shield Scan State ---
    private val _isScanningDomain = MutableStateFlow(false)
    val isScanningDomain: StateFlow<Boolean> = _isScanningDomain

    private val _domainScanResult = MutableStateFlow<String?>(null)
    val domainScanResult: StateFlow<String?> = _domainScanResult

    // --- Authentication State Flow ---
    val currentUser: StateFlow<FirebaseUser?> = FirebaseManager.currentUserFlow
    val isFirebaseSyncing: StateFlow<Boolean> = FirebaseManager.isSyncing

    init {
        // Initialize Firebase
        FirebaseManager.init(application)
        
        viewModelScope.launch {
            // Seed database checklists if empty
            repository.verifyAndSeedDatabase()
        }
    }

    // --- User Toggle Configurations ---

    fun toggleFirewall() {
        _isFirewallActive.value = !_isFirewallActive.value
        viewModelScope.launch {
            // Refresh checklist status for firewall item check
            val items = checklistItems.value
            items.find { it.title.contains("Threat Shield") }?.let { item ->
                repository.updateChecklistItem(item.copy(isCompleted = _isFirewallActive.value))
            }
        }
    }

    fun toggleBrowserShield() {
        _isBrowserShieldActive.value = !_isBrowserShieldActive.value
    }

    // --- Interactive Local Scanning Sweep Animation ---

    fun runVulnerabilityScan() {
        if (_isScanning.value) return
        _isScanning.value = true
        _scanningProgress.value = 0f
        
        viewModelScope.launch {
            val scanSteps = listOf(
                "Initializing SecureWeave core modules..." to 0.15f,
                "Querying device root integrity status..." to 0.35f,
                "Analyzing network Wi-Fi security keys..." to 0.55f,
                "Auditing active developer settings configurations..." to 0.75f,
                "Investigating third-party package profiles..." to 0.90f,
                "Completing telemetry verification sweep..." to 1.0f
            )

            for (step in scanSteps) {
                _scanningStepDescription.value = step.first
                val targetProgress = step.second
                while (_scanningProgress.value < targetProgress) {
                    _scanningProgress.value += 0.05f
                    delay(80)
                }
                delay(200)
            }

            // Perform real telemetry tests and persist
            val telemetryResult = repository.runTelemetryThreatScan(_isFirewallActive.value)
            repository.insertScan(telemetryResult)

            // Update respective checklists automatically based on real telemetry
            val devOptions = repository.isDeveloperOptionsEnabled()
            val wifiSec = repository.isNetworkSecure()
            val screenLock = repository.isDeviceSecure()

            checklistItems.value.forEach { item ->
                val shouldBeCompleted = when {
                    item.title.contains("Screen Lock") -> screenLock
                    item.title.contains("Developer") -> !devOptions
                    item.title.contains("Wi-Fi") -> wifiSec
                    else -> null
                }
                if (shouldBeCompleted != null && item.isCompleted != shouldBeCompleted) {
                    repository.updateChecklistItem(item.copy(isCompleted = shouldBeCompleted))
                }
            }

            _isScanning.value = false
            _scanningStepDescription.value = "SecureWeave Protection Active"
        }
    }

    // --- Checklist Interactive Hardening Toggle ---

    fun toggleChecklistItem(item: ChecklistItem) {
        viewModelScope.launch {
            val updated = item.copy(isCompleted = !item.isCompleted)
            repository.updateChecklistItem(updated)
        }
    }

    // --- Multiturn Gemini Chat Assistant ---

    fun sendChatMessage(content: String) {
        if (content.isBlank() || _isAssistantResponding.value) return

        viewModelScope.launch {
            // Save User message
            val userMsg = ChatMessage(
                sender = "user",
                content = content,
                modelUsed = _activeChatMode.value.name
            )
            repository.insertMessage(userMsg)

            _isAssistantResponding.value = true

            // Set system parameters based on chat mode
            val mode = _activeChatMode.value
            val systemDoc = """
                You are a highly specialised Mobile Security AI expert at Cyberspace mobile laboratories, codename SecureWeave. 
                Your role is to diagnose Android vulnerabilities, guide users through hardening checklists, inspect log payloads, and review security configurations.
                Explain things clearly to non-engineers, but remain deeply precise on security mitigations.
                Mode: ${mode.name}. Provide concise, effective instructions with bold guidelines.
            """.trimIndent()

            val response = GeminiClient.generateContent(
                prompt = content,
                systemInstruction = systemDoc,
                mode = mode
            )

            val botContent = when (response) {
                is GeminiResponse.Success -> {
                    val textBuilder = StringBuilder(response.text)
                    if (response.sources.isNotEmpty()) {
                        textBuilder.append("\n\n**Vulnerability References Found (Search Grounding):**")
                        response.sources.forEach { source ->
                            textBuilder.append("\n• [${source.title}](${source.url})")
                        }
                    }
                    textBuilder.toString()
                }
                is GeminiResponse.Error -> "Diagnosis Error: ${response.message}"
            }

            val assistantMsg = ChatMessage(
                sender = "assistant",
                content = botContent,
                modelUsed = mode.name
            )
            repository.insertMessage(assistantMsg)
            _isAssistantResponding.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    fun selectChatMode(mode: ChatMode) {
        _activeChatMode.value = mode
    }

    // --- AI Phishing Domain / URL Threat Shield Scan ---

    fun scanDomainShieldUrl(url: String) {
        if (url.isBlank() || _isScanningDomain.value) return

        _isScanningDomain.value = true
        _domainScanResult.value = "Analyzing cryptographic endpoints of '$url'..."

        viewModelScope.launch {
            val auditPrompt = """
                Perform deep domain inspect and lookup for '$url'. 
                Determine if this URL represents a potential phishing threat, suspicious dynamic redirect, or is a secure domain path.
                Utilize search grounding capabilities to check known active security registers or domain blacklists.
                Provide your assessment in 4 short bullet points:
                1. Domain Classification (Secure/Suspicious/Phishing)
                2. Registrar & Cryptographic Security Integrity 
                3. Historical/Active Incidents & Scams Associated
                4. Cyberspace Mitigation Core Recommendation
            """.trimIndent()

            // Run with search grounding to check latest dynamic blacklists!
            val response = GeminiClient.generateContent(
                prompt = auditPrompt,
                systemInstruction = "You are Cyberspace Domain Shield inspect system. Run a deep sandbox audit.",
                mode = ChatMode.STANDARD_GROUNDING
            )

            _domainScanResult.value = when (response) {
                is GeminiResponse.Success -> response.text
                is GeminiResponse.Error -> "Shield scan error: ${response.message}"
            }
            _isScanningDomain.value = false
        }
    }

    fun clearDomainResult() {
        _domainScanResult.value = null
    }

    // --- Authentication Actions (Firebase Integration) ---

    fun loginWithCredentials(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val auth = FirebaseManager.getAuthInstance()
        if (auth == null) {
            onError("Firebase Service is not available in offline/local container.")
            return
        }

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                // Sync checklist items upon logon
                val items = checklistItems.value
                FirebaseManager.syncChecklist(items)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "SignIn Failed")
            }
        }
    }

    fun registerWithCredentials(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val auth = FirebaseManager.getAuthInstance()
        if (auth == null) {
            onError("Firebase Service is not available in offline/local container.")
            return
        }

        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                // Seed database checklists to remote
                val items = checklistItems.value
                FirebaseManager.syncChecklist(items)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Sign up failed")
            }
        }
    }

    fun loginAnonymously(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val auth = FirebaseManager.getAuthInstance()
        if (auth == null) {
            onError("Firebase Service is not configured. Falling back to local offline mode.")
            return
        }

        viewModelScope.launch {
            try {
                auth.signInAnonymously().await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Anonymous Sign-in failed")
            }
        }
    }

    fun logout() {
        val auth = FirebaseManager.getAuthInstance()
        auth?.signOut()
    }
}
