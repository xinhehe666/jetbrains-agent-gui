package com.github.agentgui.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Tests the custom-origin boundary before requests reach the plugin classpath. */
public class WebviewAssetRequestHandlerTest {

    @Test
    public void mapsOnlyWebviewAssetOrigin() {
        String url = WebviewAssetRequestHandler.WEBVIEW_ASSET_ORIGIN
                + "/assets/index-AbCd_123.js";

        assertTrue(WebviewAssetRequestHandler.isWebviewAssetUrl(url));
        assertEquals(
                "/webview/assets/index-AbCd_123.js",
                WebviewAssetRequestHandler.resourcePathForUrl(url));
        assertFalse(WebviewAssetRequestHandler.isWebviewAssetUrl(
                "https://example.com/assets/index.js"));
    }

    @Test
    public void rejectsTraversalAndUnexpectedPaths() {
        assertNull(WebviewAssetRequestHandler.resourcePathForUrl(
                WebviewAssetRequestHandler.WEBVIEW_ASSET_ORIGIN + "/assets/../secret"));
        assertNull(WebviewAssetRequestHandler.resourcePathForUrl(
                WebviewAssetRequestHandler.WEBVIEW_ASSET_ORIGIN + "/index.html"));
        assertNull(WebviewAssetRequestHandler.resourcePathForUrl(
                WebviewAssetRequestHandler.WEBVIEW_ASSET_ORIGIN + "/assets/%2e%2e/secret"));
        assertNull(WebviewAssetRequestHandler.resourcePathForUrl(
                WebviewAssetRequestHandler.WEBVIEW_ASSET_ORIGIN + "/assets/"));
        assertNull(WebviewAssetRequestHandler.resourcePathForUrl(
                WebviewAssetRequestHandler.WEBVIEW_ASSET_ORIGIN + "/assets/path\\file.js"));
        assertNull(WebviewAssetRequestHandler.resourcePathForUrl(null));
        assertNull(WebviewAssetRequestHandler.resourcePathForUrl("not a valid URI"));
    }

    @Test
    public void rejectsOtherOriginsAndCredentials() {
        assertFalse(WebviewAssetRequestHandler.isWebviewAssetUrl(
                "http://agent-gui-webview.local/assets/main.js"));
        assertFalse(WebviewAssetRequestHandler.isWebviewAssetUrl(
                "https://agent-gui-webview.local.example.com/assets/main.js"));
        assertFalse(WebviewAssetRequestHandler.isWebviewAssetUrl(
                "https://agent-gui-webview.local:8443/assets/main.js"));
        assertFalse(WebviewAssetRequestHandler.isWebviewAssetUrl(
                "https://user@agent-gui-webview.local/assets/main.js"));
        assertTrue(WebviewAssetRequestHandler.isWebviewAssetUrl(
                "https://agent-gui-webview.local:443/assets/main.js"));
    }

    @Test
    public void returnsStrictAssetMimeTypes() {
        assertEquals("text/javascript", WebviewAssetRequestHandler.mimeTypeForPath("main.js"));
        assertEquals("text/css", WebviewAssetRequestHandler.mimeTypeForPath("main.css"));
        assertEquals("font/woff2", WebviewAssetRequestHandler.mimeTypeForPath("font.woff2"));
        assertEquals("application/octet-stream",
                WebviewAssetRequestHandler.mimeTypeForPath("unknown.bin"));
    }
}
