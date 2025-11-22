package com.example.insanecrossmobilepingpongapp.util

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat

actual fun formatFloat(value: Float, decimals: Int): String {
    return NSString.stringWithFormat("%+.${decimals}f", value)
}
