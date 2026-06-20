package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_scans")
data class SecurityScan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val securityScore: Int,
    val threatsCount: Int,
    val highRiskAppsCount: Int,
    val wifiSecured: Boolean,
    val firewallActive: Boolean,
    val scanDetails: String
)
