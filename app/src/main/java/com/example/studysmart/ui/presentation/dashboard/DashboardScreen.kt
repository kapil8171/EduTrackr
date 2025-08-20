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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.studysmart.R
import com.example.studysmart.domain.model.Session
import com.example.studysmart.domain.model.Subject
import com.example.studysmart.domain.model.Task
import com.example.studysmart.ui.presentation.components.CountCard
import com.example.studysmart.ui.presentation.components.SubjectCard
import com.example.studysmart.ui.presentation.components.studySessionsList
import com.example.studysmart.ui.presentation.components.tasksList


@Composable
fun DashboardScreen(){

       val subjects = listOf(
           Subject("English",10f, color = Subject.subjectCardColors[0],0),
           Subject("Math",10f, color = Subject.subjectCardColors[1],0),
           Subject("Hindi",10f, color = Subject.subjectCardColors[2],0),
           Subject("Computer",10f, color = Subject.subjectCardColors[3],0),
           Subject("Science",10f, color = Subject.subjectCardColors[4],0),
       )

    val tasks = listOf(
        Task("Prepare Notes", "",0L, 0, "English", false,0,1),
        Task("Do Homework", "",0L, 2, "Maths", true,0,1),
        Task("Go Coaching", "",0L, 1, "English", false,0,1),
        Task("Assignment", "",0L, 0, "English", false,0,1),
        Task("Write Poem", "",0L, 2, "English", true,0,1),
    )

    val sessions = listOf(
        Session(0,"English",22L,2L,0),
        Session(1,"Math",12L,3L,0),
        Session(2,"Science",10L,4L,0),
        Session(3,"Geography",5L,5L,0)
    )

    Scaffold(
          topBar = { DashboardScreenTopBar() }
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
                      subjectCount = 5,
                      studiedHours = "10",
                      goalHours ="15"
                  )
              }

             item {
                 SubjectCardsSection(
                     modifier = Modifier.fillMaxWidth(),
                     subjectList = subjects
                 )
             }
            item {
                Button(
                    onClick = { /*TODO*/ },
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
                onTaskCardClick = {},
                onCheckBoxClick = {}
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            studySessionsList(
                sectionTitle = "RECENT STUDY SESSIONS",
                emptyListText = "You don't have any recent study sessions.\n " +"Start a study session to begin  recording your progress.",
                sessions = sessions,
                onDeleteIconClick = {}
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
    emptyListText: String = "You don't have any subjects.\n Click on + button to add new subject."
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

                  IconButton(onClick = {

                  }) {
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
                        gradientColors = subject.color,
                        onClick = {}
                    )

                }
            }


        }
}