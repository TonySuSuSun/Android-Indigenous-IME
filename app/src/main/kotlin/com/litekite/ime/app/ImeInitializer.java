/*
 * Copyright 2021 LiteKite Startup. All rights reserved.
 */
package com.litekite.ime.app;

import android.content.res.Configuration;
import androidx.annotation.NonNull;
import com.litekite.ime.config.ConfigController;

/**
 * Initializer for the IME.
 */
public class ImeInitializer {

    private final ConfigController configController;

    public ImeInitializer(ConfigController configController) {
        this.configController = configController;
    }

    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        if (configController != null) {
            configController.onConfigChanged(newConfig);
        }
    }
}
