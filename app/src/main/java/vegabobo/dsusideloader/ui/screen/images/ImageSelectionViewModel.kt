package vegabobo.dsusideloader.ui.screen.images

import android.app.Application
import android.net.Uri
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vegabobo.dsusideloader.core.BaseViewModel
import vegabobo.dsusideloader.core.StorageManager
import vegabobo.dsusideloader.model.Session
import vegabobo.dsusideloader.preferences.AppPrefs
import vegabobo.dsusideloader.util.FilenameUtils

data class ImageItem(
    val name: String,
    val uri: Uri,
    val size: Long,
)

data class ImageSelectionUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val images: List<ImageItem> = emptyList(),
    val folderReady: Boolean = false,
)

@HiltViewModel
class ImageSelectionViewModel @Inject constructor(
    private val application: Application,
    override val dataStore: DataStore<Preferences>,
    private val storageManager: StorageManager,
    private val session: Session,
) : BaseViewModel(dataStore) {

    private val _uiState = MutableStateFlow(ImageSelectionUiState())
    val uiState: StateFlow<ImageSelectionUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val path = readStringPref(AppPrefs.SAF_PATH)
            val folderReady = withContext(Dispatchers.IO) {
                path.isNotEmpty() && storageManager.arePermissionsGrantedToFolder(path)
            }
            val images = if (folderReady) {
                withContext(Dispatchers.IO) {
                    storageManager.listImagesInFolder(path).map {
                        ImageItem(
                            name = it.name ?: "",
                            uri = it.uri,
                            size = it.length(),
                        )
                    }
                }
            } else {
                emptyList()
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    folderReady = folderReady,
                    images = images,
                )
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun selectImage(image: ImageItem) {
        session.selectImage(image.name, image.uri)
    }

    fun selectFileFromUri(uri: Uri): Boolean {
        val filename = FilenameUtils.queryName(application.contentResolver, uri)
        val extension = filename.substringAfterLast(".", "").lowercase()
        val supportedFiles = mutableListOf("gz", "xz", "img", "gzip")
        if (Build.VERSION.SDK_INT > 29) {
            supportedFiles.add("zip")
        }

        if (extension.isEmpty() || !supportedFiles.contains(extension)) {
            return false
        }

        session.selectImage(filename, uri)
        return true
    }

    fun isSelected(image: ImageItem): Boolean {
        return session.userSelection.selectedFileUri == image.uri
    }
}
