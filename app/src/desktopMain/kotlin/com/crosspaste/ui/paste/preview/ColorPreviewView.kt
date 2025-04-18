package com.crosspaste.ui.paste.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.crosspaste.db.paste.PasteData
import com.crosspaste.paste.item.ColorPasteItem
import com.crosspaste.ui.base.UISupport
import org.koin.compose.koinInject

@Composable
fun ColorPreviewView(pasteData: PasteData) {
    pasteData.getPasteItem(ColorPasteItem::class)?.let { pasteColor ->
        SimplePreviewContentView(pasteData) {
            val uiSupport = koinInject<UISupport>()
            Row(
                modifier =
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                uiSupport.openColorPicker(pasteData)
                            },
                        )
                    },
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(pasteColor.color).copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(80.dp)
                                .shadow(
                                    elevation = 1.dp,
                                    shape = RoundedCornerShape(5.dp),
                                    spotColor = Color.Black.copy(alpha = 0.1f),
                                )
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(pasteColor.color)),
                    )
                }

                Column(
                    modifier =
                        Modifier.fillMaxHeight()
                            .wrapContentWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = pasteColor.toHexString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pasteColor.toRGBAString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}
