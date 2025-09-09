package com.example.studysmart.ui.presentation.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.ui.presentation.components.DeleteDialog
import com.example.studysmart.ui.presentation.components.SubjectListBottomSheet
import com.example.studysmart.ui.presentation.components.TaskCheckBox
import com.example.studysmart.ui.presentation.components.TaskDatePicker
import com.example.studysmart.util.CurrentOrFutureSelectedDates
import com.example.studysmart.util.Priority
import com.example.studysmart.util.SnackbarEvent
import com.example.studysmart.util.changeMillisToDateString
import kotlinx.coroutines.launch

data class TaskScreenNavArgs(
    val taskId:Int?,
    val subjectId:Int?

)





@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun TaskScreen(
    taskId: Int?,
    subjectId: Int?,
    onBackButtonClick: () -> Unit,
    viewModel: TaskViewModel
){

         val state by viewModel.state.collectAsStateWithLifecycle()
          val onEvent = viewModel::onEvent


    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.snackbarEventFlow.collect { event ->
            when (event) {
                is SnackbarEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = event.duration
                    )
                }

                SnackbarEvent.NavigateUp -> { onBackButtonClick()

                }
            }
        }
    }


     var isDeleteDialogOpen by rememberSaveable { mutableStateOf(false) }
     var isDatePickerDialogOpen by rememberSaveable { mutableStateOf(false) }



      val datePickerState = rememberDatePickerState(
          initialSelectedDateMillis = System.currentTimeMillis(),
          selectableDates = CurrentOrFutureSelectedDates
      )


     val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var isBottomSheetOpen by remember { mutableStateOf(false) }


    var taskTitleError by rememberSaveable { mutableStateOf<String?>(null) }

    taskTitleError = when{
        state.title.isEmpty() -> "Title cannot be empty"
        state.title.length<3 -> "Task title is too short"
        state.title.length>30 -> "Task title is too long"
        else -> null
    }

    DeleteDialog(
        isOpen = isDeleteDialogOpen,
        title = "Delete Task?",
        bodyText = "Are you sure you want to delete this task? This action cannot be undone.",
        onDismissRequest = {isDeleteDialogOpen =false},
        onConfirmButtonClick = {
            onEvent(TaskEvent.DeleteTask)
            isDeleteDialogOpen =false}
    )

    TaskDatePicker(
        state = datePickerState,
        isOpen = isDatePickerDialogOpen,
        onDismissRequest = {isDatePickerDialogOpen = false},
        onConfirmButtonClicked = {
            onEvent(TaskEvent.OnDateChange(millis = datePickerState.selectedDateMillis))
            isDatePickerDialogOpen =false}
    )

    SubjectListBottomSheet(
        sheetState = sheetState,
        isOpen = isBottomSheetOpen,
        subjects = state.subjects,
        onDismissRequest = { isBottomSheetOpen = false },
        onSubjectClicked = {subject->
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) isBottomSheetOpen = false
            }
            onEvent(TaskEvent.OnRelatedSubjectSelect(subject))

        }

    )

          Scaffold (
              snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
              topBar = {
                  TaskScreenTopBar(
                      isTaskExists = state.currentTaskId !=null,
                      isComplete = state.isTaskCompleted,
                      checkBoxBorderColor = state.priority.color,
                      onBackButtonClick = onBackButtonClick,
                      onDeleteButtonClick = {isDeleteDialogOpen = true},
                      onCheckBoxClick = {
                          onEvent(TaskEvent.OnIsCompleteChange)
                      }
                  )

              }
          ){ paddingValue ->

              Column(
                  modifier = Modifier
                      .verticalScroll(state = rememberScrollState())
                      .fillMaxSize()
                      .padding(paddingValue)
                      .padding(horizontal = 12.dp)

              ) {

                   OutlinedTextField(
                       modifier = Modifier.fillMaxWidth(),
                       value = state.title,
                       onValueChange = {onEvent(TaskEvent.OnTitleChange(title = it))},
                       label = {Text(text = "Title")},
                       singleLine = true,
                       isError = taskTitleError != null && state.title.isNotBlank(),
                       supportingText = { Text(text = taskTitleError.orEmpty()) }
                   )
                  Spacer(modifier = Modifier.height(10.dp))

                  OutlinedTextField(
                      modifier = Modifier.fillMaxWidth(),
                      value = state.description,
                      onValueChange = {onEvent(TaskEvent.OnDescriptionChange(description = it))},
                      label = {Text(text = "Description")}
                  )
                  Spacer(modifier = Modifier.height(20.dp))
                  Text(
                      text = "Due Date",
                      style = MaterialTheme.typography.bodySmall
                  )
                  Row (
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                  ){
                      Text(
                          text = state.dueDate.changeMillisToDateString(),
                          style = MaterialTheme.typography.bodyLarge
                      )
                      IconButton(onClick = {isDatePickerDialogOpen =true}) {
                          Icon(
                              imageVector = Icons.Default.DateRange,
                              contentDescription = "Select Due Date"
                          )
                      }

                  }
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                      text = "Priority",
                      style = MaterialTheme.typography.bodySmall
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Row (modifier = Modifier.fillMaxWidth()){
                      Priority.entries.forEach {

                          PriorityButton(
                              modifier = Modifier.weight(1f),
                              label = it.title,
                              backgroundColor = it.color,
                               borderColor = if (it==state.priority) {
                                   Color.White
                               } else Color.Transparent,
                              labelColor = if (it==state.priority) {
                                  Color.White
                              } else Color.White.copy(alpha = 0.7f),
                              onClick = {onEvent(TaskEvent.OnPriorityChange(priority = it))}
                          )
                      }

                  }
                  Spacer(modifier = Modifier.height(30.dp))

                  Text(
                      text = "Related to subject",
                      style = MaterialTheme.typography.bodySmall
                  )
                  Row (
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                  ){
                      val firstSubject =state.subjects.firstOrNull()?.name ?: ""
                      Text(
                          text = state.relatedToSubject ?: firstSubject,
                          style = MaterialTheme.typography.bodyLarge
                      )
                      IconButton(onClick = {isBottomSheetOpen =true}) {
                          Icon(
                              imageVector = Icons.Default.ArrowDropDown,
                              contentDescription = "Select Subject"
                          )
                      }

                  }
                  Button(
                      enabled = taskTitleError == null,
                      onClick = {onEvent(TaskEvent.SaveTask)},
                      modifier = Modifier
                          .fillMaxWidth()
                          .padding(vertical = 12.dp)

                  ) {
                      Text(text = "Save ")

                  }

              }
          }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskScreenTopBar(
    isTaskExists: Boolean,
    isComplete: Boolean,
    checkBoxBorderColor: Color,
    onBackButtonClick: () -> Unit,
    onDeleteButtonClick: () -> Unit,
    onCheckBoxClick: () -> Unit
    ){

        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onBackButtonClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "navigation Back"
                    )
                }
            },
            title = {
                Text(
                    text = "Task",
                    style = MaterialTheme.typography.headlineSmall
                ) },
            actions = {
                if(isTaskExists){
                    TaskCheckBox(
                        isComplete = isComplete,
                        borderColor = checkBoxBorderColor,
                        onCheckBoxClick = onCheckBoxClick
                    )
                    IconButton(onClick = onDeleteButtonClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Task"
                        )
                    }
                }

            }

        )
}

@Composable
private fun PriorityButton(
    modifier: Modifier = Modifier,
    label: String,
    backgroundColor: Color,
    borderColor: Color,
    labelColor: Color,
    onClick: () -> Unit
){
       Box(
           modifier = modifier
               .background(backgroundColor)
               .clickable { onClick() }
               .padding(5.dp)
               .border(1.dp, borderColor, RoundedCornerShape(5.dp))
                .padding(5.dp),
           contentAlignment = Alignment.Center
       ){
           Text(text = label, color = labelColor)
    }

}