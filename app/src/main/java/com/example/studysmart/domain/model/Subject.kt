package com.example.studysmart.domain.model

import androidx.compose.ui.graphics.Color
import com.example.studysmart.ui.presentation.theme.gradient1
import com.example.studysmart.ui.presentation.theme.gradient2
import com.example.studysmart.ui.presentation.theme.gradient3
import com.example.studysmart.ui.presentation.theme.gradient4
import com.example.studysmart.ui.presentation.theme.gradient5

data class Subject(
    val name: String,
    val goalHours: Float,
    val color: List<Color>,
    val subjectId: Int

    ) {
    companion object{
        val subjectCardColors = listOf(gradient1, gradient2, gradient3, gradient4, gradient5)
    }
}
