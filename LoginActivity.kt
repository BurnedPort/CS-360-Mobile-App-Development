package com.example.inventorytracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var db: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DbHelper(this)

        val usernameText = findViewById<EditText>(R.id.usernameText)
        val passwordText = findViewById<EditText>(R.id.passwordText)
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val createBtn = findViewById<Button>(R.id.createAccountButton)

        loginBtn.setOnClickListener {
            val u = usernameText.text.toString().trim()
            val p = passwordText.text.toString()

            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Enter username and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!db.userExists(u)) {
                Toast.makeText(this, "No account found. Use Create Account.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.validateLogin(u, p)) {
                startActivity(Intent(this, InventoryActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid password.", Toast.LENGTH_SHORT).show()
            }
        }

        createBtn.setOnClickListener {
            val u = usernameText.text.toString().trim()
            val p = passwordText.text.toString()

            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Enter username and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.userExists(u)) {
                Toast.makeText(this, "Username already exists. Log in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val created = db.createUser(u, p)
            if (created) {
                Toast.makeText(this, "Account created. Logged in.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, InventoryActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Could not create account.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}