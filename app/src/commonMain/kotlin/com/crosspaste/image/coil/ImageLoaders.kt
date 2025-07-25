package com.crosspaste.image.coil

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.crosspaste.app.AppFileType
import com.crosspaste.app.AppSize
import com.crosspaste.image.FaviconLoader
import com.crosspaste.image.FileExtImageLoader
import com.crosspaste.image.GenerateImageService
import com.crosspaste.image.ThumbnailLoader
import com.crosspaste.path.UserDataPathProvider

class ImageLoaders(
    private val appSize: AppSize,
    private val faviconLoader: FaviconLoader,
    private val fileExtLoader: FileExtImageLoader,
    private val thumbnailLoader: ThumbnailLoader,
    private val generateImageService: GenerateImageService,
    platformContext: PlatformContext,
    userDataPathProvider: UserDataPathProvider,
) {
    private val html2ImageCache = "html2ImageCache"
    private val baseCache = "baseCache"

    private val html2ImageTempPath =
        userDataPathProvider.resolve(
            fileName = html2ImageCache,
            appFileType = AppFileType.TEMP,
        )

    private val baseTempPath =
        userDataPathProvider.resolve(
            fileName = baseCache,
            appFileType = AppFileType.TEMP,
        )

    private val memoryCache =
        MemoryCache
            .Builder()
            .strongReferencesEnabled(false)
            .maxSizeBytes(48L * 1024L * 1024L)
            .build()

    private val htmlDiskCache =
        DiskCache
            .Builder()
            .directory(html2ImageTempPath)
            .maxSizeBytes(64L * 1024L * 1024L)
            .build()

    private val baseDiskCache =
        DiskCache
            .Builder()
            .directory(baseTempPath)
            .maxSizeBytes(64L * 1024L * 1024L)
            .build()

    val generateImageLoader =
        ImageLoader
            .Builder(platformContext)
            .components {
                add(GenerateImageFactory(appSize, generateImageService))
                    .add(GenerateImageKeyer())
            }.memoryCache {
                memoryCache
            }.diskCache {
                htmlDiskCache
            }.build()

    val faviconImageLoader =
        ImageLoader
            .Builder(platformContext)
            .components {
                add(FaviconFactory(faviconLoader))
                    .add(PasteDataKeyer())
            }.memoryCache {
                memoryCache
            }.diskCache {
                baseDiskCache
            }.build()

    val fileExtImageLoader =
        ImageLoader
            .Builder(platformContext)
            .components {
                add(FileExtFactory(fileExtLoader))
                    .add(FileExtKeyer())
            }.memoryCache {
                memoryCache
            }.diskCache {
                baseDiskCache
            }.build()

    val appSourceLoader =
        ImageLoader
            .Builder(platformContext)
            .components {
                add(AppSourceFactory(userDataPathProvider))
                    .add(PasteDataSourceKeyer())
            }.memoryCache {
                memoryCache
            }.diskCache {
                baseDiskCache
            }.build()

    val userImageLoader =
        ImageLoader
            .Builder(platformContext)
            .components {
                add(UserImageFactory(thumbnailLoader))
                    .add(ImageKeyer())
            }.memoryCache {
                memoryCache
            }.diskCache {
                baseDiskCache
            }.build()
}
