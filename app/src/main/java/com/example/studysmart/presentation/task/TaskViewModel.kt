package com.example.studysmart.ui.presentation.task

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysmart.domain.model.Task
import com.example.studysmart.domain.repository.SessionRepository
import com.example.studysmart.domain.repository.SubjectRepository
import com.example.studysmart.domain.repository.TaskRepository
import com.example.studysmart.util.Priority
import com.example.studysmart.util.SnackbarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject


@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskIdNavArgs = savedStateHandle.get<Int>("taskId")
    private val subjectIdNavArgs = savedStateHandle.get<Int>("subjectId")

    private val _state = MutableStateFlow(TaskState())
    val state = combine(
        _state,
        subjectRepository.getAllSubjects()

    ) { state, subjects ->
        state.copy(subjects = subjects)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskState()
    )

    private val _snackbarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()

     init {
         fetchTask()
         fetchSubject()
     }

    fun onEvent(event: TaskEvent) {
        when (event) {
            TaskEvent.DeleteTask -> deleteTask()
            is TaskEvent.OnDateChange -> {
                _state.update {
                    it.copy(
                        dueDate = event.millis
                    )
                }
            }

            is TaskEvent.OnDescriptionChange -> {
                _state.update {
                    it.copy(
                        description = event.description
                    )
                }
            }

            TaskEvent.OnIsCompleteChange -> {
                _state.update {
                    it.copy(
                        isTaskCompleted = !_state.value.isTaskCompleted
                    )
                }
            }

            is TaskEvent.OnPriorityChange -> {
                _state.update {
                    it.copy(
                        priority = event.priority
                    )
                }
            }

            is TaskEvent.OnRelatedSubjectSelect -> {
                _state.update {
                    it.copy(
                        relatedToSubject = event.subject.name,
                        subjectId = event.subject.subjectId
                    )
                }
            }

            is TaskEvent.OnTitleChange -> {
                _state.update {
                    it.copy(
                        title = event.title
                    )
                }
            }

            TaskEvent.SaveTask -> saveTask()
        }
    }

    private fun deleteTask() {
        viewModelScope.launch {
            viewModelScope.launch {
                try {

                    val currentTaskId = state.value.currentTaskId
                    if (currentTaskId !=null){
                        withContext(Dispatchers.IO){
                            taskRepository.deleteTask(taskId = currentTaskId)
                        }

                        _snackbarEventFlow.emit(SnackbarEvent.NavigateUp)
                        _snackbarEventFlow.emit(
                            SnackbarEvent.ShowSnackbar("Task deleted successfully")
                        )

                    } else {
                        _snackbarEventFlow.emit(
                            SnackbarEvent.ShowSnackbar("Task not found")
                        )
                    }
                }
                catch (e:Exception) {
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackbar(
                            "Couldn't delete Task${e.message}", SnackbarDuration.Long)
                    )
                }

            }
        }
    }

    private fun saveTask() {
        viewModelScope.launch {

            val state = state.value
            if (state.subjectId == null || state.relatedToSubject == null) {
                _snackbarEventFlow.emit(SnackbarEvent.ShowSnackbar(message = "Please select subject related to the task"))
                return@launch
            }
            try {
                taskRepository.upsertTask(
                    task = Task(
                        title = state.title,
                        description = state.description,
                        dueDate = state.dueDate ?: Instant.now().toEpochMilli(),
                        relatedToSubject = state.relatedToSubject,
                        priority = state.priority.value,
                        isComplete = state.isTaskCompleted,
                        taskSubjectId = state.subjectId,
                        taskId = state.currentTaskId
                    )
                )
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "Task Saved Successfully",
                        duration = SnackbarDuration.Short
                    )
                )

                _snackbarEventFlow.emit(SnackbarEvent.NavigateUp)
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "Couldn't save task.${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )

            }

        }
    }

    private fun fetchTask(){
        viewModelScope.launch {
            taskIdNavArgs?.let {
                taskRepository.getTaskById(taskId = it)?.let {task->
                    _state.update {
                        it.copy(
                            title = task.title,
                            description = task.description,
                            dueDate = task.dueDate,
                            relatedToSubject = task.relatedToSubject,
                            priority = Priority.fromInt(task.priority),
                            isTaskCompleted = task.isComplete,
                            subjectId = task.taskSubjectId,
                            currentTaskId = task.taskId
                        )
                    }

                }
            }
        }
    }

    private fun fetchSubject(){
        viewModelScope.launch {
            subjectIdNavArgs?.let {id->
                subjectRepository.getSubjectById(subjectId = id)?.let {subject->
                    _state.update {
                        it.copy(
                            relatedToSubject = subject.name,
                            subjectId = subject.subjectId
                        )
                    }
                }
            }
        }
    }


}