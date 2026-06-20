package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.ChecklistItem
import com.example.data.model.SecurityScan
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    
    private var isFirebaseInitialized = false
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _currentUserFlow = MutableStateFlow<FirebaseUser?>(null)
    val currentUserFlow: StateFlow<FirebaseUser?> = _currentUserFlow

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    fun init(context: Context) {
        try {
            // Check if Firebase is configured in the project
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            isFirebaseInitialized = true
            
            // Listen for auth state changes
            auth?.addAuthStateListener { firebaseAuth ->
                _currentUserFlow.value = firebaseAuth.currentUser
            }
            Log.d(TAG, "Firebase initialized successfully.")
        } catch (e: Exception) {
            isFirebaseInitialized = false
            auth = null
            firestore = null
            Log.w(TAG, "Firebase initialization skipped (google-services.json might be missing): ${e.message}")
        }
    }

    fun isAvailable(): Boolean = isFirebaseInitialized

    fun getUserId(): String? {
        return auth?.currentUser?.uid ?: _currentUserFlow.value?.uid
    }

    suspend fun syncChecklist(items: List<ChecklistItem>) {
        if (!isFirebaseInitialized) return
        val userId = getUserId() ?: return
        val db = firestore ?: return
        
        _isSyncing.value = true
        try {
            val batch = db.batch()
            items.forEach { item ->
                val docRef = db.collection("users").document(userId)
                    .collection("checklist").document(item.id.toString())
                batch.set(docRef, mapOf(
                    "id" to item.id,
                    "title" to item.title,
                    "description" to item.description,
                    "category" to item.category,
                    "isCompleted" to item.isCompleted,
                    "riskWeight" to item.riskWeight,
                    "lastUpdated" to System.currentTimeMillis()
                ))
            }
            batch.commit().await()
            Log.d(TAG, "Checklist synced with firestore successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing checklist with Firestore", e)
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun syncScan(scan: SecurityScan) {
        if (!isFirebaseInitialized) return
        val userId = getUserId() ?: return
        val db = firestore ?: return
        
        try {
            db.collection("users").document(userId)
                .collection("scans").document(scan.id.toString())
                .set(mapOf(
                    "id" to scan.id,
                    "timestamp" to scan.timestamp,
                    "securityScore" to scan.securityScore,
                    "threatsCount" to scan.threatsCount,
                    "highRiskAppsCount" to scan.highRiskAppsCount,
                    "wifiSecured" to scan.wifiSecured,
                    "firewallActive" to scan.firewallActive,
                    "scanDetails" to scan.scanDetails
                )).await()
            Log.d(TAG, "Scan log synced with Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing scan log", e)
        }
    }

    suspend fun syncMessage(message: ChatMessage) {
        if (!isFirebaseInitialized) return
        val userId = getUserId() ?: return
        val db = firestore ?: return
        
        try {
            db.collection("users").document(userId)
                .collection("chat_history").document(message.id.toString())
                .set(mapOf(
                    "id" to message.id,
                    "sender" to message.sender,
                    "content" to message.content,
                    "timestamp" to message.timestamp,
                    "modelUsed" to message.modelUsed
                )).await()
            Log.d(TAG, "Chat message synced with Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing chat message", e)
        }
    }

    suspend fun loadChecklistFromFirestore(): List<ChecklistItem>? {
        if (!isFirebaseInitialized) return null
        val userId = getUserId() ?: return null
        val db = firestore ?: return null

        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("checklist").get().await()
            snapshot.documents.map { doc ->
                ChecklistItem(
                    id = doc.getLong("id")?.toInt() ?: 0,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    category = doc.getString("category") ?: "Device",
                    isCompleted = doc.getBoolean("isCompleted") ?: false,
                    riskWeight = doc.getLong("riskWeight")?.toInt() ?: 10
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading checklist from Firestore", e)
            null
        }
    }

    fun getAuthInstance(): FirebaseAuth? = auth
}
