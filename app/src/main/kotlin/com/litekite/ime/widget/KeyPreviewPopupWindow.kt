package com.litekite.ime.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.google.android.material.color.MaterialColors
import com.litekite.ime.R
import com.litekite.ime.databinding.WidgetKeyPopupPreviewBinding
import com.litekite.ime.util.StringUtil.isPunctuation
import androidx.core.graphics.drawable.toDrawable

class KeyPreviewPopupWindow @JvmOverloads constructor(
    private val context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PopupWindow(context, attrs, defStyleAttr) {

    companion object {
        private const val PREVIEW_POPUP_DISMISS_DELAY = 70L
    }

    private val binding = WidgetKeyPopupPreviewBinding.inflate(LayoutInflater.from(context))

    init {
        contentView = binding.root
        contentView.background.setTint(
            MaterialColors.getColor(contentView, android.R.attr.colorControlHighlight)
        )
        elevation = 10.0F
        isTouchable = false
        animationStyle = android.R.style.Animation_Dialog
        setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }

    fun showPreview(parent: View, key: Keyboard.Key) {
        val keyLabel = key.adjustLabelCase()
        if (keyLabel.isEmpty() || keyLabel.length > 3) {
            // Key icon or special key code preview is disabled
            return
        }
        // Key preview label
        binding.tvKeyPreview.text = keyLabel
        // For punctuation, use large font. For labels, use small font.
        if (keyLabel.isPunctuation()) {
            binding.tvKeyPreview.textSize = context.resources.getDimensionPixelSize(
                R.dimen.keyboard_view_key_punctuation_text_size
            ).toFloat()
        } else {
            binding.tvKeyPreview.textSize = context.resources.getDimensionPixelSize(
                R.dimen.keyboard_view_key_text_size
            ).toFloat()
        }
        // Width & height of the popup window
        width = key.width
        height = key.height * 2
        // Horizontal & vertical padding of the container
        val containerHPadding = ((parent.parent as ViewGroup).width - parent.width) / 2
        val containerVPadding = ((parent.parent as ViewGroup).height - parent.height) / 2
        // Showing popup relative to the key x & y position
        showAtLocation(
            parent,
            Gravity.NO_GRAVITY,
            key.x + parent.paddingLeft + containerHPadding,
            key.y + parent.paddingTop + containerVPadding - key.height
        )
        // Transparent background for the popup decor view
        (contentView.parent.parent as ViewGroup).setBackgroundColor(Color.TRANSPARENT)
    }

    fun hidePreview() {
        if (isShowing) {
            contentView.postDelayed({ dismiss() }, PREVIEW_POPUP_DISMISS_DELAY)
        }
    }
}
