package vegabobo.dsusideloader.ui.screen.logs

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.ui.screen.home.HomeDeviceInfoProvider
import vegabobo.dsusideloader.ui.util.launcherAcResult
import vegabobo.dsusideloader.util.OperationModeUtils
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    navigate: (String) -> Unit,
    logsViewModel: LogsViewModel = hiltViewModel(),
) {
    val logs by logsViewModel.logs.collectAsStateWithLifecycle()
    val operationMode by logsViewModel.operationMode.collectAsStateWithLifecycle()
    val emptyLogsText = stringResource(id = R.string.logs_empty)
    val logsSavedText = stringResource(id = R.string.saved_logs)
    val context = LocalContext.current

    val saveLogsResult = launcherAcResult {
        logsViewModel.saveLogs(it)
        Toast.makeText(context, logsSavedText, Toast.LENGTH_SHORT).show()
    }

    val displayLogs = remember(logs, operationMode, emptyLogsText) {
        val deviceInfo = HomeDeviceInfoProvider.collect()
        buildString {
            appendLine("DSU -Reborn ${deviceInfo.appVersion}")
            appendLine("Device: ${deviceInfo.deviceModel}")
            appendLine("Kernel: ${deviceInfo.kernelVersion}")
            appendLine("VBMeta: ${deviceInfo.vbmetaStatus}")
            appendLine("SELinux: ${deviceInfo.selinuxStatus}")
            appendLine("Operation mode: ${OperationModeUtils.getOperationModeAsString(operationMode)}")
            appendLine()
            if (logs.isBlank()) {
                appendLine(emptyLogsText)
            } else {
                append(logs)
            }
        }
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(displayLogs) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    ApplicationScreen(
        enableDefaultScrollBehavior = false,
        columnContent = false,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.logs_title),
                scrollBehavior = it,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                ) {
                    SelectionContainer {
                        Text(
                            text = displayLogs,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        intent.type = "text/plain"
                        intent.putExtra(Intent.EXTRA_TITLE, "dsu-reborn-logs")
                        saveLogsResult.launch(intent)
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Outlined.Save,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(id = R.string.save_logs))
                }
            }
        }
    }
}
