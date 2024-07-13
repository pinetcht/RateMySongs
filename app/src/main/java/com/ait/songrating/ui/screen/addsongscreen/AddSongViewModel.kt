package com.ait.songrating.ui.screen.addsongscreen

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ait.songrating.ui.data.Song
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.UUID

class AddSongViewModel : ViewModel() {

    var addSongUiState: AddSongUiState by mutableStateOf(AddSongUiState.Init)

    fun addSong(
        name: String,
        artist: String,
        songLink: String,
        upbeatRating: Float,
        sadRating: Float,
        exerciseRating: Float,
        studyRating: Float,
        overallRating: Float

    ) {
        addSongUiState = AddSongUiState.LoadingPostUpload

        val song = Song(
            uid = FirebaseAuth.getInstance().uid!!,
            author = FirebaseAuth.getInstance().currentUser?.email!!,
            name = name,
            artist = artist,
            songLink = songLink,
            upbeatRating = upbeatRating,
            sadRating = sadRating,
            exerciseRating = exerciseRating,
            studyRating = studyRating,
            overallRating = overallRating
        )

        val collection = FirebaseFirestore.getInstance().collection("songs")
        collection.add(song)
            .addOnSuccessListener {
                addSongUiState = AddSongUiState.PostUploadSuccess
            }
            .addOnFailureListener {
                addSongUiState = AddSongUiState.ErrorDuringPostUpload("Post upload failed")
            }

    }


    fun editSong(songKey: String, editedSong: Song) {
        addSongUiState = AddSongUiState.LoadingPostUpload

        FirebaseFirestore.getInstance().collection(
            "songs"
        ).document(songKey)
            .set(editedSong)
            .addOnSuccessListener {
                addSongUiState = AddSongUiState.PostUploadSuccess
            }
            .addOnFailureListener {
                addSongUiState = AddSongUiState.ErrorDuringPostUpload("Song upload failed")
            }
    }


}

sealed interface AddSongUiState {
    object Init : AddSongUiState
    object LoadingPostUpload : AddSongUiState
    object PostUploadSuccess : AddSongUiState
    data class ErrorDuringPostUpload(val error: String?) : AddSongUiState
    object LoadingImageUpload : AddSongUiState
    data class ErrorDuringImageUpload(val error: String?) : AddSongUiState
    object ImageUploadSuccess : AddSongUiState
}