package com.example.inventorytracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SmsActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private val prefs by lazy { getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    private val requestSmsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Save choice (only true if granted)
        prefs.edit().putBoolean(KEY_SMS_ENABLED, granted).apply()
        updateStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)

        statusText = findViewById(R.id.smsPermissionStatusText)
        val requestBtn = findViewById<Button>(R.id.requestSmsPermissionButton)

        updateStatus()

        requestBtn.setOnClickListener {
            // If already granted, just store true and refresh UI
            if (hasSmsPermission()) {
                prefs.edit().putBoolean(KEY_SMS_ENABLED, true).apply()
                updateStatus()
            } else {
                requestSmsPermission.launch(Manifest.permission.SEND_SMS)
            }
        }
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun updateStatus() {
        val enabled = prefs.getBoolean(KEY_SMS_ENABLED, false)
        val granted = hasSmsPermission()

        val text = when {
            enabled && granted -> "Permission status: Granted"
            enabled && !granted -> "Permission status: Enabled in app, but not granted (tap button)"
            else -> "Permission status: Disabled"
        }
        statusText.text = text
    }

    companion object {
        private const val PREFS_NAME = "inventory_prefs"
        const val KEY_SMS_ENABLED = "sms_enabled"
    }
}