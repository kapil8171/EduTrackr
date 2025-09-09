package com.example.studysmart.ui.presentation.subject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.ui.presentation.components.AddSubjectDialog
import com.example.studysmart.ui.presentation.components.CountCard
import com.example.studysmart.ui.presentation.components.DeleteDialog
import com.example.studysmart.ui.presentation.components.studySessionsList
import com.example.studysmart.ui.presentation.components.tasksList
import com.example.studysmart.util.SnackbarEvent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun SubjectScreen(
    subjectId: Int,
    onBackButtonClick: () -> Unit,
    onAddTaskButtonClick: () -> Unit,
    onTaskCardClick: (Int?) -> Unit,
    viewModel: SubjectViewModel
 )
{

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

                SnackbarEvent.NavigateUp -> {
                    onBackButtonClick()
                }
            }
        }
    }


    LaunchedEffect(key1 = state.studiedHours, key2 = state.goalStudyHours) {
        onEvent(SubjectEvent.UpdateProgress)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
     val listState = rememberLazyListState()
     val isFABExpanded by remember {
         derivedStateOf { listState.firstVisibleItemIndex == 0 }
     }

    var isEditSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteSessionDialogOpen by rememberSaveable { mutableStateOf(false) }

    AddSubjectDialog(
        isOpen = isEditSubjectDialogOpen,
        selectedColors = state.subjectCardColors,
        subjectName = state.subjectName,
        goalHours = state.goalStudyHours,
        onColorChange = {onEvent(SubjectEvent.OnSubjectCardColorChange(it))},
        onSubjectNameChange = {onEvent(SubjectEvent.OnSubjectNameChange(name = it))},
        onGoalHoursChange = {onEvent(SubjectEvent.OnGoalStudyHoursChange(hours = it))},
        onDismissRequest = { isEditSubjectDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(SubjectEvent.UpdateSubject)
            isEditSubjectDialogOpen = false
        }
    )

    DeleteDialog(
        isOpen = isDeleteSubjectDialogOpen,
        title = "Delete Subject",
        bodyText = "Are you sure you want to delete this Subject? All related "+
                "tasks and study sessions will be permanently deleted. This action cannot be undone.",
        onDismissRequest = { isDeleteSubjectDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(SubjectEvent.DeleteSubject)
            isDeleteSubjectDialogOpen = false


        }
    )

    DeleteDialog(
        isOpen = isDeleteSessionDialogOpen,
        title = "Delete Session",
        bodyText = "Are you sure you want to delete this session? Your studied hours will be reduced "+
                "by this session time. This action cannot be undone.",
        onDismissRequest = { isDeleteSessionDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(SubjectEvent.DeleteSession)
            isDeleteSessionDialogOpen = false }
    )

     Scaffold(
         modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
         snackbarHost = {
             SnackbarHost(hostState = snackbarHostState)
         },
         topBar = {
             SubjectScreenTopBar(
                 title = state.subjectName,
                 onBackButtonClick = onBackButtonClick,
                 onDeleteButtonClick = {isDeleteSubjectDialogOpen = true},
                 onEditButtonClick = {isEditSubjectDialogOpen = true},
                 scrollBehavior = scrollBehavior
             )
         },
         floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTaskButtonClick,
                icon = {Icon(imageVector = Icons.Default.Add, contentDescription = "Add")},
                text = { Text(text = "Add Task")},
                expanded = isFABExpanded
            )

         }
     ) {
         LazyColumn(
             state = listState,
             modifier = Modifier
                 .fillMaxSize()
                 .padding(it)
         ) {
             item {
                 SubjectOverviewSection(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(12.dp),
                     studiedHours = state.studiedHours.toString() ,
                     goalHours = state.goalStudyHours,
                     progress = state.progress
                 )
             }
             tasksList(
                 sectionTitle = "UPCOMING TASKS",
                 emptyListText = "You don't have any upcoming tasks.\n " +
                         "Click on + button  to add new task.",
                 tasks = state.upcomingTasks,
                 onTaskCardClick = onTaskCardClick,
                 onCheckBoxClick = {onEvent(SubjectEvent.OnTaskIsCompleteChange(it))}
             )
             item {
                 Spacer(modifier = Modifier.height(20.dp))
             }
             tasksList(
                 sectionTitle = "COMPLETED TASKS",
                 emptyListText = "You don't have any completed tasks.\n " +
                         "Click the check box on completion of task.",
                 tasks = state.completedTasks,
                 onTaskCardClick = onTaskCardClick,
                 onCheckBoxClick = {onEvent(SubjectEvent.OnTaskIsCompleteChange(it))}
             )
             item {
                 Spacer(modifier = Modifier.height(20.dp))
             }

             studySessionsList(
                 sectionTitle = "RECENT STUDY SESSIONS",
                 emptyListText = "You don't have any recent study sessions.\n " +"Start a study session to begin  recording your progress.",
                 sessions = state.recentSessions,
                 onDeleteIconClick = {
                     onEvent(SubjectEvent.OnDeleteSessionButtonClick(it))
                     isDeleteSessionDialogOpen = true}
             )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

         }
     }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectScreenTopBar(
    title: String,
    onBackButtonClick: () -> Unit,
    onDeleteButtonClick: () -> Unit,
    onEditButtonClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
){
    LargeTopAppBar(
        scrollBehavior = scrollBehavior ,
        navigationIcon = {
            IconButton(onClick = onBackButtonClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "navigation Back"
                )
            }
        },
        title = { Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineSmall
            ) },

           actions = {
               IconButton(onClick = onDeleteButtonClick) {
                   Icon(
                       imageVector = Icons.Default.Delete,
                       contentDescription = "Delete Subject"
                   )
               }
               IconButton(onClick = onEditButtonClick) {
                   Icon(
                       imageVector = Icons.Default.Edit,
                       contentDescription = "Edit Subject"
                   )
               }
           }
    )
}

@Composable
private fun SubjectOverviewSection(
    modifier: Modifier,
    studiedHours: String,
    goalHours: String,
    progress: Float
) {
          val percentageProgress = remember(progress) {
              (progress*100).toInt().coerceIn(0,100)
          }

         Row (
             modifier =modifier,
             horizontalArrangement = Arrangement.SpaceAround,
             verticalAlignment = Alignment.CenterVertically
         ){
                CountCard(
                    modifier = Modifier.weight(1f),
                    headingText = "Goal Study Hours",
                    count = goalHours
                )
             Spacer(modifier = Modifier.width(10.dp))
                CountCard(
                    modifier = Modifier.weight(1f),
                    headingText = " Studied Hours",
                    count = studiedHours
                )
             Spacer(modifier = Modifier.width(10.dp))
              Box(
                  modifier = Modifier.size(75.dp),
                  contentAlignment = Alignment.Center
              ){
                  CircularProgressIndicator(
                      modifier = Modifier.fillMaxSize(),
                      progress = 1f,
                      strokeWidth = 4.dp,
                      strokeCap = StrokeCap.Round,
                      color = MaterialTheme.colorScheme.surfaceVariant
                  )
                  CircularProgressIndicator(
                      modifier = Modifier.fillMaxSize(),
                      progress = progress,
                      strokeWidth = 4.dp,
                      strokeCap = StrokeCap.Round
                  )
                  Text(text = "$percentageProgress%")

              }
         }
}
