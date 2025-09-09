package com.example.studysmart.ui.presentation.subject

import androidx.compose.ui.graphics.Color
import com.example.studysmart.domain.model.Session
import com.example.studysmart.domain.model.Subject
import com.example.studysmart.domain.model.Task


data class SubjectState(

    val currentSubjectId:Int? = null,
    val subjectName:String = "",
    val goalStudyHours:String = "",
    val studiedHours:Float = 0f,
    val subjectCardColors:List<Color> = Subject.subjectCardColors.random(),
    val recentSessions:List<Session> = emptyList(),
    val upcomingTasks:List<Task> = emptyList(),
    val completedTasks:List<Task> = emptyList(),
    val progress:Float = 0f,
    val session: Session? =null
)
