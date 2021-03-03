package com.project.proabt.utils

import android.graphics.Rect
import android.view.View

class KeyboardVisibilityUtil(contentView: View, onKeyboardShown: (Boolean) -> Unit) {

    private var currentKeyboardState: Boolean = false

    val visibilityListener = {
        val rectangle = Rect()
        contentView.getWindowVisibleDisplayFrame(rectangle)
        val screenHeight = contentView.rootView.height

        val keypadHeight = screenHeight.minus(rectangle.bottom)
        val isKeyboardNowVisible = keypadHeight > screenHeight * 0.15

        if (currentKeyboardState != isKeyboardNowVisible) {
            if (isKeyboardNowVisible) {
                onKeyboardShown(false)
            } else {
                onKeyboardShown(true)
            }
        }
        currentKeyboardState = isKeyboardNowVisible
    }
}
