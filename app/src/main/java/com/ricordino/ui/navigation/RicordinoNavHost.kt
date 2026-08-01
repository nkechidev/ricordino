package com.ricordino.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ricordino.ui.capture.CaptureScreen
import com.ricordino.ui.detail.NoteDetailScreen
import com.ricordino.ui.notelist.NoteListScreen
import com.ricordino.ui.review.ReviewScreen

@Composable
fun RicordinoNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Destination.NoteList.route) {
        composable(Destination.NoteList.route) {
            NoteListScreen(
                onNoteClick = { noteId -> navController.navigate(Destination.NoteDetail.createRoute(noteId)) },
                onCaptureClick = { navController.navigate(Destination.Capture.route) },
            )
        }

        composable(Destination.Capture.route) {
            CaptureScreen(
                onPhotoCaptured = { photoPath ->
                    navController.navigate(Destination.Review.createRoute(photoPath)) {
                        // Drop Capture from the back stack — pressing back from Review
                        // should return to the list, not to a spent camera session.
                        popUpTo(Destination.Capture.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Destination.Review.route,
            arguments = listOf(navArgument(Destination.Review.PHOTO_PATH_ARG) { type = NavType.StringType }),
        ) {
            ReviewScreen(
                onNoteSaved = { navController.popBackStack(Destination.NoteList.route, inclusive = false) },
            )
        }

        composable(
            route = Destination.NoteDetail.route,
            arguments = listOf(navArgument(Destination.NoteDetail.NOTE_ID_ARG) { type = NavType.LongType }),
        ) {
            NoteDetailScreen(
                onDeleted = { navController.popBackStack() },
            )
        }
    }
}
