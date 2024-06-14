package com.clipevery.app

import kotlinx.coroutines.runBlocking

class TestWindowManager(
    private val mockOS: MockOS,
) : AbstractAppWindowManager() {

    var prevApp: String? = null

    var pasterId: Int = 0

    var currentTitle: String? = null

    override fun getPrevAppName(): String? {
        return prevApp
    }

    override fun getCurrentActiveAppName(): String? {
        return mockOS.currentApp
    }

    override fun activeMainWindow() {
        showMainWindow = true
        bringToFront(MAIN_WINDOW_TITLE)
    }

    override fun unActiveMainWindow() {
        runBlocking {
            bringToBack(MAIN_WINDOW_TITLE, false)
        }
        showMainWindow = false
    }

    override suspend fun activeSearchWindow() {
        showSearchWindow = true

        bringToFront(SEARCH_WINDOW_TITLE)
    }

    override suspend fun unActiveSearchWindow(preparePaste: suspend () -> Boolean) {
        val toPaste = preparePaste()
        bringToBack(SEARCH_WINDOW_TITLE, toPaste)
        showSearchWindow = false
    }

    private fun bringToFront(windowTitle: String) {
        currentTitle = windowTitle
        if (mockOS.currentApp != "Clipevery") {
            prevApp = mockOS.currentApp
        }
        mockOS.currentApp = "Clipevery"
    }

    private suspend fun bringToBack(
        windowTitle: String,
        toPaste: Boolean,
    ) {
        currentTitle = windowTitle
        mockOS.currentApp = prevApp
        if (toPaste) {
            toPaste()
        }
    }

    override suspend fun toPaste() {
        pasterId++
    }
}

class MockOS(var currentApp: String? = null)
