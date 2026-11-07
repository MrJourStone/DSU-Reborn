package vegabobo.dsusideloader.ui.screen.logs

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import vegabobo.dsusideloader.core.StorageManager
import vegabobo.dsusideloader.model.Session
import vegabobo.dsusideloader.util.OperationMode

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val session: Session,
    private val storageManager: StorageManager,
) : ViewModel() {

    val logs: StateFlow<String> = session.installationLogs
    val operationMode: StateFlow<OperationMode> = session.operationMode

    fun saveLogs(uri: Uri) {
        storageManager.writeStringToUri(logs.value, uri)
    }
}
