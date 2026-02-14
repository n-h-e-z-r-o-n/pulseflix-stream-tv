globalThis.chrome = globalThis.browser;

import DailyStats from '../store/daily-stats.js';
import store from '../npm/hybrids/src/store.js';

async function clearCookiesForDomain(domain) {
  if (!domain) {
    console.warn("No domain provided for cookie cleaning");
    return 0;
  }
  try {
    const cookies = await chrome.cookies.getAll({ domain });
    let removed = 0;
    for (const cookie of cookies) {
      const url = `http${cookie.secure ? "s" : ""}://${cookie.domain.replace(/^\./, "")}${cookie.path}`;
      try {
        await chrome.cookies.remove({
          url,
          name: cookie.name,
          storeId: cookie.storeId
        });
        removed++;
        console.debug(
          `[cookies] Removed cookie ${cookie.name} for domain ${domain}`
        );
      } catch (error) {
        console.error(
          `[cookies] Failed to remove cookie ${cookie.name}:`,
          error
        );
      }
    }
    const dailyStats = await store.resolve(
      DailyStats,
      (/* @__PURE__ */ new Date()).toISOString().split("T")[0]
    );
    store.set(dailyStats, {
      cookiesRemoved: dailyStats.cookiesRemoved + removed
    });
    console.log(`[cookies] Removed ${removed} cookies for domain: ${domain}`);
    return removed;
  } catch (error) {
    console.error("[cookies] Error cleaning cookies:", error);
    throw error;
  }
}
{
  chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => {
    if (msg.action === "cookies:clean") {
      clearCookiesForDomain(msg.domain).then((removed) => {
        sendResponse({ success: true, removed });
      }).catch((error) => {
        sendResponse({ success: false, error: error.message });
      });
      return true;
    }
  });
}
