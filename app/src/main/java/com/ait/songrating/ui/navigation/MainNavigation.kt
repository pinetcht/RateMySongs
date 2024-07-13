package com.ait.songrating.ui.navigation

import com.ait.songrating.ui.data.Song
import com.ait.songrating.ui.data.SongWithId

sealed class MainNavigation(val route: String) {
    object LoginScreen : MainNavigation("loginscreen")
    object MainScreen : MainNavigation("mainscreen")

}