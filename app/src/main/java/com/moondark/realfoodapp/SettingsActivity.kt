package com.moondark.realfoodapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.moondark.realfoodapp.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var currencyManager: CurrencyManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currencyManager = CurrencyManager(this)

        // Cargar datos guardados (si existen)
        loadUserData()
        setupCurrencySelector()

        // Botón Admin
        binding.adminButton.setOnClickListener {
            showAdminPasswordDialog()
        }

        // Botón Guardar
        binding.saveButton.setOnClickListener {
            saveUserData()
        }

        // Navegación inferior
        binding.homeButton.setOnClickListener {
            startActivity(Intent(this, MainMenuActivity::class.java))
        }

        binding.cartButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.profileButton.setOnClickListener {
            // Ya estamos aquí
        }
    }

    private fun showAdminPasswordDialog() {
        val input = EditText(this)
        input.hint = "Contraseña de administrador"
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        // Padding para el EditText
        val padding = resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 4
        input.setPadding(padding, padding, padding, padding)

        AlertDialog.Builder(this)
            .setTitle("🔒 Acceso Administrador")
            .setMessage("Ingresa la contraseña para acceder al panel de administración:")
            .setView(input)
            .setPositiveButton("Entrar") { _, _ ->
                val password = input.text.toString()
                if (password == "admin123") {  // ← Cambia esta contraseña
                    startActivity(Intent(this, AdminActivity::class.java))
                    Toast.makeText(this, "✅ Acceso concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_lock_lock)
            .show()
    }

    private fun loadUserData() {
        val sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)

        binding.nameInput.setText(sharedPreferences.getString("name", ""))
        binding.birthdayInput.setText(sharedPreferences.getString("birthday", ""))
        binding.emailInput.setText(sharedPreferences.getString("email", ""))
        binding.phoneInput.setText(sharedPreferences.getString("phone", ""))
        binding.addressInput.setText(sharedPreferences.getString("address", ""))
        binding.cityInput.setText(sharedPreferences.getString("city", ""))
    }

    private fun saveUserData() {
        val name = binding.nameInput.text.toString()
        val birthday = binding.birthdayInput.text.toString()
        val email = binding.emailInput.text.toString()
        val phone = binding.phoneInput.text.toString()
        val address = binding.addressInput.text.toString()
        val city = binding.cityInput.text.toString()

        if (name.isBlank() || email.isBlank()) {
            Toast.makeText(this, "⚠️ Nombre y email son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("name", name)
        editor.putString("birthday", birthday)
        editor.putString("email", email)
        editor.putString("phone", phone)
        editor.putString("address", address)
        editor.putString("city", city)
        editor.apply()

        Toast.makeText(this, "✅ Datos guardados exitosamente", Toast.LENGTH_SHORT).show()
    }
    private fun setupCurrencySelector() {
        val currentCurrency = currencyManager.getSelectedCurrency()
        val currentSymbol = currencyManager.getCurrencySymbol()
        binding.currencyButton.text = "$currentSymbol $currentCurrency"

        binding.currencyButton.setOnClickListener {
            showCurrencyDialog()
        }
    }

    private fun showCurrencyDialog() {
        val currencies = currencyManager.availableCurrencies
        val currencyNames = currencies.map { "${it.symbol} ${it.code} - ${it.name}" }.toTypedArray()
        val currentCurrency = currencyManager.getSelectedCurrency()
        val currentIndex = currencies.indexOfFirst { it.code == currentCurrency }

        AlertDialog.Builder(this)
            .setTitle("💱 Seleccionar Moneda")
            .setSingleChoiceItems(currencyNames, currentIndex) { dialog, which ->
                val selectedCurrency = currencies[which]
                currencyManager.saveSelectedCurrency(selectedCurrency.code)
                binding.currencyButton.text = "${selectedCurrency.symbol} ${selectedCurrency.code}"

                Toast.makeText(
                    this,
                    "✅ Moneda cambiada a ${selectedCurrency.name}",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


}
