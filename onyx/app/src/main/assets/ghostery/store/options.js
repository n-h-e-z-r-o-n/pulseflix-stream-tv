globalThis.chrome = globalThis.browser;

import { DEFAULT_REGIONS } from '../utils/regions.js';
import CustomFilters from './custom-filters.js';
import ManagedConfig, { TRUSTED_DOMAINS_NONE_ID } from './managed-config.js';
import Notification from './notification.js';
import store from '../npm/hybrids/src/store.js';

const UPDATE_OPTIONS_ACTION_NAME = "updateOptions";
const GLOBAL_PAUSE_ID = "<all_urls>";
const MODE_DEFAULT = "default";
const MODE_ZAP = "zap";
const ENGINES = [
  { name: "ads", key: "blockAds" },
  { name: "tracking", key: "blockTrackers" },
  { name: "annoyances", key: "blockAnnoyances" }
];
const LOCAL_OPTIONS = [
  "autoconsent",
  "terms",
  "feedback",
  "onboarding",
  "panel",
  "sync",
  "revision",
  "filtersUpdatedAt",
  "fixesFilters",
  "whatsNewVersion"
];
const PROTECTED_OPTIONS = ["exceptions", "paused", "zapped"];
const OPTIONS_VERSION = 4;
const Options = {
  // Mode
  mode: MODE_DEFAULT,
  // 'default' | 'zap'
  // Main features
  blockAds: true,
  blockTrackers: true,
  blockAnnoyances: true,
  // Regional filters
  regionalFilters: {
    enabled: DEFAULT_REGIONS.length > 0,
    regions: DEFAULT_REGIONS.length ? DEFAULT_REGIONS : [String]
  },
  // Advanced features
  customFilters: {
    enabled: false,
    trustedScriptlets: false
  },
  // Experimental features
  autoconsent: { autoAction: "optOut" },
  experimentalFilters: false,
  fixesFilters: true,
  // SERP protection
  serpTrackingPrevention: true,
  // Redirect protection
  redirectProtection: {
    enabled: false,
    exceptions: store.record(true)
  },
  // WhoTracks.Me
  wtmSerpReport: true,
  trackerWheel: false,
  ...{ trackerCount: true } ,
  pauseAssistant: true,
  // Onboarding
  terms: false,
  feedback: true,
  onboarding: false,
  // UI
  panel: { statsType: "graph", notifications: true },
  theme: "",
  // Tracker exceptions
  exceptions: store.record({ global: false, domains: [String] }),
  // Paused domains (ghostery filtering mode)
  paused: store.record({ revokeAt: 0, assist: false, managed: false }),
  // Zapped domains (zap filtering mode)
  zapped: store.record(true),
  // Sync & Update
  sync: true,
  revision: 0,
  // Filters update timestamp
  filtersUpdatedAt: 0,
  // What's new
  whatsNewVersion: 0,
  [store.connect]: {
    async get() {
      let { options = {}, optionsVersion } = await chrome.storage.local.get([
        "options",
        "optionsVersion"
      ]);
      if (!optionsVersion) {
        chrome.storage.local.set({ optionsVersion: OPTIONS_VERSION });
      } else if (optionsVersion < OPTIONS_VERSION) {
        await migrate(options, optionsVersion);
      }
      {
        return manage(options);
      }
    },
    async set(_, options) {
      options = options || {};
      await chrome.storage.local.set({
        options: (
          // Firefox does not serialize correctly objects with getters
          JSON.parse(JSON.stringify(options)) 
        )
      });
      await chrome.runtime.sendMessage({
        action: UPDATE_OPTIONS_ACTION_NAME
      }).catch(() => {
      });
      return options;
    }
  }
};
const SYNC_OPTIONS = Object.keys(Options).filter(
  (key) => !LOCAL_OPTIONS.includes(key)
);
const REPORT_OPTIONS = [
  ...SYNC_OPTIONS.filter((key) => !PROTECTED_OPTIONS.includes(key)),
  "filtersUpdatedAt"
];
chrome.runtime.onMessage.addListener((msg) => {
  if (msg.action === UPDATE_OPTIONS_ACTION_NAME) {
    store.clear(Options, false);
    store.get(Options);
  }
});
async function migrate(options, optionsVersion) {
  if (optionsVersion < 2) {
    if (options.paused) {
      options.paused = options.paused.reduce((acc, { id, revokeAt }) => {
        acc[id] = { revokeAt };
        return acc;
      }, {});
    }
    console.debug("[options] Migrated to version 2:", options);
  }
  if (optionsVersion < 3) {
    const { text } = await store.resolve(CustomFilters);
    if (text) {
      options.customFilters = {
        ...options.customFilters,
        enabled: true
      };
    }
    console.debug("[options] Migrated to version 3:", options);
  }
  if (optionsVersion < 4) {
    if (options.onboarding) {
      if (options.onboarding.pinIt) {
        await store.set(Notification, { id: "pin-it", shown: 1 });
      }
      if (options.onboarding.serpShown) {
        await store.set(Notification, {
          id: "opera-serp",
          shown: options.onboarding.serpShown,
          lastShownAt: options.onboarding.serpShownAt
        });
      }
      options.onboarding = !!options.onboarding.shown;
    }
  }
  await chrome.storage.local.set({
    options,
    optionsVersion: OPTIONS_VERSION
  });
  console.log(
    `[options] Migrated to version ${OPTIONS_VERSION} from version ${optionsVersion}...`
  );
}
async function manage(options) {
  const managed = await store.resolve(ManagedConfig);
  if (managed.disableOnboarding === true) {
    options.terms = true;
    options.onboarding = true;
  }
  if (managed.disableUserControl === true) {
    options.sync = false;
    options.paused = {};
  } else {
    if (options.paused) {
      for (const domain of Object.keys(options.paused)) {
        if (options.paused[domain].managed === true) {
          delete options.paused[domain];
        }
      }
    }
  }
  if (managed.disableUserAccount === true) {
    options.sync = false;
  }
  if (managed.disableTrackersPreview === true) {
    options.wtmSerpReport = false;
  }
  if (managed.trustedDomains[0] !== TRUSTED_DOMAINS_NONE_ID) {
    options.paused ||= {};
    managed.trustedDomains.forEach((domain) => {
      options.paused[domain] = { revokeAt: 0, managed: true };
    });
  }
  return options;
}
function findParentDomain(record, hostname = "") {
  if (!hostname) return null;
  const domain = Object.keys(record).sort((a, b) => b.localeCompare(a)).find((d) => hostname.endsWith(d));
  return domain || null;
}
function isGloballyPaused(options) {
  return !!options.paused[GLOBAL_PAUSE_ID];
}
async function revokeGlobalPause(options) {
  await store.set(options, { paused: { [GLOBAL_PAUSE_ID]: null } });
}
function getPausedDetails(options, hostname) {
  if (!hostname) {
    throw new Error("Hostname is required to get paused details");
  }
  if (isGloballyPaused(options)) {
    return { revokeAt: 0 };
  }
  switch (options.mode) {
    case MODE_DEFAULT: {
      const pausedHostname = findParentDomain(options.paused, hostname);
      return pausedHostname ? options.paused[pausedHostname] : null;
    }
    case MODE_ZAP: {
      const zappedHostname = findParentDomain(options.zapped, hostname);
      return zappedHostname ? null : { revokeAt: 0 };
    }
    default:
      return null;
  }
}

export { ENGINES, GLOBAL_PAUSE_ID, MODE_DEFAULT, MODE_ZAP, REPORT_OPTIONS, SYNC_OPTIONS, Options as default, findParentDomain, getPausedDetails, isGloballyPaused, revokeGlobalPause };
