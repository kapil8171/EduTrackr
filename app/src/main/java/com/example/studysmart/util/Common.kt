package com.example.studysmart.util

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import com.example.studysmart.ui.presentation.theme.Green
import com.example.studysmart.ui.presentation.theme.Orange
import com.example.studysmart.ui.presentation.theme.Red
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


enum class Priority(val title: String, val color: Color, val value: Int) {
    LOW("Low", color = Green, 0),
    MEDIUM("Medium", color = Orange, 1),
    HIGH("High", color = Red, 2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: MEDIUM
    }
}

fun Long?.changeMillisToDateString(): String {
    val date:LocalDate = this?.let {
        Instant
            .ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
             .toLocalDate()
    } ?:LocalDate.now()
    return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}

@OptIn(ExperimentalMaterial3Api::class)
object CurrentOrFutureSelectedDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val currentDateMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return utcTimeMillis>=currentDateMillis
    }

    override fun isSelectableYear(year: Int): Boolean {
        return year >= LocalDate.now().year

    }

}


fun Long.toHours():Float{
     val hours = this.toFloat() /3600f
    return "%.2f".format(hours).toFloat()
}

sealed class SnackbarEvent {

      data class ShowSnackbar(
          val message: String,
          val duration: SnackbarDuration = SnackbarDuration.Short
      ) : SnackbarEvent()

    data object NavigateUp : SnackbarEvent()

}

fun Int.pad(): String {
     return this.toString().padStart(length = 2, padChar = '0')
}