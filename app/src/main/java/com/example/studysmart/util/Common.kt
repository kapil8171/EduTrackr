package com.example.studysmart.util

import androidx.compose.ui.graphics.Color
import com.example.studysmart.ui.presentation.theme.Green
import com.example.studysmart.ui.presentation.theme.Orange
import com.example.studysmart.ui.presentation.theme.Red


enum class Priority(val title: String, val color: Color, val value: Int) {
    LOW("Low", color = Green, 0),
    MEDIUM("Medium", color = Orange, 1),
    HIGH("High", color = Red, 2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: MEDIUM
    }
}