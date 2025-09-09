package com.example.studysmart.ui.navigation



sealed class NavRoutes(val route:String) {

    object Dashboard : NavRoutes("dashboard")

    object Subject : NavRoutes("subject/{subjectId}") {
        fun createRoute(subjectId: Int) = "subject/$subjectId"
    }

    object Task : NavRoutes("task/{taskId}?subjectId={subjectId}") {
        fun createRoute(taskId: Int?, subjectId: Int?) =
            "task/${taskId ?: -1}?subjectId=${subjectId ?: -1}"
    }

    object Session : NavRoutes("session")

}