package com.crosspaste.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import com.crosspaste.i18n.GlobalCopywriter
import com.crosspaste.listen.DesktopShortcutKeys.Companion.PASTE
import com.crosspaste.listen.DesktopShortcutKeys.Companion.PASTE_LOCAL_LAST
import com.crosspaste.listen.DesktopShortcutKeys.Companion.PASTE_PLAIN_TEXT
import com.crosspaste.listen.DesktopShortcutKeys.Companion.PASTE_PRIMARY_TYPE
import com.crosspaste.listen.DesktopShortcutKeys.Companion.PASTE_REMOTE_LAST
import com.crosspaste.listen.DesktopShortcutKeys.Companion.SHOW_MAIN
import com.crosspaste.listen.DesktopShortcutKeys.Companion.SHOW_SEARCH
import com.crosspaste.listen.DesktopShortcutKeys.Companion.TOGGLE_ENCRYPT
import com.crosspaste.listen.DesktopShortcutKeys.Companion.TOGGLE_PASTEBOARD_MONITORING
import com.crosspaste.listener.KeyboardKey
import com.crosspaste.listener.ShortcutKeys
import com.crosspaste.listener.ShortcutKeysListener
import com.crosspaste.ui.base.DialogButtonsView
import com.crosspaste.ui.base.DialogService
import com.crosspaste.ui.base.KeyboardView
import com.crosspaste.ui.base.PasteDialogFactory
import com.crosspaste.ui.base.edit
import org.koin.compose.koinInject

@Composable
fun ShortcutKeysContentView() {
    val scrollState = rememberScrollState()

    Box(
        modifier =
            Modifier.fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier.verticalScroll(scrollState)
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
        ) {
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                ShortcutKeyRow(PASTE)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                ShortcutKeyRow(PASTE_PLAIN_TEXT)

                HorizontalDivider(modifier = Modifier.padding(start = 15.dp))

                ShortcutKeyRow(PASTE_PRIMARY_TYPE)

                HorizontalDivider(modifier = Modifier.padding(start = 15.dp))

                ShortcutKeyRow(PASTE_LOCAL_LAST)

                HorizontalDivider(modifier = Modifier.padding(start = 15.dp))

                ShortcutKeyRow(PASTE_REMOTE_LAST)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                ShortcutKeyRow(SHOW_MAIN)

                HorizontalDivider(modifier = Modifier.padding(start = 15.dp))

                ShortcutKeyRow(SHOW_SEARCH)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                ShortcutKeyRow(TOGGLE_PASTEBOARD_MONITORING)

                HorizontalDivider(modifier = Modifier.padding(start = 15.dp))

                ShortcutKeyRow(TOGGLE_ENCRYPT)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShortcutKeyRow(name: String) {
    val copywriter = koinInject<GlobalCopywriter>()
    val dialogService = koinInject<DialogService>()
    val pasteDialogFactory = koinInject<PasteDialogFactory>()
    val shortcutKeys = koinInject<ShortcutKeys>()

    var hover by remember { mutableStateOf(false) }

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .height(40.dp)
                .onPointerEvent(
                    eventType = PointerEventType.Enter,
                    onEvent = {
                        hover = true
                    },
                ).onPointerEvent(
                    eventType = PointerEventType.Exit,
                    onEvent = {
                        hover = false
                    },
                )
                .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsText(text = copywriter.getText(name))
        Spacer(modifier = Modifier.weight(1f))

        val shortcutKeysListener = koinInject<ShortcutKeysListener>()

        Icon(
            modifier =
                Modifier.size(16.dp)
                    .clickable {
                        dialogService.pushDialog(
                            pasteDialogFactory.createDialog(
                                key = name,
                                title = "please_directly_enter_the_new_shortcut_key_you_wish_to_set",
                            ) {
                                DisposableEffect(Unit) {
                                    shortcutKeysListener.editShortcutKeysMode = true
                                    onDispose {
                                        shortcutKeysListener.currentKeys.clear()
                                        shortcutKeysListener.editShortcutKeysMode = false
                                    }
                                }

                                Column(
                                    modifier =
                                        Modifier.fillMaxWidth()
                                            .wrapContentHeight(),
                                ) {
                                    Row(
                                        modifier =
                                            Modifier.fillMaxWidth()
                                                .height(40.dp)
                                                .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(5.dp)),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                modifier =
                                                    Modifier.size(16.dp),
                                                painter = edit(),
                                                contentDescription = "edit shortcut key",
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            ShortcutKeyItemView(shortcutKeysListener.currentKeys)
                                        }
                                    }
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        DialogButtonsView(
                                            cancelAction = {
                                                dialogService.popDialog()
                                            },
                                            confirmAction = {
                                                if (name != PASTE || shortcutKeysListener.currentKeys.isNotEmpty()) {
                                                    shortcutKeys.update(name, shortcutKeysListener.currentKeys)
                                                }
                                                shortcutKeysListener.currentKeys.clear()
                                                dialogService.popDialog()
                                            },
                                        )
                                    }
                                }
                            },
                        )
                    },
            painter = edit(),
            contentDescription = "edit shortcut key",
            tint =
                if (hover) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
        )

        Spacer(modifier = Modifier.width(10.dp))

        shortcutKeys.shortcutKeysCore.value.keys[name]?.let { keys ->
            ShortcutKeyItemView(keys)
        } ?: run {
            Text(
                text = copywriter.getText("unassigned"),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
fun ShortcutKeyItemView(keys: List<KeyboardKey>) {
    Row(
        modifier = Modifier.wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        keys.forEachIndexed { index, info ->
            KeyboardView(keyboardValue = info.name, backgroundColor = MaterialTheme.colorScheme.primaryContainer)
            if (index != keys.size - 1) {
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "+",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
        }
    }
}
