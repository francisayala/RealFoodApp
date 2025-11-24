package com.moondark.realfoodapp

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.moondark.realfoodapp.databinding.ActivityTrackingBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.*
import kotlin.math.*

class TrackingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrackingBinding
    private lateinit var mapView: MapView
    private lateinit var sharedPreferences: SharedPreferences

    private var deliveryPlacemark: PlacemarkMapObject? = null
    private var customerPlacemark: PlacemarkMapObject? = null

    private var deliveryLat = 56.0000
    private var deliveryLon = 92.8700
    private var customerLocation: Point? = null

    private var trackingJob: Job? = null
    private var isMapInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.initialize(this)

        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView
        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)

        loadCustomerLocation()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadCustomerLocation() {
        val latitude = getCoordinateFromSharedPref("delivery_latitude")
        val longitude = getCoordinateFromSharedPref("delivery_longitude")

        if (latitude != 0.0 && longitude != 0.0) {
            customerLocation = Point(latitude, longitude)
            deliveryLat = latitude - 0.015
            deliveryLon = longitude - 0.010

            Toast.makeText(this, "📍 Rastreando tu pedido...", Toast.LENGTH_SHORT).show()

            setupMap()
            startDeliveryTracking()
        } else {
            Toast.makeText(
                this,
                "⚠️ No tienes dirección configurada",
                Toast.LENGTH_LONG
            ).show()

            customerLocation = Point(56.0153, 92.8932)
            deliveryLat = 56.0000
            deliveryLon = 92.8700

            setupMap()
            startDeliveryTracking()
        }
    }

    private fun getCoordinateFromSharedPref(key: String): Double {
        return Double.fromBits(sharedPreferences.getLong(key, 0L))
    }

    private fun setupMap() {
        if (customerLocation == null || isMapInitialized) return

        val map = mapView.mapWindow.map

        val centerLat = (deliveryLat + customerLocation!!.latitude) / 2
        val centerLon = (deliveryLon + customerLocation!!.longitude) / 2

        val distance = calculateDistance(
            deliveryLat, deliveryLon,
            customerLocation!!.latitude, customerLocation!!.longitude
        )

        val zoom = when {
            distance < 1.0 -> 15.0f
            distance < 3.0 -> 13.5f
            distance < 5.0 -> 12.5f
            else -> 11.5f
        }

        map.move(
            CameraPosition(Point(centerLat, centerLon), zoom, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )

        // Marcador cliente (verde)
        val customerIcon = createMarkerBitmap(Color.parseColor("#4CAF50"), "🏠")
        customerPlacemark = map.mapObjects.addPlacemark().apply {
            geometry = customerLocation!!
            setIcon(ImageProvider.fromBitmap(customerIcon))
            opacity = 1.0f
        }

        // Marcador delivery (rojo)
        val deliveryIcon = createMarkerBitmap(Color.parseColor("#FF5722"), "🚗")
        deliveryPlacemark = map.mapObjects.addPlacemark().apply {
            geometry = Point(deliveryLat, deliveryLon)
            setIcon(ImageProvider.fromBitmap(deliveryIcon))
            opacity = 1.0f
        }

        map.isZoomGesturesEnabled = true
        map.isScrollGesturesEnabled = true
        isMapInitialized = true
    }

    private fun createMarkerBitmap(markerColor: Int, emoji: String): Bitmap {
        val size = 120
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            isAntiAlias = true
            setColor(markerColor)
            style = Paint.Style.FILL
        }

        canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)

        paint.apply {
            setColor(Color.WHITE)
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)

        paint.apply {
            setColor(Color.WHITE)
            style = Paint.Style.FILL
            textSize = 50f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(emoji, size / 2f, size / 2f + 15f, paint)

        return bitmap
    }

    private fun startDeliveryTracking() {
        if (customerLocation == null) return

        trackingJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                delay(2000)

                // Movimiento suave hacia el cliente
                deliveryLat += (customerLocation!!.latitude - deliveryLat) * 0.08
                deliveryLon += (customerLocation!!.longitude - deliveryLon) * 0.08

                // Actualizar marcador
                deliveryPlacemark?.geometry = Point(deliveryLat, deliveryLon)

                val distance = calculateDistance(
                    deliveryLat, deliveryLon,
                    customerLocation!!.latitude, customerLocation!!.longitude
                )

                val timeMinutes = (distance / 30.0) * 60.0

                binding.statusText.text = when {
                    distance < 0.1 -> "¡Tu pedido ha llegado! 🎉"
                    distance < 0.5 -> """
                        🚗 El repartidor está muy cerca
                        📍 ${String.format("%.0f", distance * 1000)} metros
                    """.trimIndent()
                    else -> """
                        🛵 En camino a tu ubicación
                        📍 Distancia: ${String.format("%.2f", distance)} km
                        ⏱️ Tiempo estimado: ${String.format("%.0f", timeMinutes)} min
                    """.trimIndent()
                }

                if (distance < 0.05) {
                    binding.statusText.text = "¡Pedido entregado con éxito! 🎉🍕"
                    Toast.makeText(
                        this@TrackingActivity,
                        "¡Disfruta tu comida!",
                        Toast.LENGTH_LONG
                    ).show()
                    delay(3000)
                    finish()
                }
            }
        }
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroy() {
        trackingJob?.cancel()
        super.onDestroy()
    }
}
