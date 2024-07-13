package com.ait.songrating.ui.screen.mainscreen

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ait.songrating.ui.data.Song
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ait.songrating.R
import com.ait.songrating.ui.data.SongWithId
import com.ait.songrating.ui.screen.addsongscreen.AddSongUiState
import com.ait.songrating.ui.screen.addsongscreen.AddSongViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.firebase.appcheck.internal.util.Logger
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.smarttoolfactory.ratingbar.RatingBar
import com.smarttoolfactory.ratingbar.model.GestureStrategy
import com.smarttoolfactory.ratingbar.model.RateChangeStrategy
import com.smarttoolfactory.ratingbar.model.RatingInterval
import com.smarttoolfactory.ratingbar.model.ShimmerEffect


@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel(), onNavigateToLogin: () -> Unit
) {
    val songListState = mainViewModel.songList().collectAsState(initial = SongsUiState.Init)
    var genre by remember { mutableStateOf("Overall") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var postIdForEdit: String? by remember { mutableStateOf(null) }


    Scaffold(topBar = {
        TopAppBar(title = { Text("Rate my Songs") }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ), actions = {

            DropDown(preselected = "Overall", onSelectionChanged = { genre = it })

            IconButton(onClick = {
                onNavigateToLogin()
            }) {
                Icon(imageVector = Icons.Filled.ExitToApp, contentDescription = "Exit")
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = {
                showAddDialog = true
            },
            containerColor = MaterialTheme.colorScheme.secondary,
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Create,
                contentDescription = "Add",
                tint = Color.White,
            )
        }
    }) { contentPadding ->

        Column(modifier = Modifier.padding(contentPadding)) {
            Text(text = "Sorted by $genre rating",
                modifier = Modifier.padding(10.dp))

            if (songListState.value == SongsUiState.Init) {
                Text(text = "Initializing..")
            } else if (songListState.value == SongsUiState.Loading) {
                CircularProgressIndicator()
            } else if (songListState.value is SongsUiState.Success) {

                val selectedCategory = "average${genre}Rating"
                val songList = (songListState.value as SongsUiState.Success).songList
                val sortedSongList = remember(songList, selectedCategory) {
                    songList.sortedByDescending { songWithId ->
                        when (selectedCategory) {
                            "averageUpbeatRating" -> songWithId.song.averageUpbeatRating
                            "averageSadRating" -> songWithId.song.averageSadRating
                            "averageExerciseRating" -> songWithId.song.averageExerciseRating
                            "averageStudyRating" -> songWithId.song.averageStudyRating
                            else -> songWithId.song.overallRating
                        }
                    }
                }


                LazyColumn(modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)) {
                    items(sortedSongList) {
                        SongCard(songWithId = it,
                            currentUserId = FirebaseAuth.getInstance().uid!!,
                            onRemoveItem = { mainViewModel.deletePost(it.postId) },
                            onEditItem = {
                                showEditDialog = true
                                postIdForEdit = it.postId
                            })
                    }
                }

            } else if (songListState.value is SongsUiState.Error) {
                Text(text = "Error: ${(mainViewModel.songsUiState as SongsUiState.Error).error}")
            }

            if (showAddDialog) {
                AddSongDialogue(
                    onDismissRequest = { showAddDialog = false })
            }

            if (showEditDialog) {
                AddSongDialogue(postId = postIdForEdit,
                    onDismissRequest = { showEditDialog = false })
            }
        }

    }
}


@Composable
fun SongCard(
    currentUserId: String = "",
    songWithId: SongWithId,
    onRemoveItem: () -> Unit = {},
    onEditItem: (SongWithId) -> Unit = {}

) {

    var expanded by rememberSaveable { mutableStateOf(false) }
    var showRateDialog by rememberSaveable {
        mutableStateOf(false)
    }
    val song = songWithId.getInternalSong()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ), modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)

    ) {
        Column() {
            Row(
                modifier = Modifier.padding(10.dp)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.defaultalbum),
                    contentDescription = "Default Album",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RectangleShape)
                )


                Column {
                    Text(text = song.name)
                    Text(text = song.artist)
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End


                ) {
                    // can only edit and delete your own messages
                    if (currentUserId.equals(song.uid)) {

                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = "Edit",
                            modifier = Modifier.clickable {
                                onEditItem(songWithId)
                            },
                            tint = Color.DarkGray
                        )

                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.clickable {
                                onRemoveItem()
                            },
                            tint = Color.DarkGray
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rate",
                            modifier = Modifier.clickable {
                                showRateDialog = true
                            },
                            tint = Color.DarkGray
                        )
                    }

                    if (showRateDialog) {
                        RateSongDialog(
                            songWithId = songWithId,
                            onDismissRequest = { showRateDialog = false })
                    }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (expanded) {
                                "Less"
                            } else {
                                "More"
                            }
                        )
                    }
                }

            }



            if (expanded) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                   listOf(RatingDisplay(onRatingChanged = { song.averageUpbeatRating }, text = "Upbeat", rating = song.averageUpbeatRating),
                        RatingDisplay(onRatingChanged = { song.averageSadRating }, text = "Sad", rating = song.averageSadRating),
                        RatingDisplay(onRatingChanged = { song.averageExerciseRating }, text = "Exercise",  rating = song.averageExerciseRating),
                        RatingDisplay(onRatingChanged = { song.averageStudyRating }, text = "Study",  rating = song.averageStudyRating),
                        RatingDisplay(onRatingChanged = { song.overallRating }, text = "Overall", rating = song.overallRating))
                }
            }

        }


    }
}

@Composable
fun RatingDisplay(
    text: String,
    rating: Float,
    onRatingChanged: (Float) -> Unit,

){
    val imageBackground = ImageBitmap.imageResource(id = R.drawable.star)
    val imageForeground = ImageBitmap.imageResource(id = R.drawable.star_full)
    var currentRating by remember { mutableStateOf(rating) }


    Row() {
        Text(text = text)
        Spacer(modifier = Modifier.fillMaxWidth(0.05f))
        RatingBar(
            rating = currentRating,
            space = 2.dp,
            imageEmpty = imageBackground,
            imageFilled = imageForeground,
            itemSize = 20.dp
        ) {
            currentRating = it
            onRatingChanged(it)
        }
        Spacer(modifier = Modifier.fillMaxWidth(0.05f))
        Text(text = currentRating.toString().take(3))
    }
}


@Composable
fun RateSongDialog(
    songWithId: SongWithId, onDismissRequest: () -> Unit = {}
) {
    val song = songWithId.song
    val songName = song.name

    var upbeatRating by rememberSaveable { mutableStateOf(0f) }
    var sadRating by rememberSaveable { mutableStateOf(0f) }
    var exerciseRating by rememberSaveable { mutableStateOf(0f) }
    var studyRating by rememberSaveable { mutableStateOf(0f) }
    var overallRating by rememberSaveable { mutableStateOf(0f) }
    var totalRatings by rememberSaveable { mutableStateOf(0f) }

    val songRef = Firebase.firestore.collection("songs").document(songWithId.postId)

    // Fetch total ratings from Firestore
    LaunchedEffect(songWithId.postId) {
        songRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val data = document.toObject(Song::class.java)
                totalRatings = data?.totalRatings ?: 0f
            }
        }
    }


    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(5.dp),
            shape = RoundedCornerShape(16.dp)
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Rate $songName for these genres",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center),
                    fontSize = 15.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(text = "Upbeat")

                    StarRatingBar(maxStars = 5, rating = upbeatRating, onRatingChanged = {
                        upbeatRating = it
                        totalRatings++
                    })

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sad")

                    StarRatingBar(maxStars = 5, rating = sadRating, onRatingChanged = {
                        sadRating = it
                    })

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Exercise")

                    StarRatingBar(maxStars = 5, rating = exerciseRating, onRatingChanged = {
                        exerciseRating = it
                    })

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Study")

                    StarRatingBar(maxStars = 5, rating = studyRating, onRatingChanged = {
                        studyRating = it
                    })

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Overall")

                    StarRatingBar(maxStars = 5, rating = overallRating, onRatingChanged = {
                        overallRating = it
                    })

                }

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Button(onClick = {
                        // Update ratings in Firestore whenever any rating changes
                        songRef.update(
                            mapOf(
                                "upbeatRating" to (song.upbeatRating + upbeatRating),
                                "sadRating" to (song.sadRating + sadRating),
                                "exerciseRating" to (song.exerciseRating + exerciseRating),
                                "studyRating" to (song.studyRating + studyRating),
                                "totalRatings" to (song.totalRatings + totalRatings)
                            )
                        )
                        onDismissRequest()

                    }) {
                        Text(text = "Save")
                    }

                    Button(onClick = { onDismissRequest() }) {
                        Text(text = "Cancel")
                    }
                }
            }

        }

    }
}

@Composable
fun DropDown(
    preselected: String, onSelectionChanged: (String) -> Unit, modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf(preselected) }
    var expanded by remember { mutableStateOf(false) }
    val category = listOf("Upbeat", "Sad", "Exercise", "Study", "Overall")


    OutlinedCard(modifier = modifier.clickable {
        expanded = !expanded
    }) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                category.forEach { listEntry ->
                    DropdownMenuItem(text = {
                        Text(
                            text = listEntry,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Start)
                        )
                    }, onClick = {
                        selected = listEntry
                        expanded = false
                        onSelectionChanged(selected)
                    })
                }
            }

            Icon(
                Icons.Filled.ArrowDropDown, null, modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun AddSongDialogue(
    addSongViewModel: AddSongViewModel = viewModel(),
    postId: String? = null,
    onDismissRequest: () -> Unit,
    onNavigateToMainScreen: () -> Unit = {}
) {


    var songWithId by remember { mutableStateOf<SongWithId?>(null) }

    LaunchedEffect(postId) {
        if (postId != null) {
            FirebaseFirestore.getInstance().collection("songs").document(postId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val songObj = document.toObject(Song::class.java)
                        songWithId = SongWithId(document.id, songObj!!)

                    } else {
                        Log.d(Logger.TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(Logger.TAG, "get failed with ", exception)
                }
        }
    }

    val song = songWithId?.getInternalSong()

    var songName by rememberSaveable { mutableStateOf(song?.name ?: "") }
    var songArtist by rememberSaveable { mutableStateOf(song?.artist ?: "") }
    var songLink by rememberSaveable { mutableStateOf(song?.songLink ?: "") }


    var upbeatRating by rememberSaveable { mutableFloatStateOf(song?.upbeatRating ?: 0f) }
    var sadRating by rememberSaveable { mutableFloatStateOf(song?.sadRating ?: 0f) }
    var exerciseRating by rememberSaveable { mutableFloatStateOf(song?.exerciseRating ?: 0f) }
    var studyRating by rememberSaveable { mutableFloatStateOf(song?.studyRating ?: 0f) }
    var overallRating by rememberSaveable { mutableFloatStateOf(song?.overallRating ?: 0f) }

    LaunchedEffect(song) {
        songName = song?.name ?: ""
        songArtist = song?.artist ?: ""
        songLink = song?.songLink ?: ""
        upbeatRating = song?.upbeatRating ?: 0f
        sadRating = song?.sadRating ?: 0f
        exerciseRating = song?.exerciseRating ?: 0f
        studyRating = song?.studyRating ?: 0f
        overallRating = song?.overallRating ?: 0f
    }

    var isError by remember { mutableStateOf(false) }


    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center, modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (song == null) "Add song" else "Edit song",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center

                )


                OutlinedTextField(value = songName,
                    isError = isError,
                    label = { Text(text = "Name") },
                    supportingText = {
                        if (isError) {
                            Text(text = "Error")
                        }
                    },
                    onValueChange = {
                        songName = it

                        if (songName.isEmpty() || songName.isBlank()){
                            isError = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    trailingIcon = {
                        if (isError)
                            Icon(
                                Icons.Filled.Warning,
                                "Error", tint = MaterialTheme.colorScheme.error)
                        else
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear Text",
                                modifier = Modifier
                                    .clickable {
                                        songName = ""
                                    }
                            )
                    }
                )
                OutlinedTextField(value = songArtist,
                    isError = isError,
                    label = { Text(text = "Artist") },
                    supportingText = {
                        if (isError) {
                            Text(text = "Error")
                        }
                    },
                    onValueChange = {
                        songArtist = it

                        if (songArtist.isEmpty() || songArtist.isBlank()){
                            isError = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    trailingIcon = {
                        if (isError)
                            Icon(
                                Icons.Filled.Warning,
                                "Error", tint = MaterialTheme.colorScheme.error)
                        else
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear Text",
                                modifier = Modifier
                                    .clickable {
                                        songArtist = ""
                                    }
                            )
                    }
                )

                OutlinedTextField(value = songLink,
                    isError = isError,
                    label = { Text(text = "Song Url") },
                    supportingText = {
                        if (isError) {
                            Text(text = "Error")
                        }
                    },
                    onValueChange = {
                        songLink = it

                        if (songLink.isEmpty() || songLink.isBlank()){
                            isError = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    trailingIcon = {
                        if (isError)
                            Icon(
                                Icons.Filled.Warning,
                                "Error", tint = MaterialTheme.colorScheme.error)
                        else
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear Text",
                                modifier = Modifier
                                    .clickable {
                                        songName = ""
                                    }
                            )
                    }
                )


                Text(
                    text = "Rate each genre",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(text = "Upbeat")

                    StarRatingBar(maxStars = 5, rating = upbeatRating, onRatingChanged = {
                        upbeatRating = it
                    })

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sad")

                    StarRatingBar(maxStars = 5, rating = sadRating, onRatingChanged = {
                        sadRating = it
                    })

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Exercise")

                    StarRatingBar(maxStars = 5, rating = exerciseRating, onRatingChanged = {
                        exerciseRating = it
                    })

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Study")

                    StarRatingBar(maxStars = 5, rating = studyRating, onRatingChanged = {
                        studyRating = it
                    })

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Overall")

                    StarRatingBar(maxStars = 5, rating = overallRating, onRatingChanged = {
                        overallRating = it
                    })

                }

                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Button(
                        enabled = songName.isNotEmpty() && songArtist.isNotEmpty() && songLink.isNotEmpty(),
                        onClick = {
                            if (song == null) {
                                addSongViewModel.addSong(
                                    name = songName,
                                    artist = songArtist,
                                    songLink = songLink,
                                    upbeatRating = upbeatRating,
                                    sadRating = sadRating,
                                    exerciseRating = exerciseRating,
                                    studyRating = studyRating,
                                    overallRating = overallRating
                                )
                            }

                            // edit song
                            else {
                                val editedSong = song.copy(
                                    name = songName, artist = songArtist, songLink = songLink
                                )

                                addSongViewModel.editSong(postId!!, editedSong)

                            }

                            onDismissRequest()
                        },
                        modifier = Modifier.padding(15.dp),

                        ) {
                        Text(text = "Save Item")
                    }

                    Button(onClick = { onDismissRequest() }) {
                        Text(text = "Cancel")
                    }




                    when (addSongViewModel.addSongUiState) {
                        is AddSongUiState.Init -> {}
                        is AddSongUiState.LoadingPostUpload -> CircularProgressIndicator()
                        is AddSongUiState.PostUploadSuccess -> onNavigateToMainScreen()
                        is AddSongUiState.ErrorDuringPostUpload -> {
                            Text(
                                text = "Error: ${
                                    (addSongViewModel.addSongUiState as AddSongUiState.ErrorDuringPostUpload).error
                                }"
                            )
                        }

                        is AddSongUiState.LoadingImageUpload -> CircularProgressIndicator()
                        is AddSongUiState.ImageUploadSuccess -> {
                            Text(text = "Image uploaded, starting post upload.")
                        }

                        is AddSongUiState.ErrorDuringImageUpload -> Text(
                            text = "${(addSongViewModel.addSongUiState as AddSongUiState.ErrorDuringImageUpload).error}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StarRatingBar(
    maxStars: Int = 5, rating: Float, onRatingChanged: (Float) -> Unit
) {
    val density = LocalDensity.current.density
    val starSize = (12f * density).dp
    val starSpacing = (0.5f * density).dp

    Row(
        modifier = Modifier.selectableGroup(), verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            val icon = if (isSelected) Icons.Filled.Star else Icons.Default.Star
            val iconTintColor = if (isSelected) Color(0xFFFFC700) else Color(0xffd3d3d3)
            Icon(imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier
                    .selectable(selected = isSelected, onClick = {
                        onRatingChanged(i.toFloat())
                    })
                    .width(starSize)
                    .height(starSize))

            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }

        }
    }
}