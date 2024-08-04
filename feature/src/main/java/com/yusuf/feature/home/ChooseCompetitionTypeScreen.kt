package com.yusuf.feature.home

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.yusuf.component.LoadingLottie
import com.yusuf.domain.model.firebase.CompetitionData
import com.yusuf.domain.util.RootResult
import com.yusuf.feature.R
import com.yusuf.feature.home.viewmodel.CompetitionViewModel
import com.yusuf.navigation.NavigationGraph
import com.yusuf.theme.DARK_BLUE
import com.yusuf.theme.APPBAR_GREEN
import com.yusuf.utils.Competition
import com.yusuf.utils.SharedPreferencesHelper
import com.yusuf.utils.predefinedCompetitions
import com.yusuf.utils.toCompetition


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChooseCompetitionTypeScreen(
    navController: NavController,
    competitionViewModel: CompetitionViewModel = hiltViewModel()
) {
    val addDeleteState by competitionViewModel.addDeleteState.collectAsState()
    val getAllState by competitionViewModel.getAllState.collectAsState()
    val openDialog = remember { mutableStateOf(false) }
    val openUpdateDialog = remember { mutableStateOf(false) }
    val selectedCompetition = remember { mutableStateOf<CompetitionData?>(null) }
    var competitionDatatoUpdate by remember { mutableStateOf<CompetitionData?>(null) }

    val context = LocalContext.current
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }

    val sharedPreferencesHelper = SharedPreferencesHelper(context)

    BackHandler {
        (context as? Activity)?.finish()
    }

    LaunchedEffect(Unit) {
        competitionViewModel.getAllCompetitions()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri.value = uri
        }
    )

    Scaffold(
        floatingActionButton = {
            if (getAllState.result !is RootResult.Loading) {
                FloatingActionButton(onClick = { openDialog.value = true }, containerColor = APPBAR_GREEN, contentColor = Color.White) {
                    Icon(Icons.Default.Add, contentDescription = "Add Competition")
                }
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (openDialog.value) {
                    AddCompetitionDialog(
                        onDismiss = { openDialog.value = false },
                        onSave = { competition ->
                            selectedImageUri.value?.let { uri ->
                                val bitmap = MediaStore.Images.Media.getBitmap(
                                    context.contentResolver,
                                    uri
                                )
                                competitionViewModel.uploadImageAndAddCompetition(bitmap, competition.competitionName)
                            }
                        },
                        onImagePick = {
                            imagePickerLauncher.launch("image/*")
                        },
                        selectedImageUri = selectedImageUri.value
                    )
                }

                if (openUpdateDialog.value && selectedCompetition.value != null) {
                    competitionDatatoUpdate?.let { it1 ->
                        UpdateCompetitionDialog(
                            competitionData = it1,
                            onDismiss = { openUpdateDialog.value = false },
                            onUpdateCompetition = { competitionData ->
                                competitionViewModel.updateCompetition(
                                    competitionData.competitionId,
                                    competitionData,
                                    selectedImageUri.value,
                                    context
                                )
                                openUpdateDialog.value = false
                            },
                            onImagePick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            selectedImageUri = selectedImageUri.value
                        )
                    }
                }

                when (val state = getAllState.result) {
                    is RootResult.Loading -> {
                        Column(
                            Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LoadingLottie(R.raw.loading_anim)
                        }
                    }

                    is RootResult.Success -> {
                        if (getAllState.competitions.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "You don't have any competitions yet.",
                                    modifier = Modifier.padding(bottom = 10.dp),
                                    fontFamily = FontFamily(
                                        Font(R.font.onboarding_title1, FontWeight.Normal)
                                    ),
                                    style = TextStyle(
                                        fontSize = 20.sp
                                    )
                                )
                                Text(
                                    text = "Start adding now.",
                                    fontFamily = FontFamily(
                                        Font(R.font.onboarding_title1, FontWeight.Normal)
                                    ),
                                    style = TextStyle(
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }

                        val competitions = state.data ?: emptyList()
                        LazyColumn(
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            items(competitions) { competition ->

                                CompetitionCard(
                                    competition = competition,
                                    onClick = {
                                        val competitionData = competition.toCompetition()

                                        Log.d("ChooseSportScreen", "Competition clicked: ${competitionData.toString()}")

                                        if (competitionData != null) {
                                            sharedPreferencesHelper.competitionName = competition.competitionName
                                            navController.navigate(NavigationGraph.getOptionsRoute(competitionData))
                                        }
                                    },
                                    onDelete = {
                                        Log.d(
                                            "ChooseSportScreen",
                                            "Delete button clicked for competition: ${competition.competitionId}"
                                        )
                                        competitionViewModel.deleteCompetition(competition)
                                    },
                                    onUpdate = {
                                        selectedCompetition.value = competition
                                        competitionDatatoUpdate = competition
                                        openUpdateDialog.value = true
                                    }
                                )
                            }
                        }
                    }

                    is RootResult.Error -> {
                        Text(text = state.message)
                    }

                    else -> {}
                }

                when (val addState = addDeleteState.result) {
                    is RootResult.Loading -> {
                        Column(Modifier.fillMaxSize()) {
                            LoadingLottie(R.raw.loading_anim)
                        }
                    }

                    is RootResult.Success -> {
                        Log.d("ChooseSportScreen", "Competition added/updated successfully: ${addState.data}")
                    }

                    is RootResult.Error -> {
                        Text(text = addState.message)
                    }

                    else -> {}
                }
            }
        }
    )
}

@Composable
fun AddCompetitionDialog(
    onDismiss: () -> Unit,
    onSave: (Competition) -> Unit,
    onImagePick: () -> Unit,
    selectedImageUri: Uri?
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCompetition by remember { mutableStateOf<Competition?>(null) }
    var customCompetitionName by remember { mutableStateOf("") }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Competition") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onImagePick() }
                            .align(Alignment.CenterHorizontally)
                    ) {
                        selectedImageUri?.let { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,

                            )
                        } ?: run {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Select Image",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(50.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                Spacer(modifier = Modifier.height(16.dp))
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded = true
                                customCompetitionName = ""
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCompetition?.competitionName ?: "Select Competition",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        predefinedCompetitions.forEach { competition ->
                            DropdownMenuItem(
                                text = { Text(competition.competitionName) },
                                onClick = {
                                    selectedCompetition = competition
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = customCompetitionName,
                    onValueChange = {
                        customCompetitionName = it
                        selectedCompetition = null
                    },
                    label = { Text("Or Enter Custom Competition Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val competitionName = selectedCompetition?.competitionName ?: customCompetitionName
                if (competitionName.isNotBlank()) {
                    if (selectedCompetition != null) {
                        onSave(selectedCompetition!!)
                    } else {
                        onSave(
                            Competition(
                                competitionName = competitionName,
                                competitionFirstImage = 0,
                                competitionTeamImage = 0
                            )
                        )
                    }
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UpdateCompetitionDialog(
    competitionData: CompetitionData,
    onDismiss: () -> Unit,
    onUpdateCompetition: (CompetitionData) -> Unit,
    onImagePick: () -> Unit,
    selectedImageUri: Uri?
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCompetition by remember { mutableStateOf<Competition?>(null) }
    var customCompetitionName by remember { mutableStateOf("") }

    LaunchedEffect(competitionData) {
        selectedCompetition = predefinedCompetitions.find { it.competitionName == competitionData.competitionName }
        customCompetitionName = if (selectedCompetition == null) competitionData.competitionName else ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Update Competition") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onImagePick() }
                        .align(Alignment.CenterHorizontally)
                ) {
                    selectedImageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                    } ?: run {
                        Image(
                            painter = rememberAsyncImagePainter(competitionData.competitionImageUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expanded = true
                                customCompetitionName = ""
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCompetition?.competitionName ?: "Select Competition",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        predefinedCompetitions.forEach { competition ->
                            DropdownMenuItem(
                                text = { Text(competition.competitionName) },
                                onClick = {
                                    selectedCompetition = competition
                                    customCompetitionName = ""
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = customCompetitionName,
                    onValueChange = {
                        customCompetitionName = it
                        selectedCompetition = null
                    },
                    label = { Text("Or Enter Custom Competition Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val competitionName = selectedCompetition?.competitionName ?: customCompetitionName
                if (competitionName.isNotBlank()) {
                    onUpdateCompetition(
                        competitionData.copy(
                            competitionName = competitionName
                        )
                    )
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun CompetitionCard(
    competition: CompetitionData,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Box {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.image_loading))
            var isImageLoading by remember { mutableStateOf(true) }

            SubcomposeAsyncImage(
                model = competition.competitionImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop,
                loading = {
                    LottieAnimation(
                        composition,
                        modifier = Modifier.size(100.dp),
                        iterations = Int.MAX_VALUE
                    )
                },
                onSuccess = {
                    isImageLoading = false
                },
                onError = {
                    isImageLoading = false
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )

            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                if (!isImageLoading) {
                    Text(
                        text = competition.competitionName,
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 35.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = Color.White
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onUpdate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DARK_BLUE,
                        contentColor = Color.White
                    )
                ) {
                    Text("Update")
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}