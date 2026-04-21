/*
 * Copyright 2021 LiteKite Startup. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.litekite.ime.service

import android.annotation.SuppressLint
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.os.LocaleList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import com.google.android.material.color.MaterialColors
import com.litekite.ime.R
import com.litekite.ime.app.ImeApp
import com.litekite.ime.audio.AudioController
import com.litekite.ime.config.ConfigController
import com.litekite.ime.databinding.LayoutKeyboardViewBinding
import com.litekite.ime.util.CharUtil.cycleCharacter
import com.litekite.ime.widget.Keyboard
import com.litekite.ime.widget.KeyboardView
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import androidx.core.graphics.drawable.toDrawable
import com.litekite.ime.data.WordDatabase
import com.litekite.ime.data.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * @author Vignesh S
 * @version 1.0, 01/06/2021
 * @since 1.0
 */
@AndroidEntryPoint
class ImeService : InputMethodService(), ConfigController.Callback {

    companion object {

        private val TAG: String = ImeService::class.java.simpleName

        private const val DEFAULT_LOCALE = "en"
        private const val IME_ACTION_CUSTOM_LABEL = EditorInfo.IME_MASK_ACTION + 1
    }

    var currentLanguage = 1
    val languageText = listOf("Amis", "Atayal", "Pinayuanan", "Bunun", "Pinuyumayan", "Drekay", "Cou", "SaiSiyat", "Tao", "Thau", "Kevalan", "Truku", "Sakizaya", "Seediq", "Hlaʼalua", "Kanakanavu")

    @Inject
    lateinit var configController: ConfigController

    @Inject
    lateinit var audioController: AudioController

    private var _editorInfo: EditorInfo? = null
    private val editorInfo: EditorInfo get() = _editorInfo!!

    private lateinit var qwertyKeyboard: Keyboard
    private lateinit var symbolKeyboard: Keyboard

    private var _binding: LayoutKeyboardViewBinding? = null
    private val binding: LayoutKeyboardViewBinding get() = _binding!!

    private lateinit var repository: WordRepository

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        ImeApp.printLog(TAG, "init:")
    }

    override fun onCreate() {
        setTheme(R.style.Theme_AndroidIME)
        super.onCreate()
        ImeApp.printLog(TAG, "onCreate:")
        parseKeyboardLayoutFromXml()
        // Setting configuration callback
        configController.addCallback(this)
        switchLanguage(languageText[0])
    }

    override fun onThemeChanged() {
        super.onThemeChanged()
        ImeApp.printLog(TAG, "onThemeChanged:")
        // Applying theme to resolve attributes of the current theme
        theme.applyStyle(R.style.Theme_AndroidIME, true)
        // Changing nav bar background
        window.window?.navigationBarColor = MaterialColors.getColor(
            binding.vKeyboard,
            android.R.attr.navigationBarColor
        )
        // Recreate all the keyboard layouts to reflect theme change
        parseKeyboardLayoutFromXml()
        binding.vKeyboard.setKeyboard(qwertyKeyboard)
    }

    override fun onDeviceOrientationChanged() {
        super.onDeviceOrientationChanged()
        // Recreate all the keyboard layouts to reflect device orientation change
        parseKeyboardLayoutFromXml()
        binding.vKeyboard.setKeyboard(qwertyKeyboard)
    }

    private fun parseKeyboardLayoutFromXml() {
        qwertyKeyboard = createKeyboard(Keyboard.LAYOUT_KEYBOARD_QWERTY)
        symbolKeyboard = createKeyboard(Keyboard.LAYOUT_KEYBOARD_SYMBOL)
    }

    @SuppressLint("DiscouragedApi")
    private fun createKeyboard(layoutXml: String): Keyboard {
        val overrideConfig = resources.configuration
        // Set default locale
        val localeList = LocaleList(Locale(DEFAULT_LOCALE))
        overrideConfig.setLocales(localeList)
        // Update configuration
        createConfigurationContext(overrideConfig)
        // Keyboard layout
        return Keyboard(
            this,
            resources.getIdentifier(layoutXml, Keyboard.DEF_TYPE, packageName)
        )
    }

    override fun onCreateInputView(): View {
        ImeApp.printLog(TAG, "onCreateInputView:")
        _binding = LayoutKeyboardViewBinding.inflate(LayoutInflater.from(this))
        return binding.root
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        ImeApp.printLog(TAG, "onStartInputView:")
        _editorInfo = info
        binding.vKeyboard.setKeyboard(qwertyKeyboard)
        binding.vKeyboard.addCallback(keyboardActionListener)
        binding.vKeyboard.setShifted(info.initialCapsMode != 0)
        changeLanguageText()
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onDestroy() {
        ImeApp.printLog(TAG, "onDestroy:")
        // Removing callback
        configController.removeCallback(this)
        binding.vKeyboard.removeCallback(keyboardActionListener)
        _binding = null
        super.onDestroy()
        scope.cancel()
        WordDatabase.close()
    }

    private val keyboardActionListener = object : KeyboardView.KeyboardActionListener {

        override fun onKey(primaryCode: Int) {
            ImeApp.printLog(TAG, "onKey: $primaryCode")
            when (primaryCode) {
                Keyboard.KEYCODE_SHIFT -> {
                    // Toggle Capitalization
                    binding.vKeyboard.setShifted(!binding.vKeyboard.isShifted())
                }
                Keyboard.KEYCODE_MODE_CHANGE -> {
                    if (binding.vKeyboard.keyboard == symbolKeyboard) {
                        binding.vKeyboard.setKeyboard(qwertyKeyboard)
                    } else {
                        binding.vKeyboard.setKeyboard(symbolKeyboard)
                    }
                }
                Keyboard.KEYCODE_DONE -> {
                    val action = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
                    currentInputConnection.performEditorAction(action)
                }
                Keyboard.KEYCODE_DELETE -> {
                    currentInputConnection.deleteSurroundingText(1, 0)
                }
                Keyboard.KEYCODE_MAIN_KEYBOARD -> {
                    binding.vKeyboard.setKeyboard(qwertyKeyboard)
                }
                Keyboard.KEYCODE_LANGUAGE_KEYBOARD -> {
                    showLanguagePopup()
                }
                Keyboard.KEYCODE_AUTO_FILL_1 -> {
                    val text = qwertyKeyboard.keys.find { it.codes.contains(primaryCode) }?.label
                    currentInputConnection.commitText(text, 1)
                }
                Keyboard.KEYCODE_AUTO_FILL_2 -> {
                    val text = qwertyKeyboard.keys.find { it.codes.contains(primaryCode) }?.label
                    currentInputConnection.commitText(text, 1)
                }
                Keyboard.KEYCODE_AUTO_FILL_3 -> {
                    val text = qwertyKeyboard.keys.find { it.codes.contains(primaryCode) }?.label
                    currentInputConnection.commitText(text, 1)
                }
                Keyboard.KEYCODE_CYCLE_CHAR -> {
                    val text = currentInputConnection.getTextBeforeCursor(1, 0)
                    if (text.isNullOrEmpty()) {
                        return
                    }
                    val currChar = text[0]
                    val altChar = currChar.cycleCharacter(binding.vKeyboard.getLocale())
                    // Don't modify text if there is no alternate.
                    if (currChar != altChar) {
                        currentInputConnection.deleteSurroundingText(1, 0)
                        currentInputConnection.commitText(altChar.toString(), 1)
                    }
                }
                Keyboard.KEYCODE_ENTER -> {
                    val imeOptionsActionId = getImeOptionsActionId(editorInfo)
                    when {
                        IME_ACTION_CUSTOM_LABEL == imeOptionsActionId -> {
                            // Either we have an actionLabel and we should
                            // performEditorAction with actionId regardless of its value.
                            currentInputConnection.performEditorAction(editorInfo.actionId)
                        }
                        EditorInfo.IME_ACTION_NONE != imeOptionsActionId -> {
                            // We didn't have an actionLabel, but we had another action to execute.
                            // EditorInfo.IME_ACTION_NONE explicitly means no action.
                            // In contrast, EditorInfo.IME_ACTION_UNSPECIFIED is the default value
                            // for an action, so it means there should be an action and
                            // the app didn't bother to set a specific code for it
                            // - presumably it only handles one. It does not have to be treated
                            // in any specific way: anything that is not IME_ACTION_NONE
                            // should be sent to performEditorAction.
                            currentInputConnection.performEditorAction(imeOptionsActionId)
                        }
                        else -> {
                            // No action label, and the action from imeOptions is NONE:
                            // this is a regular enter key that should input a carriage return.
                            commitText(primaryCode)
                        }
                    }
                }
                else -> {
                    commitText(primaryCode)
                }
            }
            audioController.playSoundEffect()
        }

        override fun onStopInput() {
            requestHideSelf(0)
        }
    }

    private fun getImeOptionsActionId(info: EditorInfo): Int {
        return when {
            info.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0 -> {
                EditorInfo.IME_ACTION_NONE
            }
            info.actionLabel != null -> {
                IME_ACTION_CUSTOM_LABEL
            }
            else -> {
                // Note: this is different from editorInfo.actionId, hence "ImeOptionsActionId"
                info.imeOptions and EditorInfo.IME_MASK_ACTION
            }
        }
    }

    private fun commitText(code: Int) {
        var commitText = Char(code).toString()
        // Chars always come through as lowercase, so we have to explicitly
        // uppercase them if the keyboard is shifted.
        if (binding.vKeyboard.isShifted()) {
            commitText = commitText.replaceFirstChar(Char::uppercase)
        }
        ImeApp.printLog(TAG, "commitText: $commitText")
        currentInputConnection.commitText(commitText, 1)
    }

    @SuppressLint("InflateParams")
    private fun showLanguagePopup() {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.language_menu, null)

        updateLanguage(popupView)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val keyboardView = binding.vKeyboard
        val location = IntArray(2)
        keyboardView.getLocationOnScreen(location)

        val key = keyboardView.keyboard?.keys?.find { it.codes.contains(-99) }

        key?.let {
            val x = location[0] + it.x
            val y = location[1] + it.y

            popupView.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )

            popupWindow.showAtLocation(
                keyboardView,
                Gravity.NO_GRAVITY,
                x,
                y - 3440
            )
        }

        val clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.Amis ->  currentLanguage = 1
                R.id.Atayal -> currentLanguage = 2
                R.id.Pinayuanan -> currentLanguage = 3
                R.id.Bunun -> currentLanguage = 4
                R.id.Pinuyumayan -> currentLanguage = 5
                R.id.Drekay -> currentLanguage = 6
                R.id.Cou -> currentLanguage = 7
                R.id.SaiSiyat -> currentLanguage = 8
                R.id.Tao -> currentLanguage = 9
                R.id.Thau -> currentLanguage = 10
                R.id.Kevalan -> currentLanguage = 11
                R.id.Truku -> currentLanguage = 12
                R.id.Sakizaya -> currentLanguage = 13
                R.id.Seediq -> currentLanguage = 14
                R.id.Hlaʼalua -> currentLanguage = 15
                R.id.Kanakanavu -> currentLanguage = 16
            }
            switchLanguage(languageText[currentLanguage - 1])
            updateLanguage(popupView)
            popupWindow.dismiss()
            keyboardView.invalidateAllKeys()
        }

        val ids = listOf(
            R.id.Amis,
            R.id.Atayal,
            R.id.Pinayuanan,
            R.id.Bunun,
            R.id.Pinuyumayan,
            R.id.Drekay,
            R.id.Cou,
            R.id.SaiSiyat,
            R.id.Tao,
            R.id.Thau,
            R.id.Kevalan,
            R.id.Truku,
            R.id.Sakizaya,
            R.id.Seediq,
            R.id.Hlaʼalua,
            R.id.Kanakanavu
        )

        ids.forEach {
            popupView.findViewById<View>(it).setOnClickListener(clickListener)
        }
    }

    fun updateLanguage(view: View) {
        val map = mapOf(
            1 to R.id.Amis,
            2 to R.id.Atayal,
            3 to R.id.Pinayuanan,
            4 to R.id.Bunun,
            5 to R.id.Pinuyumayan,
            6 to R.id.Drekay,
            7 to R.id.Cou,
            8 to R.id.SaiSiyat,
            9 to R.id.Tao,
            10 to R.id.Thau,
            11 to R.id.Kevalan,
            12 to R.id.Truku,
            13 to R.id.Sakizaya,
            14 to R.id.Seediq,
            15 to R.id.Hlaʼalua,
            16 to R.id.Kanakanavu
        )
        map.values.forEach { id ->
            view.findViewById<View>(id).setBackgroundColor(Color.TRANSPARENT)
        }
        map[currentLanguage]?.let { id ->
            view.findViewById<View>(id).setBackgroundColor(Color.GREEN)
        }
        changeLanguageText()
    }

    fun changeLanguageText() {
        val qwertyKey = qwertyKeyboard.keys.find { it.codes.contains(32) }
        val symbolKey = symbolKeyboard.keys.find { it.codes.contains(32) }
        qwertyKey?.label = languageText[currentLanguage - 1]
        symbolKey?.label = languageText[currentLanguage - 1]
    }

    fun switchLanguage(language: String) {
        val dao = WordDatabase.getInstance(this, language).wordDao()
        repository = WordRepository(dao)
    }
}