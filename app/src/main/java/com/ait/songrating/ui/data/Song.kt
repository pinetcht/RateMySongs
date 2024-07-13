package com.ait.songrating.ui.data

import kotlin.math.round


data class Song(
    var uid: String = "",
    var author: String = "",
    var name: String = "",
    var artist: String = "",
    var songLink: String = "",

    var upbeatRating: Float = 0f,
    var sadRating: Float = 0f,
    var exerciseRating: Float = 0f,
    var studyRating: Float = 0f,

    var overallRating: Float = 0f,
    var totalRatings: Float = 1f,

){
    // Custom getter for averageUpbeatRating
    val averageUpbeatRating: Float
        get() = if (totalRatings == 0f) 0f else (upbeatRating / totalRatings)

    // Custom getter for averageSadRating
    val averageSadRating: Float
        get() = if (totalRatings == 0f) 0f else (sadRating / totalRatings)

    // Custom getter for averageExerciseRating
    val averageExerciseRating: Float
        get() = if (totalRatings == 0f) 0f else (exerciseRating / totalRatings)

    // Custom getter for averageStudyRating
    val averageStudyRating: Float
        get() = if (totalRatings == 0f) 0f else (studyRating / totalRatings)
}

data class SongWithId(
    val postId: String, val song: Song
){
    fun getInternalSong() : Song {
        return song
    }
}
