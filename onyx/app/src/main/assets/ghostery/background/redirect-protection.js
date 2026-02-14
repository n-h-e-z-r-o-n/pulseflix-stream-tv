globalThis.chrome = globalThis.browser;

import AutoSyncingMap from '../utils/map.js';
import '../utils/options-observer.js';

const REDIRECT_PROTECTION_PAGE_URL = chrome.runtime.getURL(
  "pages/redirect-protection/index.html"
);
new AutoSyncingMap({
  storageKey: "redirectUrls:v1",
  ttlInMs: 5 * 60 * 1e3
});
const allowedRedirectUrls = /* @__PURE__ */ new Set();
function getRedirectProtectionUrl(url, hostname, options) {
  if (allowedRedirectUrls.has(url)) {
    allowedRedirectUrls.delete(url);
    return void 0;
  }
  if (!options.redirectProtection.enabled) {
    return void 0;
  }
  if (Object.keys(options.redirectProtection.exceptions).some(
    (domain) => hostname.endsWith(domain)
  )) {
    return void 0;
  }
  return REDIRECT_PROTECTION_PAGE_URL + "?url=" + btoa(url);
}
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.action === "allowRedirect") {
    if (!message.url) {
      sendResponse({ success: false, error: "Missing URL" });
      return false;
    }
    {
      allowedRedirectUrls.add(message.url);
      sendResponse({ success: true });
      return false;
    }
  }
  return false;
});

export { getRedirectProtectionUrl };
