globalThis.chrome = globalThis.browser;

import { debugMode } from '../utils/debug.js';
import store from '../npm/hybrids/src/store.js';

const TRUSTED_DOMAINS_NONE_ID = "<none>";
const ManagedConfig = {
  disableOnboarding: false,
  disableUserControl: false,
  disableUserAccount: false,
  disableTrackersPreview: false,
  trustedDomains: [TRUSTED_DOMAINS_NONE_ID],
  disableNotifications: (config) => config.disableOnboarding || config.disableUserControl,
  disableModes: (config) => config.disableOnboarding || config.disableUserControl || config.trustedDomains[0] !== TRUSTED_DOMAINS_NONE_ID,
  [store.connect]: async () => {
    try {
      let managedConfig = await chrome.storage.managed.get().catch((e) => {
        if (debugMode) return {};
        throw e;
      });
      if (debugMode) {
        if (Object.keys(managedConfig).length) {
          chrome.storage.local.set({ managedConfig });
        } else {
          const { managedConfig: fallbackConfig } = await chrome.storage.local.get("managedConfig");
          managedConfig = fallbackConfig;
        }
      }
      return managedConfig || {};
    } catch {
      return {};
    }
  }
};

export { TRUSTED_DOMAINS_NONE_ID, ManagedConfig as default };
