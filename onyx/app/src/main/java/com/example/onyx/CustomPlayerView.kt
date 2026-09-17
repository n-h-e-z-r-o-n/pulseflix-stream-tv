package com.example.onyx

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.media3.ui.PlayerView

class CustomPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PlayerView(context, attrs, defStyleAttr) {

    var onDpadDownHidden: ((KeyEvent) -> Boolean)? = null
    var onDpadUpHidden: ((KeyEvent) -> Boolean)? = null
    var onDpadDownVisible: ((KeyEvent) -> Boolean)? = null
    var onDpadUpVisible: ((KeyEvent) -> Boolean)? = null
    var onBackPress: ((KeyEvent) -> Boolean)? = null

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val isControllerVisible = isControllerFullyVisible
        var handled = false
        
        if (event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (isControllerVisible) {
                handled = onDpadDownVisible?.invoke(event) ?: false
            } else {
                handled = onDpadDownHidden?.invoke(event) ?: false
            }
        } else if (event.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (isControllerVisible) {
                handled = onDpadUpVisible?.invoke(event) ?: false
            } else {
                handled = onDpadUpHidden?.invoke(event) ?: false
            }
        } else if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            handled = onBackPress?.invoke(event) ?: false
        }
        
        if (handled) {
            return true
        }
        
        return super.dispatchKeyEvent(event)
    }
}
