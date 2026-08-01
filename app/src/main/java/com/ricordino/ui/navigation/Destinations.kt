package com.ricordino.ui.navigation

import android.net.Uri

sealed class Destination(val route: String) {
    data object NoteList : Destination("noteList")

    data object Capture : Destination("capture")

    data object Review : Destination("review/{photoPath}") {
        const val PHOTO_PATH_ARG = "photoPath"

        // File paths contain '/' characters, which would otherwise be misread as path
        // segment separators by the nav route matcher.
        fun createRoute(photoPath: String): String = "review/${Uri.encode(photoPath)}"
    }

    data object NoteDetail : Destination("noteDetail/{noteId}") {
        const val NOTE_ID_ARG = "noteId"

        fun createRoute(noteId: Long): String = "noteDetail/$noteId"
    }
}
