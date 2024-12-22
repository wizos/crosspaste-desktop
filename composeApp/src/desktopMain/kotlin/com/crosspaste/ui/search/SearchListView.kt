package com.crosspaste.ui.search

import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.crosspaste.app.DesktopAppWindowManager
import com.crosspaste.ui.base.PasteTitleView
import com.crosspaste.ui.model.PasteSearchViewModel
import com.crosspaste.ui.model.PasteSelectionViewModel
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SearchListView(setSelectedIndex: (Int) -> Unit) {
    val appWindowManager = koinInject<DesktopAppWindowManager>()
    val pasteSearchViewModel = koinInject<PasteSearchViewModel>()
    val pasteSelectionViewModel = koinInject<PasteSelectionViewModel>()
    val searchListState = rememberLazyListState()
    val adapter = rememberScrollbarAdapter(scrollState = searchListState)
    var showScrollbar by remember { mutableStateOf(false) }
    var scrollJob: Job? by remember { mutableStateOf(null) }

    val coroutineScope = rememberCoroutineScope()

    val searchResult by pasteSearchViewModel.searchResults.collectAsState()

    val selectedIndex by pasteSelectionViewModel.selectedIndex.collectAsState()

    val showSearchWindow by appWindowManager.showSearchWindow.collectAsState()

    LaunchedEffect(showSearchWindow, searchResult) {
        if (showSearchWindow) {
            pasteSelectionViewModel.setSelectedIndex(0)
            delay(32)
            searchListState.animateScrollToItem(0, 0)
        }
    }

    LaunchedEffect(selectedIndex, showSearchWindow) {
        if (showSearchWindow) {
            val visibleItems = searchListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                val lastIndex = visibleItems.last().index

                if (lastIndex < selectedIndex) {
                    searchListState.animateScrollToItem(selectedIndex - 9)
                } else if (visibleItems.first().index > selectedIndex) {
                    searchListState.animateScrollToItem(selectedIndex)
                }

                if (searchResult.size - lastIndex <= 10) {
                    pasteSearchViewModel.tryAddLimit()
                }
            }
        }
    }

    val latestSearchResult = rememberUpdatedState(searchResult)

    LaunchedEffect(searchListState) {
        snapshotFlow {
            searchListState.firstVisibleItemIndex to searchListState.layoutInfo.visibleItemsInfo
        }.distinctUntilChanged().collect { (_, visibleItems) ->
            if (visibleItems.isNotEmpty()) {
                if (latestSearchResult.value.size - visibleItems.last().index <= 10) {
                    pasteSearchViewModel.tryAddLimit()
                }
            }

            val shouldShowScrollbar = latestSearchResult.value.size > 10
            if (shouldShowScrollbar) {
                scrollJob?.cancel()
                scrollJob =
                    coroutineScope.launch(CoroutineName("HiddenScroll")) {
                        delay(500)
                        showScrollbar = false
                    }
            }
            showScrollbar = shouldShowScrollbar
        }
    }

    LaunchedEffect(searchResult.size) {
        if (searchResult.size > 10) {
            showScrollbar = true
        }
    }

    Box(modifier = Modifier.width(280.dp).height(420.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(
                state = searchListState,
                modifier = Modifier.width(280.dp).height(400.dp),
            ) {
                itemsIndexed(
                    searchResult,
                    key = { _, item -> item.id },
                ) { index, pasteData ->
                    PasteTitleView(pasteData, index == selectedIndex) {
                        setSelectedIndex(index)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        VerticalScrollbar(
            modifier =
                Modifier.background(color = Color.Transparent)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state =
                            rememberDraggableState { delta ->
                                coroutineScope.launch(CoroutineName("ScrollPaste")) {
                                    searchListState.scrollBy(-delta)
                                }
                            },
                    ),
            adapter = adapter,
            style =
                ScrollbarStyle(
                    minimalHeight = 16.dp,
                    thickness = 8.dp,
                    shape = RoundedCornerShape(4.dp),
                    hoverDurationMillis = 300,
                    unhoverColor =
                        if (showScrollbar) {
                            MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.48f)
                        } else {
                            Color.Transparent
                        },
                    hoverColor = MaterialTheme.colorScheme.inverseSurface,
                ),
        )
    }
}
