/*
 * Copyright 2021 LiteKite Startup. All rights reserved.
 */
package com.litekite.ime.config;

import android.content.res.Configuration;
import androidx.annotation.NonNull;

/**
 * Interface for configuration changes.
 */
public interface ConfigCallback {
    default void onConfigChanged(@NonNull Configuration newConfig) {}
    default void onDensityOrFontScaleChanged() {}
    default void onLocaleChanged() {}
    default void onThemeChanged() {}
    default void onOverlayChanged() {}
    default void onDeviceOrientationChanged() {}
}
