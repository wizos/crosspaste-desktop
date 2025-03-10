package com.crosspaste.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.crosspaste.notification.MessageType
import com.crosspaste.notification.ToastManager
import com.crosspaste.notification.getMessagePainter
import com.crosspaste.utils.ColorUtils
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

data class Toast(val messageType: MessageType, val message: String, val duration: Long? = 3000)

@Composable
fun ToastView(
    toast: Toast,
    onCancelTapped: () -> Unit,
) {
    val density = LocalDensity.current
    val toastManager = koinInject<ToastManager>()

    val messageStyle by remember {
        mutableStateOf(toast.messageType.getMessageStyle())
    }

    LaunchedEffect(Unit) {
        if ((toast.duration ?: 0) > 0) {
            delay(toast.duration!!)
            toastManager.cancel()
        }
    }
    Popup(
        alignment = Alignment.TopCenter,
        offset =
            IntOffset(
                with(density) { (0.dp).roundToPx() },
                with(density) { (50.dp).roundToPx() },
            ),
        properties = PopupProperties(clippingEnabled = false),
    ) {
        Box(
            modifier =
                Modifier
                    .wrapContentSize()
                    .background(Color.Transparent)
                    .shadow(15.dp),
        ) {
            val background =
                if (toast.messageType == MessageType.Error) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                }

            val tint =
                ColorUtils.getAdaptiveColor(
                    background,
                    messageStyle.baseColor.targetHue,
                )

            Row(
                modifier =
                    Modifier
                        .background(background, shape = RoundedCornerShape(8.dp))
                        .padding(all = 8.dp)
                        .width(280.dp)
                        .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    painter = getMessagePainter(messageStyle),
                    contentDescription = "toast icon",
                    tint = tint,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = toast.message,
                    style =
                        TextStyle(
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.contentColorFor(background),
                            fontSize = 16.sp,
                        ),
                )
                Spacer(Modifier.width(12.dp))
                Icon(
                    modifier = Modifier.clickable(onClick = onCancelTapped),
                    painter = close(),
                    contentDescription = "Cancel",
                    tint = tint,
                )
            }
        }
    }
}
