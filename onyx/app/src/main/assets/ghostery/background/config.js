globalThis.chrome = globalThis.browser;

import Config from '../store/config.js';
import { CDN_URL } from '../utils/urls.js';
import { debugMode } from '../utils/debug.js';
import store from '../npm/hybrids/src/store.js';

const CONFIG_URL = CDN_URL + "configs/v1.json";
function filter(item) {
  if (item.filter) {
    const { platform } = item.filter;
    let check = true;
    if (check && Array.isArray(platform)) {
      check = platform.includes(
        "firefox"
      );
    }
    return check;
  }
  return true;
}
const HALF_HOUR_IN_MS = 1e3 * 60 * 30;
async function syncConfig() {
  const config = await store.resolve(Config);
  if (config.updatedAt > Date.now() - HALF_HOUR_IN_MS) {
    return;
  }
  try {
    const fetchedConfig = await fetch(CONFIG_URL).then((res) => {
      if (!res.ok) throw new Error("Failed to fetch the remote config");
      return res.json();
    });
    const domains = { ...config.domains };
    for (const name of Object.keys(domains)) {
      if (fetchedConfig.domains[name] === void 0) {
        domains[name] = null;
      }
    }
    for (const [name, item] of Object.entries(fetchedConfig.domains)) {
      domains[name] = filter(item) ? item : null;
    }
    const flags = { ...config.flags };
    for (const name of Object.keys(flags)) {
      if (fetchedConfig.flags[name] === void 0) {
        flags[name] = null;
      }
    }
    for (const [name, items] of Object.entries(fetchedConfig.flags)) {
      const item = items.find((item2) => filter(item2));
      if (!item) {
        flags[name] = null;
        continue;
      }
      const percentage = flags[name]?.percentage ?? Math.floor(Math.random() * 100) + 1;
      flags[name] = {
        percentage,
        enabled: percentage <= item.percentage
      };
    }
    await store.set(Config, { domains, flags, updatedAt: Date.now() });
    console.log("[config] Remote config synced");
  } catch (e) {
    console.error("[config] Failed to sync remote config:", e);
  }
}
if (!debugMode) {
  store.observe(Config, (_, config, lastConfig) => {
    if (!lastConfig || config.updatedAt === 0) syncConfig();
  });
  store.resolve(Config);
}

export { syncConfig as default };
