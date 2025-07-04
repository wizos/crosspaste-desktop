package com.crosspaste.ui.paste.side.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.crosspaste.db.paste.PasteData
import com.crosspaste.i18n.GlobalCopywriter
import com.crosspaste.image.DesktopIconColorExtractor
import com.crosspaste.ui.base.SidePasteTypeIconView
import com.crosspaste.ui.theme.AppUIColors
import com.crosspaste.ui.theme.AppUIColors.sidePasteTitle
import com.crosspaste.ui.theme.AppUISize.huge
import com.crosspaste.ui.theme.AppUISize.medium
import com.crosspaste.ui.theme.DesktopAppUIFont
import org.koin.compose.koinInject

@Composable
fun SidePasteLayoutView(
    pasteData: PasteData,
    pasteBottomContent: @Composable () -> Unit,
    pasteContent: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SidePasteTitleView(pasteData)
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(AppUIColors.pasteBackground),
            contentAlignment = Alignment.Center,
        ) {
            pasteContent()
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                pasteBottomContent()
            }
        }
    }
}

@Composable
fun SidePasteTitleView(pasteData: PasteData) {
    val copywriter = koinInject<GlobalCopywriter>()
    val desktopIconColorExtractor = koinInject<DesktopIconColorExtractor>()
    val color = sidePasteTitle

    var background by remember { mutableStateOf(color) }

    LaunchedEffect(Unit) {
        pasteData.source?.let {
            desktopIconColorExtractor.getBackgroundColor(it)?.let { color -> background = color }
        }
    }

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .height(huge)
                .background(background)
                .padding(start = medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                Modifier.fillMaxHeight()
                    .wrapContentWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = copywriter.getText(pasteData.getTypeText()),
                style =
                    DesktopAppUIFont.sidePasteTitleTextStyle.copy(
                        color =
                            MaterialTheme.colorScheme.contentColorFor(
                                AppUIColors.importantColor,
                            ),
                    ),
            )
        }
        Spacer(Modifier.weight(1f))
        SidePasteTypeIconView(
            modifier = Modifier.fillMaxHeight().wrapContentWidth(),
            pasteData = pasteData,
            tint =
                MaterialTheme.colorScheme.contentColorFor(
                    AppUIColors.importantColor,
                ),
            background = AppUIColors.importantColor,
        )
    }
}
