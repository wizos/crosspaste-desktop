package com.crosspaste.ui.devices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crosspaste.app.AppControl
import com.crosspaste.config.ConfigManager
import com.crosspaste.db.sync.SyncRuntimeInfo.Companion.createSyncRuntimeInfo
import com.crosspaste.db.sync.SyncRuntimeInfoDao
import com.crosspaste.dto.sync.SyncInfo
import com.crosspaste.i18n.GlobalCopywriter
import com.crosspaste.sync.NearbyDeviceManager
import com.crosspaste.sync.SyncManager
import com.crosspaste.ui.theme.CrossPasteTheme.connectedColor
import com.crosspaste.ui.theme.CrossPasteTheme.disconnectedColor
import com.crosspaste.utils.getJsonUtils
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun NearbyDeviceView(syncInfo: SyncInfo) {
    val appControl = koinInject<AppControl>()
    val copywriter = koinInject<GlobalCopywriter>()
    val nearbyDeviceManager = koinInject<NearbyDeviceManager>()
    val deviceViewProvider = koinInject<DeviceViewProvider>()
    val syncRuntimeInfoDao = koinInject<SyncRuntimeInfoDao>()
    val syncManager = koinInject<SyncManager>()
    val configManager = koinInject<ConfigManager>()
    val jsonUtils = getJsonUtils()
    val scope = rememberCoroutineScope()

    val config by configManager.config.collectAsState()

    deviceViewProvider.SyncDeviceView(syncInfo = syncInfo) { background ->
        Button(
            modifier = Modifier.height(28.dp),
            onClick = {
                if (appControl.isDeviceConnectionEnabled(syncManager.getSyncHandlers().size + 1)) {
                    val newSyncRuntimeInfo = createSyncRuntimeInfo(syncInfo)
                    syncRuntimeInfoDao.insertOrUpdateSyncRuntimeInfo(newSyncRuntimeInfo)
                }
            },
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, connectedColor(background)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
            elevation =
                ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    hoveredElevation = 0.dp,
                    focusedElevation = 0.dp,
                ),
        ) {
            Text(
                text = copywriter.getText("add"),
                color = connectedColor(background),
                style = MaterialTheme.typography.labelMedium,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            modifier = Modifier.height(28.dp),
            onClick = {
                val blackSyncInfos: MutableList<SyncInfo> =
                    jsonUtils.JSON.decodeFromString(
                        config.blacklist,
                    )
                for (blackSyncInfo in blackSyncInfos) {
                    if (blackSyncInfo.appInfo.appInstanceId == syncInfo.appInfo.appInstanceId) {
                        return@Button
                    }
                }
                blackSyncInfos.add(syncInfo)
                val newBlackList = jsonUtils.JSON.encodeToString(blackSyncInfos)
                configManager.updateConfig("blacklist", newBlackList)
                scope.launch {
                    nearbyDeviceManager.refresh()
                }
            },
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, disconnectedColor(background)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
            elevation =
                ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    hoveredElevation = 0.dp,
                    focusedElevation = 0.dp,
                ),
        ) {
            Text(
                text = copywriter.getText("block"),
                color = disconnectedColor(background),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}
