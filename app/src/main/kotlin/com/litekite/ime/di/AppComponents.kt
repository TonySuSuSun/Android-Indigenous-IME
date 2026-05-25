package com.litekite.ime.di

import android.content.Context
import com.litekite.ime.audio.AudioController
import com.litekite.ime.config.ConfigController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Vignesh S
 * @version 1.0, 19/07/2021
 * @since 1.0
 */
@Module
@InstallIn(SingletonComponent::class)
object AppComponents {

    @Provides
    @Singleton
    fun provideConfigController(@ApplicationContext context: Context): ConfigController =
        ConfigController(context)

    @Provides
    @Singleton
    fun provideAudioController(@ApplicationContext context: Context): AudioController =
        AudioController(context)
}
