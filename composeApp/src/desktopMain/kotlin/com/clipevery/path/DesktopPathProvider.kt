package com.clipevery.path

import com.clipevery.app.AppEnv
import com.clipevery.platform.currentPlatform
import com.clipevery.utils.FileUtils
import com.clipevery.utils.getFileUtils
import com.clipevery.utils.getResourceUtils
import com.clipevery.utils.getSystemProperty
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass

object DesktopPathProvider : PathProvider {

    private val pathProvider = getPathProvider()

    override val fileUtils: FileUtils = getFileUtils()

    override val userHome: Path = pathProvider.userHome

    override val clipAppPath: Path = pathProvider.clipAppPath

    override val clipAppJarPath: Path = pathProvider.clipAppJarPath

    override val clipUserPath: Path = pathProvider.clipUserPath

    override val clipLogPath: Path get() = pathProvider.clipLogPath

    override val clipEncryptPath get() = pathProvider.clipEncryptPath

    override val clipDataPath get() = pathProvider.clipDataPath

    @Suppress("UNCHECKED_CAST")
    private fun getPathProvider(): PathProvider {
        val appEnv = AppEnv.getAppEnv()
        return if (appEnv == AppEnv.DEVELOPMENT) {
            DevelopmentPathProvider()
        } else if (appEnv == AppEnv.TEST) {
            val kClass = Class.forName("com.clipevery.path.TestPathProvider").kotlin as KClass<PathProvider>
            kClass.objectInstance ?: throw IllegalStateException("Expected a singleton instance")
        } else {
            if (currentPlatform().isWindows()) {
                WindowsPathProvider()
            } else if (currentPlatform().isMacos()) {
                MacosPathProvider()
            } else if (currentPlatform().isLinux()) {
                LinuxPathProvider()
            } else {
                throw IllegalStateException("Unknown platform: ${currentPlatform().name}")
            }
        }
    }
}

class DevelopmentPathProvider : PathProvider {

    private val systemProperty = getSystemProperty()

    private val composeAppDir = systemProperty.get("user.dir")

    private val resourceUtils = getResourceUtils()

    private val development = resourceUtils.loadProperties("development.properties")

    override val clipAppPath: Path = getAppPath()

    override val clipAppJarPath: Path = getAppPath()

    override val clipUserPath: Path = getUserPath()

    override val fileUtils: FileUtils = getFileUtils()

    override val userHome: Path = Paths.get(systemProperty.get("user.home"))

    private fun getAppPath(): Path {
        development.getProperty("clipAppPath")?.let {
            val path = Paths.get(it)
            if (path.isAbsolute) {
                return path
            } else {
                return Paths.get(composeAppDir).resolve(it)
            }
        } ?: run {
            return Paths.get(composeAppDir)
        }
    }

    private fun getUserPath(): Path {
        development.getProperty("clipUserPath")?.let {
            val path = Paths.get(it)
            if (path.isAbsolute) {
                return path
            } else {
                return Paths.get(composeAppDir).resolve(it)
            }
        } ?: run {
            return Paths.get(composeAppDir).resolve(".user")
        }
    }
}

class WindowsPathProvider : PathProvider {

    private val systemProperty = getSystemProperty()

    override val userHome: Path = Paths.get(systemProperty.get("user.home"))

    override val clipAppPath: Path = getAppJarPath().parent

    override val clipAppJarPath: Path = getAppJarPath()

    override val clipUserPath: Path = userHome.resolve(".clipevery")

    override val fileUtils: FileUtils = getFileUtils()

    private fun getAppJarPath(): Path {
        systemProperty.getOption("compose.application.resources.dir")?.let {
            return Paths.get(it)
        }
        systemProperty.getOption("skiko.library.path")?.let {
            return Paths.get(it)
        }
        throw IllegalStateException("Could not find app path")
    }
}

class MacosPathProvider : PathProvider {

    /**
     * .
     * ├── Info.plist
     * ├── MacOS
     * ├── PkgInfo
     * ├── Resources
     * ├── _CodeSignature
     * ├── app
     * └── runtime
     */
    private val systemProperty = getSystemProperty()

    override val userHome: Path = Paths.get(systemProperty.get("user.home"))

    override val clipAppPath: Path = getAppJarPath().parent.parent

    override val clipAppJarPath: Path = getAppJarPath()

    override val clipUserPath: Path = getAppSupportPath()

    override val clipLogPath: Path = getLogPath()

    override val fileUtils: FileUtils = getFileUtils()

    private fun getAppSupportPath(): Path {
        val appSupportPath =
            userHome.resolve("Library")
                .resolve("Application Support")
                .resolve("Clipevery")

        if (Files.notExists(appSupportPath)) {
            Files.createDirectories(appSupportPath)
        }

        return appSupportPath
    }

    private fun getLogPath(): Path {
        val appLogsPath =
            userHome
                .resolve("Library")
                .resolve("Logs")
                .resolve("Clipevery")

        if (Files.notExists(appLogsPath)) {
            Files.createDirectories(appLogsPath)
        }

        return appLogsPath
    }

    private fun getAppJarPath(): Path {
        systemProperty.getOption("compose.application.resources.dir")?.let {
            return Paths.get(it)
        }
        systemProperty.getOption("skiko.library.path")?.let {
            return Paths.get(it)
        }
        throw IllegalStateException("Could not find app path")
    }
}

class LinuxPathProvider : PathProvider {

    private val systemProperty = getSystemProperty()

    override val userHome: Path = Paths.get(systemProperty.get("user.home"))

    override val clipAppPath: Path = getAppJarPath().parent.parent

    override val clipAppJarPath: Path = getAppJarPath()

    override val clipUserPath: Path = userHome.resolve(".local").resolve("shard").resolve(".clipevery")

    override val fileUtils: FileUtils = getFileUtils()

    private fun getAppJarPath(): Path {
        systemProperty.getOption("compose.application.resources.dir")?.let {
            return Paths.get(it)
        }
        systemProperty.getOption("skiko.library.path")?.let {
            return Paths.get(it)
        }
        throw IllegalStateException("Could not find app path")
    }
}
