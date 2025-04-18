package com.crosspaste.ui.devices

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crosspaste.db.sync.SyncRuntimeInfo
import com.crosspaste.db.sync.SyncRuntimeInfoDao
import com.crosspaste.ui.base.CustomTextField
import com.crosspaste.ui.base.DialogButtonsView
import com.crosspaste.ui.base.DialogService
import com.crosspaste.ui.base.PasteDialogFactory
import org.koin.compose.koinInject

@Composable
fun MyDevicesView(syncRuntimeInfos: List<SyncRuntimeInfo>) {
    val dialogService = koinInject<DialogService>()
    val pasteDialogFactory = koinInject<PasteDialogFactory>()
    Box(contentAlignment = Alignment.TopCenter) {
        DevicesListView(syncRuntimeInfos) { syncRuntimeInfo ->
            dialogService.pushDialog(
                pasteDialogFactory.createDialog(
                    key = syncRuntimeInfo.deviceId,
                    title = "input_note_name",
                ) {
                    val syncRuntimeInfoDao = koinInject<SyncRuntimeInfoDao>()
                    var inputNoteName by remember { mutableStateOf("") }
                    var isError by remember { mutableStateOf(false) }

                    val focusRequester = remember { FocusRequester() }

                    val cancelAction = {
                        dialogService.popDialog()
                    }

                    val confirmAction = {
                        if (inputNoteName == "") {
                            isError = true
                        } else {
                            syncRuntimeInfoDao.updateNoteName(
                                syncRuntimeInfo.appInstanceId,
                                inputNoteName,
                            )
                            dialogService.popDialog()
                        }
                    }

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }

                    Column(
                        modifier =
                            Modifier.fillMaxWidth()
                                .wrapContentHeight(),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            CustomTextField(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .height(40.dp)
                                        .focusRequester(focusRequester)
                                        .onKeyEvent {
                                            when (it.key) {
                                                Key.Enter -> {
                                                    confirmAction()
                                                    true
                                                }
                                                Key.Escape -> {
                                                    cancelAction()
                                                    true
                                                }
                                                else -> {
                                                    false
                                                }
                                            }
                                        },
                                value = inputNoteName,
                                onValueChange = { inputNoteName = it },
                                placeholder = {
                                    Text(
                                        modifier = Modifier.wrapContentSize(),
                                        text = syncRuntimeInfo.noteName ?: syncRuntimeInfo.deviceName,
                                        style =
                                            MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Light,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                            ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                isError = isError,
                                singleLine = true,
                                colors =
                                    TextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        disabledTextColor = Color.Transparent,
                                        errorTextColor = MaterialTheme.colorScheme.error,
                                        cursorColor = MaterialTheme.colorScheme.primary,
                                        errorCursorColor = Color.Red,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                                        disabledIndicatorColor = Color.Transparent,
                                        errorIndicatorColor = MaterialTheme.colorScheme.error,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        errorContainerColor = Color.Transparent,
                                        focusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                        disabledPlaceholderColor = Color.Transparent,
                                        errorPlaceholderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                    ),
                                textStyle =
                                    TextStyle(
                                        fontWeight = FontWeight.Light,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 15.sp,
                                        lineHeight = 5.sp,
                                    ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            )
                        }

                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .wrapContentHeight(),
                        ) {
                            DialogButtonsView(
                                cancelAction = cancelAction,
                                confirmAction = confirmAction,
                            )
                        }
                    }
                },
            )
        }
    }
}

@Composable
fun DevicesListView(
    syncRuntimeInfos: List<SyncRuntimeInfo>,
    onEdit: (SyncRuntimeInfo) -> Unit,
) {
    val deviceViewProvider = koinInject<DeviceViewProvider>()
    Column(modifier = Modifier.fillMaxWidth()) {
        for ((index, syncRuntimeInfo) in syncRuntimeInfos.withIndex()) {
            deviceViewProvider.DeviceConnectView(syncRuntimeInfo, true, onEdit)
            if (index != syncRuntimeInfos.size - 1) {
                HorizontalDivider()
            }
        }
    }
}
