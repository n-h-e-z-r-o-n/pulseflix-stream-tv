globalThis.chrome = globalThis.browser;

import '../npm/tldts-experimental/npm/tldts-core/dist/es6/src/options.js';
import '../npm/@ghostery/adblocker/dist/esm/data-view.js';
import '../npm/@ghostery/adblocker/dist/esm/fetch.js';
import CosmeticFilter from '../npm/@ghostery/adblocker/dist/esm/filters/cosmetic.js';
import { detectFilterType, FilterType, parseFilters } from '../npm/@ghostery/adblocker/dist/esm/lists.js';
import '../npm/@ghostery/adblocker/dist/esm/request.js';
import '../npm/@remusao/small/dist/esm/index.js';
import '../npm/@ghostery/adblocker/dist/esm/filters/network.js';
import '../npm/@ghostery/adblocker/dist/esm/preprocessor.js';
import { init, create, CUSTOM_ENGINE } from '../utils/engines.js';
import { addListener } from '../utils/options-observer.js';
import Options from '../store/options.js';
import CustomFilters from '../store/custom-filters.js';
import { setup, reloadMainEngine } from './adblocker.js';
import './redirect-protection.js';
import store from '../npm/hybrids/src/store.js';

class TrustedScriptletError extends Error {
}
function fixScriptlet(filter, trustedScriptlets) {
  const cosmeticFilter = CosmeticFilter.parse(filter);
  if (!cosmeticFilter || !cosmeticFilter.isScriptInject() || !cosmeticFilter.selector) {
    return null;
  }
  const parsedScript = cosmeticFilter.parseScript();
  if (!parsedScript || !parsedScript.name) {
    return null;
  }
  if (!trustedScriptlets && (parsedScript.name === "rpnt" || parsedScript.name === "replace-node-text" || parsedScript.name.startsWith("trusted-"))) {
    throw new TrustedScriptletError();
  }
  const [front] = filter.split(`#+js(${parsedScript.name}`);
  const args = parsedScript.args.map((arg) => encodeURIComponent(arg));
  return `${front}#+js(${[parsedScript.name, ...args].join(", ")})`;
}
function normalizeFilters(text = "", { trustedScriptlets }) {
  const rows = text.split("\n").map((f) => f.trim());
  return rows.reduce(
    (filters, filter, index) => {
      if (!filter) return filters;
      const filterType = detectFilterType(filter, {
        extendedNonSupportedTypes: true
      });
      if (filterType === FilterType.NETWORK) {
        filters.networkFilters.add(filter);
      } else if (filterType === FilterType.COSMETIC) {
        try {
          const scriptlet = fixScriptlet(filter, trustedScriptlets);
          filters.cosmeticFilters.add(scriptlet || filter);
        } catch (e) {
          if (e instanceof TrustedScriptletError) {
            filters.errors.push(
              `Trusted scriptlets are not allowed (${index + 1}): ${filter}`
            );
          } else {
            console.error(e);
          }
        }
      } else if (filterType === FilterType.NOT_SUPPORTED || filterType === FilterType.NOT_SUPPORTED_ADGUARD) {
        filters.errors.push(`Filter not supported (${index + 1}): ${filter}`);
      }
      return filters;
    },
    {
      networkFilters: /* @__PURE__ */ new Set(),
      cosmeticFilters: /* @__PURE__ */ new Set(),
      errors: []
    }
  );
}
async function updateEngine(text) {
  const { networkFilters, cosmeticFilters, preprocessors } = parseFilters(text);
  await create(CUSTOM_ENGINE, {
    cosmeticFilters,
    networkFilters,
    preprocessors
  });
  console.info(
    `[custom filters] Engine updated with network filters: ${networkFilters.length}, cosmetic filters: ${cosmeticFilters.length}`
  );
  return {
    networkFilters: networkFilters.length,
    cosmeticFilters: cosmeticFilters.length
  };
}
async function updateCustomFilters(input, options) {
  setup.pending && await setup.pending;
  const { networkFilters, cosmeticFilters, errors } = normalizeFilters(
    input,
    options
  );
  const result = await updateEngine(
    [
      ...networkFilters ,
      ...cosmeticFilters
    ].join("\n")
  );
  result.errors = errors;
  await reloadMainEngine();
  return result;
}
addListener("customFilters", async (value, lastValue) => {
  if (!lastValue && value.enabled && !await init(CUSTOM_ENGINE)) {
    const { text } = await store.resolve(CustomFilters);
    await updateCustomFilters(text, value);
  }
});
chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => {
  if (msg.action === "customFilters:update") {
    store.resolve(Options).then((options) => {
      updateCustomFilters(msg.input, options.customFilters).then(sendResponse);
    });
    return true;
  }
});

export { updateCustomFilters };
