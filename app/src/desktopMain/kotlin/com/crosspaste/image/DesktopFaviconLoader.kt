package com.crosspaste.image

import com.crosspaste.net.DesktopProxy
import com.crosspaste.path.UserDataPathProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import okio.Path
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class DesktopFaviconLoader(
    userDataPathProvider: UserDataPathProvider,
) : AbstractFaviconLoader(userDataPathProvider) {

    override val logger = KotlinLogging.logger {}

    override fun saveIco(
        url: String,
        path: Path,
    ): Path? {
        val uri = URI(url)

        val proxy = DesktopProxy.getProxy(uri)

        return runCatching {
            val builder = HttpClient.newBuilder()

            (proxy.address() as InetSocketAddress?)?.let { address ->
                builder.proxy(ProxySelector.of(address))
            }

            val client = builder.build()

            val request =
                HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(5))
                    .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())

            if (response.statusCode() == 200) {
                FileOutputStream(path.toFile()).use { output ->
                    response.body().use { input ->
                        input.copyTo(output)
                    }
                }
                path
            } else {
                logger.warn { "Failed to save favicon for $url" }
                null
            }
        }.getOrElse { e ->
            logger.warn(e) { "Failed to save favicon for $url" }
            null
        }
    }
}
