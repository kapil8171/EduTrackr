package com.example.studysmart.ui.presentation.task

import com.example.studysmart.domain.model.Subject
import com.example.studysmart.util.Priority


data class TaskState(
    val title: String = "",
    val description: String = "",
    val dueDate:Long? =null,
    val isTaskCompleted: Boolean = false,
    val priority: Priority = Priority.LOW,
    val relatedToSubject: String? = null,
    val subjects:List<Subject> = emptyList(),
    val subjectId: Int? = null,
    val currentTaskId: Int? = null
)
