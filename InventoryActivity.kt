package com.example.inventorytracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat

class InventoryActivity : AppCompatActivity() {

    private lateinit var db: DbHelper
    private lateinit var adapter: InventoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        db = DbHelper(this)

        val recycler = findViewById<RecyclerView>(R.id.inventoryRecyclerView)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = InventoryAdapter(
            items = emptyList(),
            onDelete = { item ->
                db.deleteItem(item.id)
                refresh()
            },
            onRowClick = { item ->
                showUpdateQtyDialog(item)
            }
        )
        recycler.adapter = adapter

        val nameText = findViewById<EditText>(R.id.newItemNameText)
        val qtyText = findViewById<EditText>(R.id.newItemQtyText)
        val addBtn = findViewById<Button>(R.id.addItemButton)

        val smsBtn = findViewById<Button>(R.id.smsSettingsButton)
        smsBtn.setOnClickListener {
            startActivity(Intent(this, SmsActivity::class.java))
        }

        addBtn.setOnClickListener {
            val name = nameText.text.toString().trim()
            val qtyStr = qtyText.text.toString().trim()

            if (name.isEmpty() || qtyStr.isEmpty()) {
                Toast.makeText(this, "Enter item name and quantity.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val qty = qtyStr.toIntOrNull()
            if (qty == null || qty < 0) {
                Toast.makeText(this, "Quantity must be 0 or greater.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.addItem(name, qty)
            nameText.setText("")
            qtyText.setText("")
            refresh()
        }

        refresh()
    }

    private fun refresh() {
        adapter.update(db.getAllItems())
    }

    private fun sendLowStockSms(itemName: String) {

        val prefs = getSharedPreferences("inventory_prefs", Context.MODE_PRIVATE)
        val smsEnabled = prefs.getBoolean("sms_enabled", false)

        val permissionGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

        if (!smsEnabled || !permissionGranted) return

        val phoneNumber = "5551234567"
        val message = "Inventory Alert: $itemName is out of stock."

        SmsManager.getDefault().sendTextMessage(
            phoneNumber,
            null,
            message,
            null,
            null
        )
    }

    private fun showUpdateQtyDialog(item: InventoryItem) {
        val input = EditText(this).apply { hint = "New quantity" }
        AlertDialog.Builder(this)
            .setTitle("Update Qty: ${item.name}")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newQty = input.text.toString().trim().toIntOrNull()
                if (newQty == null || newQty < 0) {
                    Toast.makeText(this, "Quantity must be 0 or greater.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                db.updateQty(item.id, newQty)

                if (newQty == 0) {
                    sendLowStockSms(item.name)
                }

                refresh()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}