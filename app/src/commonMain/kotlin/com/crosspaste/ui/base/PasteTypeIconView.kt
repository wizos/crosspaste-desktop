package com.crosspaste.ui.base

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.PlatformContext
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.crosspaste.db.paste.PasteData
import com.crosspaste.image.coil.FileExtItem
import com.crosspaste.image.coil.ImageLoaders
import com.crosspaste.image.coil.PasteDataItem
import com.crosspaste.paste.item.PasteFiles
import com.crosspaste.path.UserDataPathProvider
import com.crosspaste.utils.safeIsDirectory
import org.koin.compose.koinInject

@Composable
fun PasteTypeIconView(
    pasteData: PasteData,
    padding: Dp = 2.dp,
    size: Dp = 20.dp,
) {
    val iconStyle = koinInject<IconStyle>()
    val imageLoaders = koinInject<ImageLoaders>()
    val platformContext = koinInject<PlatformContext>()
    val userDataPathProvider = koinInject<UserDataPathProvider>()

    val pasteType = pasteData.getType()

    if (pasteType.isUrl()) {
        SubcomposeAsyncImage(
            modifier = Modifier.padding(padding).size(size),
            model =
                ImageRequest.Builder(platformContext)
                    .data(PasteDataItem(pasteData))
                    .crossfade(false)
                    .build(),
            imageLoader = imageLoaders.faviconImageLoader,
            contentDescription = "Paste Icon",
            content = {
                when (this.painter.state.collectAsState().value) {
                    is AsyncImagePainter.State.Loading,
                    is AsyncImagePainter.State.Error,
                    -> {
                        Icon(
                            painter = link(),
                            contentDescription = "Paste Icon",
                            modifier = Modifier.padding(padding).size(size),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    else -> {
                        SubcomposeAsyncImageContent()
                    }
                }
            },
        )
    } else if (pasteType.isFile()) {
        pasteData.getPasteItem(PasteFiles::class)?.let {
            val paths = it.getFilePaths(userDataPathProvider)
            if (paths.isNotEmpty()) {
                val isSingleFile = paths.size == 1 && !paths[0].safeIsDirectory
                if (isSingleFile) {
                    SubcomposeAsyncImage(
                        modifier = Modifier.padding(padding).size(size),
                        model =
                            ImageRequest.Builder(platformContext)
                                .data(FileExtItem(paths[0]))
                                .crossfade(false)
                                .build(),
                        imageLoader = imageLoaders.fileExtImageLoader,
                        contentDescription = "Paste Icon",
                        content = {
                            when (this.painter.state.collectAsState().value) {
                                is AsyncImagePainter.State.Loading,
                                is AsyncImagePainter.State.Error,
                                -> {
                                    Icon(
                                        painter = file(),
                                        contentDescription = "Paste Icon",
                                        modifier = Modifier.padding(padding).size(size),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }

                                else -> {
                                    SubcomposeAsyncImageContent()
                                }
                            }
                        },
                    )
                } else {
                    Icon(
                        painter = folder(),
                        contentDescription = "folder",
                        modifier = Modifier.padding(padding).size(size),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                Icon(
                    painter = fileSlash(),
                    contentDescription = "fileSlash",
                    modifier = Modifier.padding(padding).size(size),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    } else if (!pasteType.isHtml() && !pasteType.isRtf()) {
        Icon(
            painter = pasteType.IconPainter(),
            contentDescription = "Paste Icon",
            modifier = Modifier.padding(padding).size(size),
            tint = MaterialTheme.colorScheme.primary,
        )
    } else {
        pasteData.source?.let {
            val isMacStyleIcon by remember(it) { mutableStateOf(iconStyle.isMacStyleIcon(it)) }
            var imageSize by remember(it) { mutableStateOf(if (isMacStyleIcon) size + 2.dp else (size + 2.dp) / 24 * 20) }
            var imagePaddingSize by remember(it) { mutableStateOf(if (isMacStyleIcon) 0.dp else (size + 2.dp) / 24 * 2) }

            SubcomposeAsyncImage(
                modifier = Modifier.padding(imagePaddingSize).size(imageSize),
                model =
                    ImageRequest.Builder(platformContext)
                        .data(PasteDataItem(pasteData))
                        .crossfade(false)
                        .build(),
                imageLoader = imageLoaders.appSourceLoader,
                contentDescription = "Paste Icon",
                content = {
                    val state = this.painter.state.collectAsState().value
                    when (state) {
                        is AsyncImagePainter.State.Loading,
                        is AsyncImagePainter.State.Error,
                        -> {
                            imageSize = (size + 2.dp) / 24 * 20
                            imagePaddingSize = (size + 2.dp) / 24 * 2
                            Icon(
                                painter = htmlOrRtf(),
                                contentDescription = "Paste Icon",
                                modifier = Modifier.padding(imagePaddingSize).size(imageSize),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        else -> {
                            SubcomposeAsyncImageContent()
                        }
                    }
                },
            )
        } ?: run {
            Icon(
                painter = htmlOrRtf(),
                contentDescription = "Paste Icon",
                modifier = Modifier.padding(padding).size(size),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
