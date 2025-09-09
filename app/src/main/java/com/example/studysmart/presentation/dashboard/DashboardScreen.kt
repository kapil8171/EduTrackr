package com.example.studysmart.ui.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.R
import com.example.studysmart.domain.model.Subject
import com.example.studysmart.ui.presentation.components.AddSubjectDialog
import com.example.studysmart.ui.presentation.components.CountCard
import com.example.studysmart.ui.presentation.components.DeleteDialog
import com.example.studysmart.ui.presentation.components.SubjectCard
import com.example.studysmart.ui.presentation.components.studySessionsList
import com.example.studysmart.ui.presentation.components.tasksList
import com.example.studysmart.util.SnackbarEvent


@Composable
 fun DashboardScreen(
    viewModel: DashboardViewModel,
    onSubjectCardClick: (Int?) -> Unit ,
    onTaskCardClick: (Int?) -> Unit,
    onStartSessionButtonClick: () -> Unit

){

            val state by viewModel.state.collectAsStateWithLifecycle()
            val onEvent = viewModel::onEvent
            val tasks by viewModel.tasks.collectAsStateWithLifecycle()
            val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle()
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

                       }
                   }
               }
           }



     var isAddSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }
     var isDeleteSessionDialogOpen by rememberSaveable { mutableStateOf(false) }



       AddSubjectDialog(
           isOpen = isAddSubjectDialogOpen,
           selectedColors = state.subjectCardColors,
           subjectName = state.subjectName,
           goalHours = state.goalStudyHours,
           onColorChange = {onEvent(DashboardEvent.OnSubjectCardColorChange(colors = it))},
           onSubjectNameChange = { onEvent(DashboardEvent.OnSubjectNameChange(name = it))},
           onGoalHoursChange = {onEvent(DashboardEvent.OnGoalStudyHoursChange(hours = it))},
           onDismissRequest = { isAddSubjectDialogOpen = false },
           onConfirmButtonClick = {
               onEvent(DashboardEvent.SaveSubject)
             isAddSubjectDialogOpen = false
           }
       )

    DeleteDialog(
        isOpen = isDeleteSessionDialogOpen,
        title = "Delete Session",
        bodyText = "Are you sure you want to delete this session? Your studied hours will be reduced "+
        "by this session time. This action cannot be undone.",
        onDismissRequest = { isDeleteSessionDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(DashboardEvent.DeleteSession)
            isDeleteSessionDialogOpen = false }
    )
         
    Scaffold(
          topBar = { DashboardScreenTopBar() },
          snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
              item{
                  CountCardsSection(
                      modifier = Modifier
                          .fillMaxWidth()
                          .padding(12.dp),
                      subjectCount = state.totalSubjectCount,
                      studiedHours = state.totalStudiedHours.toString(),
                      goalHours =state.totalGoalStudyHours.toString()
                  )
              }

             item {
                 SubjectCardsSection(
                     modifier = Modifier.fillMaxWidth(),
                     subjectList = state.subjects,
                     onAddIconClicked = {
                         isAddSubjectDialogOpen = true
                     },
                     onSubjectCardClick = onSubjectCardClick
                 )
             }
            item {
                Button(
                    onClick = onStartSessionButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 20.dp)
                ) {
                     Text(
                         text = "Start Study Session"
                     )
                }
            }
            tasksList(
                sectionTitle = "UPCOMING TASKS",
                emptyListText = "You don't have any upcoming tasks.\n " +"Click on + button in subject screen to add new task.",
                tasks = tasks,
                onTaskCardClick = onTaskCardClick,
                onCheckBoxClick = {onEvent(DashboardEvent.OnTaskIsCompletedChange(task = it))}
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            studySessionsList(
                sectionTitle = "RECENT STUDY SESSIONS",
                emptyListText = "You don't have any recent study sessions.\n " +"Start a study session to begin  recording your progress.",
                sessions = recentSessions,
                onDeleteIconClick = {
                    onEvent(DashboardEvent.OnDeleteSessionButtonClick(session = it))
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
private fun DashboardScreenTopBar(){

       CenterAlignedTopAppBar(
           title = {
               Text(
                   text = "StudySmart",
                   style = MaterialTheme.typography.headlineMedium
               )
           }
       )
}

@Composable
private fun CountCardsSection(
    modifier: Modifier,
    subjectCount: Int,
    studiedHours: String,
    goalHours: String
){

       Row(modifier = modifier) {
           CountCard(
               modifier = Modifier.weight(1f),
               headingText = "Subject Count",
               count = subjectCount.toString()
           )
           Spacer(modifier = Modifier.width(10.dp))
           CountCard(
               modifier = Modifier.weight(1f),
               headingText = "Studied Hours",
               count = studiedHours
           )
           Spacer(modifier = Modifier.width(10.dp))
           CountCard(
               modifier = Modifier.weight(1f),
               headingText = "Goal Study Hours",
               count = goalHours
           )
       }
}


@Composable
private fun SubjectCardsSection(
    modifier: Modifier,
    subjectList: List<Subject>,
    emptyListText: String = "You don't have any subjects.\n Click on + button to add new subject.",
    onAddIconClicked: () -> Unit,
    onSubjectCardClick: (Int?) -> Unit
) {

        Column (modifier = modifier){
             Row(
                 modifier = Modifier.fillMaxWidth(),
                 verticalAlignment = Alignment.CenterVertically,
                 horizontalArrangement = Arrangement.SpaceBetween
             ){

                 Text(
                     text = "SUBJECTS",
                     style = MaterialTheme.typography.bodySmall,
                     modifier = Modifier.padding(start = 12.dp)
                 )

                  IconButton(onClick = onAddIconClicked) {
                      Icon(
                          imageVector = Icons.Default.Add,
                          contentDescription = "Add Subject"

                      )
                  }

             }

            if (subjectList.isEmpty()) {
                Image(
                    modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally),
                    painter = painterResource(R.drawable.img_books),
                    contentDescription = emptyListText
                    )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = emptyListText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center

                )

            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
            ) {
                items(subjectList) { subject ->
                    SubjectCard(
                        subjectName = subject.name,
                        gradientColors = subject.color.map { Color(it) },
                        onClick = {onSubjectCardClick(subject.subjectId)}
                    )

                }
            }


        }
}
