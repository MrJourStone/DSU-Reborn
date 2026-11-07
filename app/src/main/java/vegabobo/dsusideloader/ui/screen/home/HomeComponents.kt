package vegabobo.dsusideloader.ui.screen.home

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.io.File
import vegabobo.dsusideloader.BuildConfig
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.preparation.InstallationStep
import vegabobo.dsusideloader.util.DevicePropUtils
import vegabobo.dsusideloader.util.OperationMode
import vegabobo.dsusideloader.util.OperationModeUtils

data class HomeDeviceInfo(
    val appVersion: String,
    val kernelVersion: String,
    val deviceModel: String,
    val vbmetaStatus: String,
    val selinuxStatus: String,
)

object HomeDeviceInfoProvider {
    fun collect(): HomeDeviceInfo {
        val vbmetaDeviceState = DevicePropUtils.getSystemPropertyValue("ro.boot.vbmeta.device_state", "")
        val verifiedBootState = DevicePropUtils.getSystemPropertyValue("ro.boot.verifiedbootstate", "")
        val vbmetaStatus = when {
            vbmetaDeviceState.isNotEmpty() -> vbmetaDeviceState.capitalizeFirst()
            verifiedBootState.isNotEmpty() -> verifiedBootState.capitalizeFirst()
            else -> "Unknown"
        }

        val selinuxEnforce = runCatching {
            File("/sys/fs/selinux/enforce").readText().trim()
        }.getOrNull()
        val selinuxStatus = when (selinuxEnforce) {
            "1" -> "Enforcing"
            "0" -> "Permissive"
            else -> DevicePropUtils.getSystemPropertyValue("ro.boot.selinux", "Unknown").capitalizeFirst()
        }

        val kernelVersion = System.getProperty("os.version")
            ?: DevicePropUtils.getSystemPropertyValue("ro.kernel.version", "Unknown")

        val deviceModel = listOf(Build.MANUFACTURER, Build.MODEL)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .capitalizeFirst()

        return HomeDeviceInfo(
            appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            kernelVersion = kernelVersion,
            deviceModel = deviceModel,
            vbmetaStatus = vbmetaStatus,
            selinuxStatus = selinuxStatus,
        )
    }

    private fun String.capitalizeFirst(): String {
        return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

@Composable
fun HomeStatusCard(operationMode: OperationMode) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 16.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.working_status),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${stringResource(id = R.string.method_status)}: " +
                        OperationModeUtils.getOperationModeAsString(operationMode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
fun SelectedImageCard(
    imageName: String,
    installationStep: InstallationStep,
    isInstalling: Boolean,
    onClickReboot: () -> Unit,
    onClickDiscard: () -> Unit,
) {
    val hasSelectedImage = imageName.isNotEmpty()
    val dsuInstalled = installationStep == InstallationStep.DSU_ALREADY_INSTALLED ||
        installationStep == InstallationStep.INSTALL_SUCCESS_REBOOT_DYN_OS
    val canDiscard = dsuInstalled || hasSelectedImage
    val canReboot = dsuInstalled && !isInstalling

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            imageVector = Icons.Outlined.FolderOpen,
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
                        text = stringResource(id = R.string.selected_image),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (hasSelectedImage) {
                            imageName.removeImageExtension()
                        } else {
                            stringResource(id = R.string.no_image_selected)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1F),
                    onClick = onClickReboot,
                    enabled = canReboot,
                ) {
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(id = R.string.reboot))
                }
                OutlinedButton(
                    modifier = Modifier.weight(1F),
                    onClick = onClickDiscard,
                    enabled = canDiscard && !isInstalling,
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(id = R.string.discard))
                }
            }
        }
    }
}

@Composable
fun DeviceInfoCard() {
    val deviceInfo = remember { HomeDeviceInfoProvider.collect() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = stringResource(id = R.string.device_info),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        DeviceInfoRow(stringResource(id = R.string.app_version), deviceInfo.appVersion)
        DeviceInfoRow(stringResource(id = R.string.kernel_version), deviceInfo.kernelVersion)
        DeviceInfoRow(stringResource(id = R.string.device_model), deviceInfo.deviceModel)
        DeviceInfoRow(stringResource(id = R.string.vbmeta_status), deviceInfo.vbmetaStatus)
        DeviceInfoRow(stringResource(id = R.string.selinux_status), deviceInfo.selinuxStatus)
    }
}

@Composable
private fun DeviceInfoRow(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                modifier = Modifier.weight(1F),
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                modifier = Modifier.weight(1F),
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun String.removeImageExtension(): String {
    return if (endsWith(".img", ignoreCase = true)) dropLast(4) else this
}
