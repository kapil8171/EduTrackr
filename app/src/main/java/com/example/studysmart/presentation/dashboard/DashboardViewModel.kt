package com.example.studysmart.ui.presentation.dashboard

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysmart.domain.model.Session
import com.example.studysmart.domain.model.Subject
import com.example.studysmart.domain.model.Task
import com.example.studysmart.domain.repository.SessionRepository
import com.example.studysmart.domain.repository.SubjectRepository
import com.example.studysmart.domain.repository.TaskRepository
import com.example.studysmart.util.SnackbarEvent
import com.example.studysmart.util.toHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val sessionRepository: SessionRepository,
    private val taskRepository: TaskRepository
    ) : ViewModel() {


            private val _state = MutableStateFlow(DashboardState())

           val state = combine(
               _state,
               subjectRepository.getTotalSubjectCount(),
               subjectRepository.getTotalGoalHours(),
               subjectRepository.getAllSubjects(),
               sessionRepository.getTotalSessionsDuration()
           ){
               state,subjectCount,totalGoalHours,allSubjects,totaSessionsDuration ->
                  state.copy(
                      totalSubjectCount = subjectCount,
                      totalGoalStudyHours = totalGoalHours,
                      subjects = allSubjects,
                      totalStudiedHours = totaSessionsDuration.toHours()
                  )
           }.stateIn(
               scope = viewModelScope,
               started = SharingStarted.WhileSubscribed(5000),
               initialValue = DashboardState()
           )

         val tasks : StateFlow<List<Task>> = taskRepository.getAllUpcomingTasks()
             .stateIn(
                 scope = viewModelScope,
                 started = SharingStarted.WhileSubscribed(5000),
                 initialValue = emptyList()
             )

         val recentSessions : StateFlow<List<Session>> = sessionRepository.getRecentFiveSessions()
             .stateIn(
                 scope = viewModelScope,
                 started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                 initialValue = emptyList()
             )

         fun onEvent(event: DashboardEvent) {
             when (event) {
                 is DashboardEvent.OnSubjectNameChange -> {
                     _state.update {
                         it.copy(subjectName = event.name)
                     }
                 }

                 is DashboardEvent.OnDeleteSessionButtonClick -> {
                     _state.update {
                         it.copy(session = event.session)
                     }
                 }
                 is DashboardEvent.OnGoalStudyHoursChange -> {
                     _state.update {
                         it.copy(goalStudyHours = event.hours)
                     }
                 }
                 is DashboardEvent.OnSubjectCardColorChange -> {
                     _state.update {
                         it.copy(subjectCardColors = event.colors)
                     }
                 }
                 DashboardEvent.DeleteSession -> deleteSession()
                 DashboardEvent.SaveSubject -> saveSubject()
                 is DashboardEvent.OnTaskIsCompletedChange -> {
                     updateTask(event.task)

                 }
             }
         }

    private fun updateTask(task: Task) {
        viewModelScope.launch {

            try {

                taskRepository.upsertTask(
                    task = task.copy(
                        isComplete = !task.isComplete
                    )
                )

                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        "Saved in completed tasks.")
                )
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        "Couldn't update task${e.message}",
                        SnackbarDuration.Long
                    )
                )
            }

        }

    }

    private val _snackbarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()

    private fun saveSubject() {
        viewModelScope.launch {

            try {
                subjectRepository.upsertSubject(
                    subject = Subject(
                        name = state.value.subjectName,
                        goalHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f,
                        color = state.value.subjectCardColors.map { it.toArgb() }
                    )
                )
                _state.update {
                    it.copy(
                        subjectName = "",
                        goalStudyHours = "",
                        subjectCardColors = Subject.subjectCardColors.random()
                    )
                }

                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        "Subject saved successfully")
                )
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        "Couldn't save subject${e.message}",
                        SnackbarDuration.Long
                    )
                )
            }

        }
    }

    private fun deleteSession(){
        viewModelScope.launch {
            try {

                state.value.session?.let {
                    sessionRepository.deleteSession(session = it)
                }

                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar("Session Delete Successfully", SnackbarDuration.Long)
                )

            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        "Couldn't delete session${e.message}",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }
}