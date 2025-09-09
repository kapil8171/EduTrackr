package com.example.studysmart.presentation.session

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysmart.ui.presentation.components.DeleteDialog
import com.example.studysmart.ui.presentation.components.SubjectListBottomSheet
import com.example.studysmart.ui.presentation.components.studySessionsList
import com.example.studysmart.ui.presentation.session.SessionViewModel
import com.example.studysmart.ui.presentation.theme.Red
import com.example.studysmart.util.SnackbarEvent
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.DurationUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun SessionScreen(
    onBackButtonClick: () -> Unit,
    viewModel: SessionViewModel,
    // NEW PARAMETERS for timer - same as before
    hours: String,
    minutes: String,
    seconds: String,
    timerState: TimerState,
    onStartTimerClick: (subjectIdToAssociate: Int?) -> Unit,  // Corresponds to publicStartTimerTrigger
    onStopTimerClick: () -> Unit,  // Corresponds to publicStopTimerTrigger (for pause)
    onCancelTimerClick: () -> Unit, // Corresponds to publicCancelTimerTrigger (for reset)
    totalDuration: Duration,
    subjectRelatedIdFromService: Int? // This is the subjectId from the SERVICE
){
          val context = LocalContext.current

    val state by viewModel.state.collectAsStateWithLifecycle()
    val onEvent = viewModel::onEvent

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var isBottomSheetOpen by remember { mutableStateOf(false) }
    var isDeleteDialogOpen by rememberSaveable { mutableStateOf(false) }

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

    LaunchedEffect(key1 = state.subjects) {
          val subjectId = subjectRelatedIdFromService
          onEvent(
              SessionEvent.UpdateSubjectIdAndRelatedSubject(
                  subjectId =  subjectId,
                  relatedToSubject = state.subjects.find { it.subjectId == subjectId}?.name
                  )
          )
    }

    SubjectListBottomSheet(
        sheetState = sheetState,
        isOpen = isBottomSheetOpen,
        subjects = state.subjects,
        onDismissRequest = { isBottomSheetOpen = false },
        onSubjectClicked = {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) isBottomSheetOpen = false
            }
            onEvent(SessionEvent.OnRelatedSubjectChange(subject =  it))
        }
    )

    DeleteDialog(
        isOpen = isDeleteDialogOpen,
        title = "Delete Session?",
        bodyText = "Are you sure you want to delete this task? This action cannot be undone.",
        onDismissRequest = {isDeleteDialogOpen =false},
        onConfirmButtonClick = {
             onEvent(SessionEvent.DeleteSession)
            isDeleteDialogOpen =false}
    )

      Scaffold (
          snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
          topBar = {
              SessionScreenTopBar(onBackButtonClick = onBackButtonClick)
          }
      ){paddingValues ->
          LazyColumn(
              modifier = Modifier
                  .fillMaxSize()
                  .padding(paddingValues)
          ) {
              item {
                  TimerSection(
                      modifier = Modifier
                          .fillMaxWidth()
                          .aspectRatio(1f),
                      hours = hours,
                      minutes = minutes,
                      seconds = seconds
                  )
              }
              item{
                  RelatedToSubjectSection(
                      modifier = Modifier
                          .fillMaxWidth()
                          .padding(horizontal = 12.dp),
                      relatedToSubject = state.relatedToSubject ?: "",
                      selectSubjectButtonClick = {
                          isBottomSheetOpen = true
                      },
                      seconds = seconds
                  )
              }
              item {
                  ButtonSection(
                      modifier = Modifier
                          .fillMaxWidth()
                          .padding(12.dp),
//                      startButtonClick = onStartTimerClick,
                      startButtonClick = {
                          if (state.subjectId !=null && state.relatedToSubject !=null){
                              if (timerState == TimerState.STARTED){
                                  onStopTimerClick()
                              } else onStartTimerClick(state.subjectId)
                          } else {
                              onEvent(SessionEvent.NotifyToUpdateSubject)
                          }

                      },
                      cancelButtonClick = onCancelTimerClick,
                      finishButtonClick = {
                          val duration = totalDuration.toLong(DurationUnit.SECONDS)
                          if (duration >=36){
                              onCancelTimerClick()
                          }
                          onEvent(SessionEvent.SaveSession(duration = duration ))
                      },
                      timerState = timerState,
                      seconds = seconds
                  )
              }
              studySessionsList(
                  sectionTitle = " STUDY SESSIONS HISTORY",
                  emptyListText = "You don't have any recent study sessions.\n " +"Start a study session to begin  recording your progress.",
                  sessions = state.sessions,
                  onDeleteIconClick = {

                      isDeleteDialogOpen=true
                      onEvent(SessionEvent.OnDeleteSessionButtonClick(session = it))
                  }
              )
          }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionScreenTopBar(
    onBackButtonClick: () -> Unit
){
     TopAppBar(
         navigationIcon = {
             IconButton(onClick = onBackButtonClick) {
                 Icon(
                     imageVector = Icons.Default.ArrowBack,
                     contentDescription = "navigation to Back Screen"
                 )
             }
         },
             title = {
                 Text(text = "Study Sessions", style = MaterialTheme.typography.headlineSmall)
             }


     )
}

@Composable
private fun TimerSection(
    modifier: Modifier,
    hours: String,
    minutes: String,
    seconds: String
) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ){
            Box (
                modifier = Modifier
                    .size(250.dp)
                    .border(5.dp,MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            )
            Row {
                AnimatedContent(
                    targetState = hours,
                    label = hours,
                    transitionSpec = {timerTextAnimation()}
                ) {hours->
                    Text(
                        text = "$hours:",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 45.sp)
                    )
                }
                AnimatedContent(
                    targetState = minutes,
                    label = minutes,
                    transitionSpec = {timerTextAnimation()}
                ) {minutes->
                    Text(
                        text = "$minutes:",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 45.sp)
                    )
                }
                AnimatedContent(
                    targetState = seconds,
                    label = seconds,
                    transitionSpec = {timerTextAnimation()}
                ) {seconds->
                    Text(
                        text = seconds,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 45.sp)
                    )
                }




            }

        }
}

@Composable
private fun RelatedToSubjectSection(
    modifier: Modifier,
    relatedToSubject: String,
    selectSubjectButtonClick: () -> Unit,
    seconds: String
){
    Column(
        modifier =modifier
    ) {
        Text(
            text = "Related to subject",
            style = MaterialTheme.typography.bodySmall
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = relatedToSubject,
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(
                enabled = seconds=="00",
                onClick = selectSubjectButtonClick
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Subject"
                )
            }
        }
    }
}

@Composable
private fun ButtonSection(
    modifier: Modifier,
    timerState: TimerState,
    seconds: String,
    startButtonClick: () -> Unit,
    cancelButtonClick: () -> Unit,
    finishButtonClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Button(
            onClick = cancelButtonClick,
            enabled = seconds != "00" && timerState != TimerState.STARTED
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                text = "Cancel"
            )
        }
        Button(
            onClick = startButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (timerState == TimerState.STARTED) Red
                else MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                text = when(timerState){

                    TimerState.STARTED -> "Stop"
                    TimerState.STOPPED -> "Resume"
                    else -> "Start"
                }
            )
        }
        Button(
            onClick = finishButtonClick,
            enabled = seconds != "00" && timerState != TimerState.STARTED

        ) {
            Text(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                text = "Finish"
            )
        }
    }
}

private fun timerTextAnimation(duration: Int = 600): ContentTransform {
    return slideInVertically(animationSpec = tween(duration)) { fullHeight -> fullHeight } +
            fadeIn(animationSpec = tween(duration)) togetherWith
            slideOutVertically(animationSpec = tween(duration)) { fullHeight -> -fullHeight } +
            fadeOut(animationSpec = tween(duration))
}