package com.litekite.ime.config

import android.content.Context
import android.content.res.Configuration
import com.litekite.ime.base.CallbackProvider
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigController @Inject constructor(context: Context) :
    CallbackProvider<ConfigController.Callback> {

    companion object {
        /** Constant from [android.content.pm.ActivityInfo] to detect overlay change */
        private const val CONFIG_ASSETS_PATHS = -0x80000000
    }

    private var lastConfig: Configuration = context.resources.configuration
    private var uiMode: Int
    private var orientation: Int

    override val callbacks: ArrayList<Callback> = ArrayList()

    init {
        uiMode = lastConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        orientation = lastConfig.orientation
    }

    fun onConfigChanged(newConfig: Configuration) {
        // Configuration change
        callbacks.forEach { it.onConfigChanged(newConfig) }
        // Density or font scale change
        if (lastConfig.densityDpi != newConfig.densityDpi ||
            lastConfig.fontScale != newConfig.fontScale
        ) {
            callbacks.forEach { it.onDensityOrFontScaleChanged() }
        }
        // Locale change
        if (lastConfig.locales != newConfig.locales) {
            callbacks.forEach { it.onLocaleChanged() }
        }
        // Theme change
        val newUiMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (uiMode != newUiMode) {
            callbacks.forEach { it.onThemeChanged() }
            uiMode = newUiMode
        }
        // Device orientation change
        if (orientation != newConfig.orientation) {
            callbacks.forEach { it.onDeviceOrientationChanged() }
            orientation = newConfig.orientation
        }
        // Overlay change
        if ((lastConfig.updateFrom(newConfig) and CONFIG_ASSETS_PATHS) != 0) {
            callbacks.forEach { it.onOverlayChanged() }
        }
    }

    interface Callback {

        fun onConfigChanged(newConfig: Configuration) {}

        fun onDensityOrFontScaleChanged() {}

        fun onLocaleChanged() {}

        fun onThemeChanged() {}

        fun onOverlayChanged() {}

        fun onDeviceOrientationChanged() {}
    }
}
