package io.github.lumyuan.turingbox.ui.theme

import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.TuringBox

enum class AppTheme {
    DEFAULT, DYNAMIC_COLOR, GREEN, RED, PINK, BLUE, CYAN, ORANGE, PURPLE, BROWN, GRAY;

    fun getRealName() =
        when (this) {
            DEFAULT -> TuringBox.application.getString(R.string.theme_default)
            DYNAMIC_COLOR -> TuringBox.application.getString(R.string.theme_dynamic_color)
            GREEN -> TuringBox.application.getString(R.string.theme_green)
            RED -> TuringBox.application.getString(R.string.theme_red)
            PINK -> TuringBox.application.getString(R.string.theme_pink)
            BLUE -> TuringBox.application.getString(R.string.theme_blue)
            CYAN -> TuringBox.application.getString(R.string.theme_cyan)
            ORANGE -> TuringBox.application.getString(R.string.theme_orange)
            PURPLE -> TuringBox.application.getString(R.string.theme_purple)
            BROWN -> TuringBox.application.getString(R.string.theme_brown)
            GRAY -> TuringBox.application.getString(R.string.theme_gray)
        }
}