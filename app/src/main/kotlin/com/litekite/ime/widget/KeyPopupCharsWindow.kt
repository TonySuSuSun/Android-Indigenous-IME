package com.litekite.ime.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import com.google.android.material.color.MaterialColors
import com.litekite.ime.R
import com.litekite.ime.base.CallbackProvider
import com.litekite.ime.databinding.WidgetKeyPopupCharBinding
import androidx.core.graphics.drawable.toDrawable

class KeyPopupCharsWindow @JvmOverloads constructor(
    private val context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PopupWindow(context, attrs, defStyleAttr),
    CallbackProvider<KeyPopupCharsWindow.KeyPopupCharListener> {

    companion object {
        private const val POPUP_CHARS_DISMISS_DELAY = 70L
    }

    override val callbacks: ArrayList<KeyPopupCharListener> = ArrayList()

    init {
        contentView = LinearLayoutCompat(context)
        contentView.background = ContextCompat.getDrawable(
            context,
            R.drawable.bg_keyboard_key
        )
        contentView.background.setTint(
            MaterialColors.getColor(contentView, android.R.attr.colorControlHighlight)
        )
        elevation = 10.0F
        animationStyle = android.R.style.Animation_Dialog
        setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }

    fun showPopupChars(parent: View, key: Keyboard.Key) {
        val popupChars = key.popupKeyboardChars
        if (popupChars.isEmpty()) {
            // Key popup chars is disabled if there is no alternate popup chars
            return
        }
        // Clearing existing views
        (contentView as ViewGroup).removeAllViews()
        var keyLabel = ""
        // Creating and adding popup characters
        for (charIndex in popupChars.indices) {
            if (popupChars[charIndex] == '|') {
                val keyBinding = WidgetKeyPopupCharBinding.inflate(LayoutInflater.from(context))
                keyBinding.tvKeyPopupChar.text = keyLabel
                keyLabel = ""
                keyBinding.tvKeyPopupChar.textSize =
                    (context.resources.getDimensionPixelSize(R.dimen.keyboard_view_key_text_size) * 0.5).toFloat()
                // Adding popup character
                (contentView as ViewGroup).addView(keyBinding.root)
                // Width & height of the popup window
                keyBinding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    width = key.width
                    height = key.height
                    if (charIndex != popupChars.lastIndex) {
                        marginEnd = key.verticalGap
                    }
                }
                // Listening for click events
                keyBinding.tvKeyPopupChar.setOnClickListener {
                    dismiss()
                    for (index in keyBinding.tvKeyPopupChar.text.indices) {
                        callbacks.forEach { it.onKey(keyBinding.tvKeyPopupChar.text[index].code) }
                    }
                }
            } else {
                keyLabel += key.adjustPopupCharCase(popupChars[charIndex])
            }
        }
        // Padding for the popup container
        contentView.setPadding(key.verticalGap)
        // Horizontal & vertical padding of the container
        val containerHPadding = ((parent.parent as ViewGroup).width - parent.width) / 2
        val containerVPadding = ((parent.parent as ViewGroup).height - parent.height) / 2
        // Showing popup relative to the key x & y position
        showAtLocation(
            parent,
            Gravity.NO_GRAVITY,
            key.x + parent.paddingLeft + containerHPadding,
            key.y + parent.paddingTop + containerVPadding - key.height - (key.verticalGap * 2)
        )
        // Transparent background for the popup decor view
        (contentView.parent.parent as ViewGroup).setBackgroundColor(Color.TRANSPARENT)
    }

    fun hidePopupChars() {
        if (isShowing) {
            contentView.postDelayed({ dismiss() }, POPUP_CHARS_DISMISS_DELAY)
        }
    }

    /**
     * Listener for keyboard popup character events.
     */
    fun interface KeyPopupCharListener {

        /**
         * Send a key press to the listener.
         * @param primaryCode this is the key that was pressed
         */
        fun onKey(primaryCode: Int)
    }
}
