package com.crosspaste.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.crosspaste.ui.base.ExpandView
import com.crosspaste.ui.base.database

@Composable
actual fun StoreSettingsView() {
    ExpandView(
        title = "store",
        icon = { database() },
        iconTintColor = Color(0xFF4CD964),
    ) {
        StoreSettingsContentView()
    }
}
