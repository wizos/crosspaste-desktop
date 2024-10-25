package com.crosspaste.paste

import com.crosspaste.app.AppInfo
import com.crosspaste.paste.plugin.process.PasteProcessPlugin
import com.crosspaste.paste.plugin.type.PasteTypePlugin
import com.crosspaste.realm.paste.PasteRealm
import com.crosspaste.utils.LoggerExtension.logSuspendExecutionTime
import io.github.oshai.kotlinlogging.KotlinLogging

class DesktopTransferableConsumer(
    private val appInfo: AppInfo,
    private val pasteRealm: PasteRealm,
    private val idGenerator: PasteIDGenerator,
    private val pasteProcessPlugins: List<PasteProcessPlugin>,
    pasteTypePlugins: List<PasteTypePlugin>,
) : TransferableConsumer {

    override val logger = KotlinLogging.logger {}

    private val pasteTypePluginMap: Map<String, PasteTypePlugin> =
        pasteTypePlugins.flatMap { pasteTypePlugin ->
            pasteTypePlugin.getIdentifiers().map { it to pasteTypePlugin }
        }.toMap()

    private fun createDataFlavorMap(pasteTransferable: PasteTransferable): Map<String, List<PasteDataFlavor>> {
        val dataFlavorMap = LinkedHashMap<String, MutableList<PasteDataFlavor>>()
        pasteTransferable as DesktopReadTransferable
        for (flavor in pasteTransferable.transferable.transferDataFlavors) {
            val humanPresentableName = flavor.humanPresentableName
            if (!dataFlavorMap.containsKey(humanPresentableName)) {
                dataFlavorMap[humanPresentableName] = mutableListOf()
            }
            dataFlavorMap[humanPresentableName]?.add(flavor.toPasteDataFlavor())
        }
        return dataFlavorMap
    }

    override suspend fun consume(
        pasteTransferable: PasteTransferable,
        source: String?,
        remote: Boolean,
    ) {
        logSuspendExecutionTime(logger, "consume") {
            val pasteId = idGenerator.nextID()

            val dataFlavorMap: Map<String, List<PasteDataFlavor>> = createDataFlavorMap(pasteTransferable)

            dataFlavorMap[LocalOnlyFlavor.humanPresentableName]?.let {
                logger.info { "Ignoring local only flavor" }
                return@logSuspendExecutionTime
            }

            val pasteCollector = PasteCollector(dataFlavorMap.size, appInfo, pasteRealm, pasteProcessPlugins)

            try {
                preCollect(pasteId, dataFlavorMap, pasteTransferable, pasteCollector)
                pasteCollector.createPrePasteData(pasteId, source, remote = remote)?.let {
                    updatePasteData(pasteId, dataFlavorMap, pasteTransferable, pasteCollector)
                    pasteCollector.completeCollect(it)
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to consume transferable" }
            }
        }
    }

    override fun getPlugin(identity: String): PasteTypePlugin? {
        return pasteTypePluginMap[identity]
    }
}
