package com.litekite.ime.app

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import com.litekite.ime.config.ConfigController
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ImeApp : Application() {

    companion object {

        val TAG: String = ImeApp::class.java.simpleName

        /**
         * Logs messages for Debugging Purposes.
         *
         * @param tag     TAG is a class name in which the log come from.
         * @param message Type of Log Message.
         */
        fun printLog(tag: String, message: String) {
            if (Log.isLoggable(tag, Log.DEBUG)) {
                Log.d(tag, message)
            }
        }
    }

    @Inject
    lateinit var configController: ConfigController

    override fun onCreate() {
        super.onCreate()
        printLog(TAG, "onCreate:")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        configController.onConfigChanged(newConfig)
        super.onConfigurationChanged(newConfig)
    }

    override fun onTerminate() {
        super.onTerminate()
        printLog(TAG, "onTerminate:")
    }
}
