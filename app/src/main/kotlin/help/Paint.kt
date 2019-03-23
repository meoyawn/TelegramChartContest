package help

import android.graphics.Paint

var Paint.alphaF
    set(value) {
        alpha = (value * 255).toInt()
    }
    get() = alpha / 255f
