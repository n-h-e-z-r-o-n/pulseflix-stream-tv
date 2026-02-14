globalThis.chrome = globalThis.browser;

import Options, { SYNC_OPTIONS } from '../store/options.js';
import debounce from '../utils/debounce.js';
import { addListener, isOptionEqual } from '../utils/options-observer.js';
import store from '../npm/hybrids/src/store.js';

const syncOptions = debounce(
  async function(options, lastOptions) {
    try {
      if (!options.terms || !options.sync) {
        return;
      }
      if (lastOptions && options.revision !== lastOptions.revision) return;
      const keys = lastOptions && SYNC_OPTIONS.filter(
        (key) => !isOptionEqual(options[key], lastOptions[key])
      );
      const { options: serverOptions = {} } = await chrome.storage.sync.get([
        "options"
      ]);
      try {
        if (serverOptions.revision > options.revision) {
          console.info(
            "[sync] Merging server options with revision:",
            serverOptions.revision
          );
          const values = SYNC_OPTIONS.reduce(
            (acc, key) => {
              if (!keys?.includes(key) && hasOwnProperty.call(serverOptions, key)) {
                acc[key] = serverOptions[key];
              }
              return acc;
            },
            { revision: serverOptions.revision }
          );
          options = await store.set(Options, values);
        }
      } catch (e) {
        console.error(`[sync] Error while merging server options: `, e);
      }
      if (keys?.length || !serverOptions.revision) {
        options = await store.set(Options, { revision: options.revision + 1 });
        await chrome.storage.sync.set({
          options: SYNC_OPTIONS.reduce(
            (acc, key) => {
              acc[key] = options[key];
              return acc;
            },
            { revision: options.revision }
          )
        });
        console.info("[sync] Options synced with revision:", options.revision);
      }
    } catch (e) {
      console.error(`[sync] Error while syncing options: `, e);
    }
  },
  // Avoid syncing twice with fast toggling an option
  { waitFor: 200 }
);
{
  addListener(function sync(options, lastOptions) {
    syncOptions(options, lastOptions);
  });
  chrome.storage.sync.onChanged.addListener((changes) => {
    if (changes.options) {
      store.resolve(Options).then((options) => {
        if (changes.options.newValue?.revision > options.revision) {
          console.log("[sync] Options changed:", changes.options);
          syncOptions(options);
        }
      });
    }
  });
  chrome.runtime.onMessage.addListener((msg) => {
    if (msg.action === "syncOptions") {
      store.resolve(Options).then((options) => syncOptions(options));
    }
  });
}
