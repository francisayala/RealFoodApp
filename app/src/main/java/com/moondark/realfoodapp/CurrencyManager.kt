package com.moondark.realfoodapp

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CurrencyManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("CurrencyPrefs", Context.MODE_PRIVATE)

    // ← IMPORTANTE: Pega aquí tu API Key de ExchangeRate-API
    private val API_KEY = "TU_API_AQUI"

    // Monedas soportadas
    val availableCurrencies = listOf(
        Currency("USD", "US Dollar", "$"),
        Currency("EUR", "Euro", "€"),
        Currency("RUB", "Russian Ruble", "₽"),
        Currency("MXN", "Mexican Peso", "$"),
        Currency("GBP", "British Pound", "£"),
        Currency("JPY", "Japanese Yen", "¥"),
        Currency("CNY", "Chinese Yuan", "¥")
    )

    // Guardar moneda seleccionada
    fun saveSelectedCurrency(currencyCode: String) {
        sharedPreferences.edit().putString("selected_currency", currencyCode).apply()
    }

    // Obtener moneda seleccionada (por defecto USD)
    fun getSelectedCurrency(): String {
        return sharedPreferences.getString("selected_currency", "USD") ?: "USD"
    }

    // Obtener símbolo de la moneda seleccionada
    fun getCurrencySymbol(): String {
        val code = getSelectedCurrency()
        return availableCurrencies.find { it.code == code }?.symbol ?: "$"
    }

    // Guardar tasas de cambio en cache
    fun saveCachedRates(rates: Map<String, Double>) {
        val editor = sharedPreferences.edit()
        rates.forEach { (currency, rate) ->
            editor.putFloat("rate_$currency", rate.toFloat())
        }
        editor.putLong("rates_timestamp", System.currentTimeMillis())
        editor.apply()
    }

    // Obtener tasa de cambio desde cache
    fun getCachedRate(currencyCode: String): Double {
        return sharedPreferences.getFloat("rate_$currencyCode", 1.0f).toDouble()
    }

    // Verificar si las tasas están actualizadas (menos de 24 horas)
    fun areCachedRatesValid(): Boolean {
        val timestamp = sharedPreferences.getLong("rates_timestamp", 0)
        val hoursSinceUpdate = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60)
        return hoursSinceUpdate < 24
    }

    // Convertir precio
    fun convertPrice(priceInUSD: Double, targetCurrency: String = getSelectedCurrency()): Double {
        val rate = getCachedRate(targetCurrency)
        return priceInUSD * rate
    }

    // Formatear precio con símbolo
    fun formatPrice(priceInUSD: Double): String {
        val currency = getSelectedCurrency()
        val convertedPrice = convertPrice(priceInUSD, currency)
        val symbol = getCurrencySymbol()
        return "$symbol${"%.2f".format(convertedPrice)}"
    }

    // Descargar tasas de cambio
    suspend fun updateExchangeRates(context: Context): Result<Boolean> {
        return try {
            // 1. Verificar conectividad
            if (!NetworkUtils.isNetworkAvailable(context)) {
                Log.e("CURRENCY", "❌ No hay conexión a internet")
                return Result.failure(Exception("No hay conexión a internet"))
            }

            // 2. Validar API Key
            if (API_KEY.isEmpty() || API_KEY == "TU_API_KEY_AQUI") {
                Log.e("CURRENCY", "❌ API Key no configurada")
                return Result.failure(Exception("API Key no configurada"))
            }

            Log.d("CURRENCY", "🔄 Iniciando descarga de tasas...")
            Log.d("CURRENCY", "📡 URL: https://v6.exchangerate-api.com/v6/$API_KEY/latest/USD")

            // 3. Realizar llamada a la API
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.exchangeRateService.getExchangeRates(
                    apiKey = API_KEY,
                    baseCurrency = "USD"
                )
            }

            // 4. Validar respuesta
            if (response.result == "success") {
                val rates = response.conversionRates

                if (rates.isEmpty()) {
                    Log.e("CURRENCY", "❌ Respuesta vacía de la API")
                    return Result.failure(Exception("Respuesta vacía"))
                }

                // 5. Guardar tasas en cache
                saveCachedRates(rates)

                Log.d("CURRENCY", "✅ Tasas actualizadas exitosamente")
                Log.d("CURRENCY", "📊 Total de monedas: ${rates.size}")
                Log.d("CURRENCY", "💱 EUR: ${rates["EUR"]}, RUB: ${rates["RUB"]}, MXN: ${rates["MXN"]}")

                Result.success(true)
            } else {
                Log.e("CURRENCY", "❌ Error en respuesta API: ${response.result}")
                Result.failure(Exception("API returned: ${response.result}"))
            }

        } catch (e: Exception) {
            Log.e("CURRENCY", "❌ Excepción al actualizar tasas", e)
            Log.e("CURRENCY", "❌ Tipo de error: ${e.javaClass.simpleName}")
            Log.e("CURRENCY", "❌ Mensaje: ${e.message}")
            Log.e("CURRENCY", "❌ Stack trace: ${e.stackTraceToString()}")

            Result.failure(e)
        }
    }


    fun getRatesTimestamp(): Long {
        return sharedPreferences.getLong("rates_timestamp", 0)
    }


}
