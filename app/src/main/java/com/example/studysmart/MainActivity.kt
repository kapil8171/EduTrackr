package com.example.studysmart

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.studysmart.presentation.session.StudySessionTimerService
import com.example.studysmart.ui.navigation.NavRoutes
import com.example.studysmart.ui.presentation.dashboard.DashboardScreen
import com.example.studysmart.ui.presentation.dashboard.DashboardViewModel
import com.example.studysmart.presentation.session.SessionScreen
import com.example.studysmart.presentation.session.TimerState
import com.example.studysmart.ui.presentation.session.SessionViewModel
import com.example.studysmart.ui.presentation.subject.SubjectScreen
import com.example.studysmart.ui.presentation.subject.SubjectViewModel
import com.example.studysmart.ui.presentation.task.TaskScreen
import com.example.studysmart.ui.presentation.task.TaskViewModel
import com.example.studysmart.ui.presentation.theme.StudySmartTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.time.Duration


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isBound by mutableStateOf(false)
    private  var timerService: StudySessionTimerService? by mutableStateOf(null)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as StudySessionTimerService.StudySessionTimerBinder
            timerService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
            timerService =null
        }

    }


    override fun onStart() {
        super.onStart()
        Intent(this, StudySessionTimerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
                StudySmartTheme {
                    val navController = rememberNavController()

                    val currentHours by timerService?.hours ?: remember { mutableStateOf("00") }
                    val currentMinutes by timerService?.minutes ?: remember { mutableStateOf("00") }
                    val currentSeconds by timerService?.seconds ?: remember { mutableStateOf("00") }
                    val currentTimerState by timerService?.currentTimerState ?: remember { mutableStateOf(TimerState.IDLE) }

                   val currentDuration by timerService?.duration ?: remember { mutableStateOf(Duration.ZERO) }
                   val currentSubjectIdFromService by timerService?.subjectId ?: remember { mutableStateOf(null) }

                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.Dashboard.route
                    ) {
                        // Dashboard
                        composable(NavRoutes.Dashboard.route) {
                            val viewModel: DashboardViewModel = hiltViewModel()
                            DashboardScreen(
                                viewModel = viewModel,
                                onSubjectCardClick = { subjectId ->
                                    subjectId?.let {
                                        navController.navigate(
                                            NavRoutes.Subject.createRoute(subjectId = it)
                                        )
                                    }
                                },
                                onTaskCardClick = { taskId ->
                                    navController.navigate(
                                        NavRoutes.Task.createRoute(taskId, null)
                                    )
                                },
                                onStartSessionButtonClick = {
                                    navController.navigate(NavRoutes.Session.route)
                                }
                            )
                        }

                        // Subject
                        composable(
                            route = NavRoutes.Subject.route,
                            arguments = listOf(
                                navArgument("subjectId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->

                            val viewModel: SubjectViewModel = hiltViewModel()
                            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: -1
                            SubjectScreen(
                                viewModel = viewModel,
                                subjectId = subjectId,
                                onBackButtonClick = { navController.popBackStack() },
                                onAddTaskButtonClick = {
                                    navController.navigate(
                                        NavRoutes.Task.createRoute(
                                            taskId = null,
                                            subjectId = subjectId
                                        )
                                    )
                                },
                                onTaskCardClick = { taskId ->
                                    navController.navigate(
                                        NavRoutes.Task.createRoute(taskId, subjectId)
                                    )
                                }
                            )
                        }

                        // Task
                        composable(
                            route = NavRoutes.Task.route,
                            arguments = listOf(
                                navArgument("taskId") { type = NavType.IntType },
                                navArgument("subjectId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->

                            val viewModel: TaskViewModel = hiltViewModel()

                            val taskId = backStackEntry.arguments?.getInt("taskId")
                            val subjectId = backStackEntry.arguments?.getInt("subjectId")
                            TaskScreen(
                                viewModel = viewModel, taskId = taskId, subjectId = subjectId,
                                onBackButtonClick = {
                                    navController.navigateUp()
                                })
                        }

                        // Session
                        composable(
                            NavRoutes.Session.route,
                            deepLinks = listOf(
                                navDeepLink {
                                    uriPattern = "study_smart://dashboard/session"
                                    action = Intent.ACTION_VIEW
                                }
                            )
                        ) {
                            val viewModel: SessionViewModel = hiltViewModel()
                            SessionScreen(
                                viewModel = viewModel,
                                onBackButtonClick = { navController.navigateUp() },
                                hours = currentHours,
                                minutes = currentMinutes,
                                seconds = currentSeconds,
                                timerState = currentTimerState,
                                totalDuration = currentDuration,
                                subjectRelatedIdFromService = currentSubjectIdFromService,
                                // These now call the public trigger methods on the service
                                onStartTimerClick = { serviceSubjectIdToSet ->
                                    timerService?.publicStartTimerTrigger()
                                    serviceSubjectIdToSet?.let { timerService?.setRelatedSubject(it) }
                                                    },
                                onStopTimerClick = { timerService?.publicStopTimerTrigger() }, // For PAUSE
                                onCancelTimerClick = { timerService?.publicCancelTimerTrigger() } // RESET
                            )
                        }
                    }
                }


        }
        requestPermission()

    }


    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }

    }


    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
            timerService = null
        }
    }
}



//val tasks = listOf(
//    Task("Prepare Notes", "", 0L, 0, "English", false, 0, 1),
//    Task("Do Homework", "", 0L, 2, "Maths", true, 0, 1),
//    Task("Go Coaching", "", 0L, 1, "English", false, 0, 1),
//    Task("Assignment", "", 0L, 0, "English", false, 0, 1),
//    Task("Write Poem", "", 0L, 2, "English", true, 0, 1),
//)

//val sessions = listOf(
//    Session(0, "English", 22L, 2L, 0),
//    Session(1, "Math", 12L, 3L, 0),
//    Session(2, "Science", 10L, 4L, 0),
//    Session(3, "Geography", 5L, 5L, 0)
//)


