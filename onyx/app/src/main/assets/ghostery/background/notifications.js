globalThis.chrome = globalThis.browser;

import { FLAG_NOTIFICATION_REVIEW } from '../npm/@ghostery/config/dist/esm/flags.js';
import Config from '../store/config.js';
import ManagedConfig from '../store/managed-config.js';
import Notification from '../store/notification.js';
import Options from '../store/options.js';
import { debugMode } from '../utils/debug.js';
import { CLOSE_ACTION, OPEN_ACTION, UNMOUNT_ACTION, MOUNT_ACTION } from '../utils/notifications.js';
import { checkStorage } from '../utils/storage.js';
import { getStorage } from './telemetry/index.js';
import './onboarding.js';
import store from '../npm/hybrids/src/store.js';

async function openNotification({
  id,
  tabId,
  shownLimit = 0,
  delay,
  params,
  position
}) {
  const options = await store.resolve(Options);
  const managedConfig = await store.resolve(ManagedConfig);
  const notification = await store.resolve(Notification, id).catch(() => null);
  if (
    // Terms not accepted
    !options.terms || // Disabled notifications in managed config
    managedConfig.disableNotifications || // Shown limit set and reached
    shownLimit > 0 && notification?.shown >= shownLimit || // Delay set and notification shown recently
    delay && notification?.lastShownAt && Date.now() - notification.lastShownAt < delay
  ) {
    return false;
  }
  const url = chrome.runtime.getURL(`/pages/notifications/${id}.html`) + (params ? `?${Object.entries(params).map(([key, value]) => `${key}=${encodeURIComponent(value)}`).join("&")}` : "");
  try {
    await checkStorage();
    const mounted = await chrome.tabs.sendMessage(tabId, {
      action: MOUNT_ACTION,
      url,
      position,
      debug: debugMode
    });
    if (mounted) {
      await store.set(Notification, {
        id,
        shown: (notification?.shown || 0) + 1,
        lastShownAt: Date.now()
      });
      console.log(
        `[notifications] Opened notification "${id}" with params:`,
        params
      );
    }
    return mounted;
  } catch (e) {
    console.error(
      `[notifications] Failed to open notification "${id}" in tab:`,
      tabId,
      e
    );
    return false;
  }
}
function closeNotification(tabId) {
  return chrome.tabs.sendMessage(tabId, {
    action: UNMOUNT_ACTION
  });
}
chrome.runtime.onMessage.addListener((msg, sender) => {
  const tabId = sender.tab?.id;
  if (!tabId) return;
  switch (msg.action) {
    case OPEN_ACTION: {
      openNotification({ tabId, ...msg });
      break;
    }
    case CLOSE_ACTION: {
      closeNotification(tabId);
      break;
    }
  }
});
const REVIEW_NOTIFICATION_DELAY = 30 * 24 * 60 * 60 * 1e3;
chrome.webNavigation.onCompleted.addListener(async (details) => {
  if (details.frameId !== 0) return;
  const { installDate } = await getStorage();
  if (!installDate) return;
  const config = await store.resolve(Config);
  if (!config.hasFlag(FLAG_NOTIFICATION_REVIEW)) return;
  if (debugMode || Date.now() - new Date(installDate).getTime() >= REVIEW_NOTIFICATION_DELAY) {
    openNotification({
      id: "review",
      tabId: details.tabId,
      shownLimit: 1,
      position: "center"
    });
  }
});

export { closeNotification, openNotification };
