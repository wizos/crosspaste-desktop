package com.crosspaste.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.crosspaste.app.AppExitService
import com.crosspaste.app.AppFileChooser
import com.crosspaste.app.AppRestartService
import com.crosspaste.app.AppSize
import com.crosspaste.app.ExitMode
import com.crosspaste.app.FileSelectionMode
import com.crosspaste.config.CommonConfigManager
import com.crosspaste.i18n.GlobalCopywriter
import com.crosspaste.notification.MessageType
import com.crosspaste.notification.NotificationManager
import com.crosspaste.path.DesktopMigration
import com.crosspaste.path.UserDataPathProvider
import com.crosspaste.ui.LocalExitApplication
import com.crosspaste.ui.base.CustomSwitch
import com.crosspaste.ui.base.CustomTextField
import com.crosspaste.ui.base.DialogButtonsView
import com.crosspaste.ui.base.DialogService
import com.crosspaste.ui.base.PasteDialogFactory
import com.crosspaste.ui.base.archive
import com.crosspaste.ui.theme.AppUISize.large2X
import com.crosspaste.ui.theme.AppUISize.medium
import com.crosspaste.ui.theme.AppUISize.small2X
import com.crosspaste.ui.theme.AppUISize.tiny
import com.crosspaste.ui.theme.AppUISize.tiny2X
import com.crosspaste.ui.theme.AppUISize.tiny3X
import com.crosspaste.ui.theme.DesktopAppUIFont.StorePathTextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.Path
import org.koin.compose.koinInject

@Composable
fun SetStoragePathView() {
    val appFileChooser = koinInject<AppFileChooser>()
    val appSize = koinInject<AppSize>()
    val configManager = koinInject<CommonConfigManager>()
    val copywriter = koinInject<GlobalCopywriter>()
    val dialogService = koinInject<DialogService>()
    val desktopMigration = koinInject<DesktopMigration>()
    val notificationManager = koinInject<NotificationManager>()
    val pasteDialogFactory = koinInject<PasteDialogFactory>()
    val userDataPathProvider = koinInject<UserDataPathProvider>()

    val config by configManager.config.collectAsState()

    Column(
        modifier =
            Modifier
                .wrapContentSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        SettingItemsTitleView("storage_directory")

        var useDefaultStoragePath by remember { mutableStateOf(config.useDefaultStoragePath) }

        val currentStoragePath by remember(config) {
            mutableStateOf(
                userDataPathProvider.getUserDataPath(),
            )
        }

        if (useDefaultStoragePath) {
            SettingItemView(
                painter = archive(),
                text = "use_default_storage_path",
            ) {
                CustomSwitch(
                    modifier =
                        Modifier
                            .width(medium * 2)
                            .height(large2X),
                    checked = useDefaultStoragePath,
                    onCheckedChange = {
                        useDefaultStoragePath = !useDefaultStoragePath
                    },
                )
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(appSize.settingsItemHeight)
                    .padding(horizontal = small2X, vertical = tiny2X),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CustomTextField(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clickable {
                            val action: (Path) -> Unit = { path ->
                                dialogService.pushDialog(
                                    pasteDialogFactory.createDialog(
                                        key = "storagePath",
                                        title = "determining_the_new_storage_path",
                                    ) {
                                        SetStoragePathDialogView(path)
                                    },
                                )
                            }
                            val errorAction: (String) -> Unit = { message ->
                                notificationManager.sendNotification(
                                    title = { it.getText(message) },
                                    messageType = MessageType.Error,
                                    duration = null,
                                )
                            }
                            val chooseText = copywriter.getText("selecting_storage_directory")
                            appFileChooser.openFileChooser(
                                FileSelectionMode.DIRECTORY_ONLY,
                                chooseText,
                                currentStoragePath,
                            ) { path ->

                                desktopMigration.checkMigrationPath(path as Path)?.let { errorMessage ->
                                    errorAction(errorMessage)
                                } ?: run {
                                    action(path)
                                }
                            }
                        },
                value = currentStoragePath.toString(),
                onValueChange = {},
                enabled = useDefaultStoragePath,
                readOnly = useDefaultStoragePath,
                singleLine = true,
                textStyle = StorePathTextStyle(useDefaultStoragePath),
                colors =
                    TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                contentPadding = PaddingValues(horizontal = tiny),
            )
        }
    }
}

@Composable
fun SetStoragePathDialogView(path: Path) {
    val exitApplication = LocalExitApplication.current

    val appSize = koinInject<AppSize>()
    val dialogService = koinInject<DialogService>()
    val desktopMigration = koinInject<DesktopMigration>()
    val appExitService = koinInject<AppExitService>()
    val appRestartService = koinInject<AppRestartService>()
    var isMigration by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0.0f) }
    val coroutineScope = rememberCoroutineScope()

    val confirmAction = {
        appExitService.beforeExitList.clear()
        appExitService.beforeReleaseLockList.clear()

        appExitService.beforeExitList.add {
            isMigration = true
            coroutineScope.launch {
                while (progress < 0.99f && isMigration) {
                    progress += 0.01f
                    delay(200)
                }
            }
        }

        appExitService.beforeReleaseLockList.add {
            runCatching {
                desktopMigration.migration(path)
                coroutineScope.launch {
                    progress = 1f
                    delay(500)
                    isMigration = false
                }
                isMigration = false
            }.onFailure {
                coroutineScope.launch {
                    isMigration = false
                }
            }
        }
        appRestartService.restart { exitApplication(ExitMode.MIGRATION) }
    }

    val cancelAction = {
        dialogService.popDialog()
    }

    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(appSize.settingsItemHeight),
        ) {
            CustomTextField(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                value = path.toString(),
                onValueChange = {},
                enabled = false,
                readOnly = true,
                singleLine = true,
                textStyle = StorePathTextStyle(false),
                colors =
                    TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                contentPadding = PaddingValues(horizontal = tiny),
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            if (isMigration) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(tiny3X),
                    progress = { progress },
                )
            } else {
                DialogButtonsView(
                    confirmTitle = "migrate_and_then_restart_the_app",
                    cancelAction = cancelAction,
                    confirmAction = confirmAction,
                )
            }
        }
    }
}
