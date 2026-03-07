package com.kirillNay.telegram.miniapp.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import com.kirillNay.telegram.miniapp.webApp.EventType
import com.kirillNay.telegram.miniapp.webApp.webApp
import kotlinx.browser.document

/**
 * Use [telegramWebApp] to provide Compose content of mini app.
 *
 * @param rootElementId the id of the root HTML element to mount Compose into. Defaults to "root".
 * @param content is compose content of your mini app.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun telegramWebApp(
    rootElementId: String = "root",
    content: @Composable (TelegramStyle) -> Unit
) {
    val root = document.getElementById(rootElementId) ?: document.body!!
    ComposeViewport(root) {
        var paddings by remember { mutableStateOf(ViewPort(webApp.viewportHeight.dp, webApp.viewportStableHeight.dp)) }
        var colors by remember { mutableStateOf(TelegramColors.fromWebApp()) }

        LaunchedEffect(true) {
            webApp.addEventHandler(EventType.VIEWPORT_CHANGED) {
                paddings = ViewPort(webApp.viewportHeight.dp, webApp.viewportStableHeight.dp)
            }

            webApp.addEventHandler(EventType.THEME_CHANGED) {
                colors = TelegramColors.fromWebApp()
            }
        }

        content(TelegramStyle(paddings, colors))
    }
}