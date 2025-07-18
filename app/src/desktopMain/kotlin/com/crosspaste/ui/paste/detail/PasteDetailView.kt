package com.crosspaste.ui.paste.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.crosspaste.app.DesktopAppSize
import com.crosspaste.ui.theme.AppUISize.tiny5X
import org.koin.compose.koinInject

@Composable
fun PasteDetailView(
    detailView: @Composable () -> Unit,
    detailInfoView: @Composable () -> Unit,
) {
    val appSize = koinInject<DesktopAppSize>()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .size(appSize.centerSearchWindowDetailViewDpSize)
                    .padding(appSize.searchDetailPaddingValues)
                    .clip(appSize.searchDetailRoundedCornerShape),
        ) {
            detailView()
        }

        HorizontalDivider(thickness = tiny5X)

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(appSize.searchInfoPaddingValues),
        ) {
            detailInfoView()
        }
    }
}
