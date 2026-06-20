package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_items")
data class ChecklistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "Account", "Device", "Network", "Privacy"
    val isCompleted: Boolean = false,
    val riskWeight: Int // Out of 100, used to compute overall security score
)
