package com.moondark.realfoodapp

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class RealFoodApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Aquí irá tu API Key de Yandex
        MapKitFactory.setApiKey("TU_API_VA_AQUI")

        // Opcional: establecer idioma español
        MapKitFactory.setLocale("es_ES")
    }
}
