package com.moondark.realfoodapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.moondark.realfoodapp.ui.theme.RealFoodAppTheme

class MainActivity : ComponentActivity() {
    lateinit var usernameInput: EditText
    lateinit var phoneNumber: EditText
    lateinit var passwordInput: EditText
    lateinit var createAccountInput: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usernameInput = findViewById(R.id.username_input)
        phoneNumber = findViewById(R.id.phone_number_input)
        passwordInput = findViewById(R.id.password_input)
        createAccountInput = findViewById(R.id.create_account_button)

        createAccountInput.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            Log.i("Test Credentials", "Username: $username and Password : $password")

        }

    }

}