globalThis.chrome = globalThis.browser;

import { getOS } from './browser-info.js';

async function openTabWithUrl(host, event) {
  if (!chrome.tabs || getOS() === "android") {
    event.currentTarget.target = "_blank";
    setTimeout(() => window.close(), 50);
    return;
  }
  const { href } = event.currentTarget;
  event.preventDefault();
  try {
    const tabs = await chrome.tabs.query({
      url: href.split("#")[0],
      currentWindow: true
    });
    if (tabs.length) {
      await chrome.tabs.update(tabs[0].id, {
        active: true,
        url: href !== tabs[0].url ? href : void 0
      });
      window.close();
      return;
    }
  } catch (e) {
    console.error("[utils|tabs] Error while try to find existing tab:", e);
  }
  chrome.tabs.create({ url: href });
  window.close();
}
async function getCurrentTab() {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
  return tab || null;
}

export { getCurrentTab, openTabWithUrl };
