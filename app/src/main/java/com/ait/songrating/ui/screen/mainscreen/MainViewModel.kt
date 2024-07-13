package com.ait.songrating.ui.screen.mainscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ait.songrating.ui.data.Song
import com.ait.songrating.ui.data.SongWithId
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class MainViewModel : ViewModel() {
    var songsUiState: SongsUiState by mutableStateOf(SongsUiState.Init)

    fun songList() = callbackFlow {
        val snapshotListener =
            FirebaseFirestore.getInstance().collection("songs")
                .addSnapshotListener() { snapshot, e ->
                    val response = if (snapshot != null) {
                        val songList = snapshot.toObjects(Song::class.java)
                        val songWithIdList = mutableListOf<SongWithId>()

                        songList.forEachIndexed { index, song ->
                            songWithIdList.add(SongWithId(snapshot.documents[index].id, song))
                        }

                        SongsUiState.Success(
                            songWithIdList
                        )
                    } else {
                        SongsUiState.Error(e?.message.toString())
                    }

                    trySend(response) // emit this value through the flow
                }
        awaitClose { // when we navigate out from the screen,
            // the flow stop and we stop here the firebase listener
            snapshotListener.remove()
        }
    }

    fun deletePost(songKey: String) {
        FirebaseFirestore.getInstance().collection(
            "songs"
        ).document(songKey).delete()
    }
}

sealed interface SongsUiState {
    object Init : SongsUiState
    object Loading : SongsUiState
    data class Success(val songList: List<SongWithId>) : SongsUiState
    data class Error(val error: String?) : SongsUiState
}