package vegabobo.dsusideloader.ui.screen.images

import android.content.Intent
import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.LocalFloatingDockPadding
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.ui.screen.Destinations
import vegabobo.dsusideloader.ui.util.launcherAcResult
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionScreen(
    navigate: (String) -> Unit,
    onImageSelected: () -> Unit,
    imageSelectionViewModel: ImageSelectionViewModel = hiltViewModel(),
) {
    val uiState by imageSelectionViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val unsupportedFileText = stringResource(id = R.string.file_unsupported)
    val manualFilePicker = launcherAcResult { uri ->
        if (imageSelectionViewModel.selectFileFromUri(uri)) {
            onImageSelected()
        } else {
            Toast.makeText(context, unsupportedFileText, Toast.LENGTH_SHORT).show()
        }
    }
    val filteredImages = remember(uiState.images, uiState.query) {
        if (uiState.query.isBlank()) {
            uiState.images
        } else {
            uiState.images.filter { it.name.contains(uiState.query, ignoreCase = true) }
        }
    }

    ApplicationScreen(
        enableDefaultScrollBehavior = false,
        columnContent = false,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.image_selection_title),
                scrollBehavior = it,
                actions = {
                    ImageOptionsMenu(
                        onRefresh = { imageSelectionViewModel.refresh() },
                        onPickFile = {
                            var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
                            chooseFile.type = "*/*"
                            chooseFile = Intent.createChooser(chooseFile, "")
                            manualFilePicker.launch(chooseFile)
                        },
                    )
                },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 12.dp,
                bottom = 24.dp + LocalFloatingDockPadding.current,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.query,
                    onValueChange = { imageSelectionViewModel.updateQuery(it) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                        )
                    },
                    placeholder = { Text(text = stringResource(id = R.string.search_images)) },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                )
            }

            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                !uiState.folderReady -> {
                    item {
                        MissingFolderCard(onClickSettings = { navigate(Destinations.Preferences) })
                    }
                }

                filteredImages.isEmpty() -> {
                    item { EmptyImagesCard() }
                }

                else -> {
                    items(filteredImages, key = { it.uri.toString() }) { image ->
                        ImageCard(
                            image = image,
                            isSelected = imageSelectionViewModel.isSelected(image),
                            onClick = {
                                imageSelectionViewModel.selectImage(image)
                                onImageSelected()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageOptionsMenu(
    onRefresh: () -> Unit,
    onPickFile: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = stringResource(id = R.string.image_options),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.refresh_images)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    onRefresh()
                },
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.pick_image_file)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.FolderOpen,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    onPickFile()
                },
            )
        }
    }
}

@Composable
private fun ImageCard(
    image: ImageItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        modifier = Modifier.size(26.dp),
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 16.dp),
            ) {
                Text(
                    text = image.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Formatter.formatFileSize(context, image.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isSelected) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = stringResource(id = R.string.image_selected),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun MissingFolderCard(onClickSettings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = stringResource(id = R.string.images_folder_missing),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onClickSettings,
            ) {
                Text(text = stringResource(id = R.string.images_folder_missing_action))
            }
        }
    }
}

@Composable
private fun EmptyImagesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier.size(40.dp),
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.images_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
