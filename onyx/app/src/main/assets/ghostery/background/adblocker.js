globalThis.chrome = globalThis.browser;

import { filterRequestHTML, updateResponseHeadersWithCSP } from '../npm/@ghostery/adblocker-webextension/dist/esm/index.js';
import { parse } from '../npm/tldts-experimental/dist/es6/index.js';
import scriptlets from '../npm/@ghostery/scriptlets/index.js';
import { FLAG_EXTENDED_SELECTORS, FLAG_FIREFOX_CONTENT_SCRIPT_SCRIPTLETS, FLAG_INJECTION_TARGET_DOCUMENT_ID } from '../npm/@ghostery/config/dist/esm/flags.js';
import { resolveFlag } from '../store/config.js';
import Options, { ENGINES, getPausedDetails } from '../store/options.js';
import { getStatus } from '../utils/exceptions.js';
import { init, setEnv, FIXES_ENGINE, ELEMENT_PICKER_ENGINE, CUSTOM_ENGINE, replace, create, isPersistentEngine, update, TRACKERDB_ENGINE, MAIN_ENGINE, get } from '../utils/engines.js';
import { setup as setup$1, getMetadata } from '../utils/trackerdb.js';
import { addListener } from '../utils/options-observer.js';
import ExtendedRequest from '../utils/request.js';
import asyncSetup from '../utils/setup.js';
import { updateTabStats, tabStats } from './stats.js';
import { getRedirectProtectionUrl } from './redirect-protection.js';
import store from '../npm/hybrids/src/store.js';

let options = Options;
const contentScripts = /* @__PURE__ */ (() => {
  const map = /* @__PURE__ */ new Map();
  return {
    async register(hostname, code) {
      this.unregister(hostname);
      try {
        const contentScript = await browser.contentScripts.register({
          js: [
            {
              code
            }
          ],
          allFrames: true,
          matches: [`https://*.${hostname}/*`, `http://*.${hostname}/*`],
          matchAboutBlank: true,
          matchOriginAsFallback: true,
          runAt: "document_start",
          world: "MAIN"
        });
        map.set(hostname, contentScript);
      } catch (e) {
        console.warn(e);
        this.unregister(hostname);
      }
    },
    isRegistered(hostname) {
      return map.has(hostname);
    },
    unregister(hostname) {
      const contentScript = map.get(hostname);
      if (contentScript) {
        contentScript.unregister();
        map.delete(hostname);
      }
    },
    unregisterAll() {
      for (const hostname of map.keys()) {
        this.unregister(hostname);
      }
    }
  };
})();
let FIREFOX_CONTENT_SCRIPT_SCRIPTLETS = { enabled: false };
{
  FIREFOX_CONTENT_SCRIPT_SCRIPTLETS = resolveFlag(
    FLAG_FIREFOX_CONTENT_SCRIPT_SCRIPTLETS
  );
}
function getEnabledEngines(config) {
  if (config.terms) {
    const list = ENGINES.filter(({ key }) => config[key]).map(
      ({ name }) => name
    );
    if (config.regionalFilters.enabled) {
      list.push(...config.regionalFilters.regions.map((id) => `lang-${id}`));
    }
    if (config.fixesFilters && list.length) {
      list.push(FIXES_ENGINE);
    }
    list.push(ELEMENT_PICKER_ENGINE);
    if (config.customFilters.enabled) {
      list.push(CUSTOM_ENGINE);
    }
    return list;
  }
  return [];
}
function pause(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
async function reloadMainEngine() {
  await pause(1e3);
  const enabledEngines = getEnabledEngines(options);
  const resolvedEngines = (await Promise.all(
    enabledEngines.map(
      (id) => init(id).catch(() => {
        console.error(`[adblocker] failed to load engine: ${id}`);
        return null;
      }).then((engine) => {
        if (!engine) {
          enabledEngines.splice(enabledEngines.indexOf(id), 1);
        }
        return engine;
      })
    )
  )).filter((engine) => engine);
  if (resolvedEngines.length) {
    replace(MAIN_ENGINE, resolvedEngines);
    console.info(
      `[adblocker] Main engine reloaded with: ${enabledEngines.join(", ")}`
    );
  } else {
    await create(MAIN_ENGINE);
    console.info("[adblocker] Main engine reloaded with no filters");
  }
  if (FIREFOX_CONTENT_SCRIPT_SCRIPTLETS.enabled) {
    contentScripts.unregisterAll();
  }
}
let updating = false;
async function updateEngines({ cache = true } = {}) {
  if (updating) return;
  try {
    updating = true;
    const enabledEngines = getEnabledEngines(options);
    if (enabledEngines.length) {
      let updated = false;
      await Promise.all(
        enabledEngines.filter(isPersistentEngine).map(async (id) => {
          await init(id);
          updated = await update(id, { cache }) || updated;
        })
      );
      setup$1.pending && await setup$1.pending;
      await update(TRACKERDB_ENGINE, { cache });
      await store.set(Options, { filtersUpdatedAt: Date.now() });
      if (updated) await reloadMainEngine();
    }
  } finally {
    updating = false;
  }
}
const UPDATE_ENGINES_DELAY = 60 * 60 * 1e3;
const setup = asyncSetup("adblocker", [
  addListener(
    async function adblockerEngines(value, lastValue) {
      options = value;
      const enabledEngines = getEnabledEngines(value);
      const lastEnabledEngines = lastValue && getEnabledEngines(lastValue);
      if (
        // Reload/mismatched main engine
        !await init(MAIN_ENGINE) || // Enabled engines changed
        lastEnabledEngines && (enabledEngines.length !== lastEnabledEngines.length || enabledEngines.some((id, i) => id !== lastEnabledEngines[i]))
      ) {
        await reloadMainEngine();
      }
      if (options.filtersUpdatedAt < Date.now() - UPDATE_ENGINES_DELAY) {
        await updateEngines();
      }
    }
  ),
  addListener(
    "experimentalFilters",
    async (value, lastValue) => {
      setEnv("env_experimental", value);
      if (lastValue !== void 0 && value) {
        await updateEngines();
      }
    }
  )
]);
resolveFlag(
  FLAG_INJECTION_TARGET_DOCUMENT_ID
);
function resolveInjectionTarget(details) {
  const target = { tabId: details.tabId };
  {
    target.frameIds = [details.frameId];
  }
  return target;
}
const scriptletGlobals = {
  // Request a real extension resource to obtain a dynamic ID to the resource.
  // Redirect resources are defined with `use_dynamic_url` restriction.
  // The dynamic ID is generated per session.
  // refs https://developer.chrome.com/docs/extensions/reference/manifest/web-accessible-resources#manifest_declaration
  warOrigin: chrome.runtime.getURL("/rule_resources/redirects/empty").slice(0, -6)
};
function injectScriptlets(filters, hostname, details) {
  let contentScript = "";
  for (const filter of filters) {
    const parsed = filter.parseScript();
    if (!parsed) {
      console.warn(
        "[adblocker] could not inject script filter:",
        filter.toString()
      );
      continue;
    }
    const scriptletName = `${parsed.name}${parsed.name.endsWith(".js") ? "" : ".js"}`;
    const scriptlet = scriptlets[scriptletName];
    if (!scriptlet) {
      console.warn("[adblocker] unknown scriptlet with name:", scriptletName);
      continue;
    }
    const func = scriptlet.func;
    const args = [
      scriptletGlobals,
      ...parsed.args.map((arg) => decodeURIComponent(arg))
    ];
    if (FIREFOX_CONTENT_SCRIPT_SCRIPTLETS.enabled) {
      contentScript += `(${func.toString()})(...${JSON.stringify(args)});
`;
      continue;
    }
    chrome.scripting.executeScript(
      {
        injectImmediately: true,
        world: chrome.scripting.ExecutionWorld?.MAIN ?? (void 0 ),
        target: resolveInjectionTarget(details),
        func,
        args
      },
      () => {
        if (chrome.runtime.lastError) {
          console.warn(chrome.runtime.lastError);
        }
      }
    );
  }
  if (FIREFOX_CONTENT_SCRIPT_SCRIPTLETS.enabled) {
    if (filters.length === 0) {
      contentScripts.unregister(hostname);
    } else if (!contentScripts.isRegistered(hostname)) {
      contentScripts.register(hostname, contentScript);
    } else ;
  }
}
function injectStyles(styles, details) {
  chrome.scripting.insertCSS({
    css: styles,
    origin: "USER",
    target: resolveInjectionTarget(details)
  }).catch((e) => console.warn("[adblocker] failed to inject CSS", e));
}
const EXTENDED_SELECTORS = resolveFlag(FLAG_EXTENDED_SELECTORS);
async function injectCosmetics(details, config) {
  const { bootstrap: isBootstrap = false, scriptletsOnly } = config;
  try {
    setup.pending && await setup.pending;
  } catch (e) {
    console.error("[adblocker] not ready for cosmetic injection", e);
    return;
  }
  const { frameId, url, tabId } = details;
  const parsed = parse(url);
  const domain = parsed.domain || "";
  const hostname = parsed.hostname || "";
  if (FIREFOX_CONTENT_SCRIPT_SCRIPTLETS.enabled && scriptletsOnly && contentScripts.isRegistered(hostname)) {
    return;
  }
  if (getPausedDetails(options, hostname)) return;
  const tabHostname = tabStats.get(tabId)?.hostname;
  if (tabHostname && getPausedDetails(options, tabHostname)) {
    return;
  }
  const engine = get(MAIN_ENGINE);
  {
    const { matches } = engine.matchCosmeticFilters({
      domain,
      hostname,
      url,
      classes: config.classes,
      hrefs: config.hrefs,
      ids: config.ids,
      // This needs to be done only once per frame
      getInjectionRules: isBootstrap,
      getExtendedRules: isBootstrap,
      getRulesFromHostname: isBootstrap,
      getPureHasRules: true,
      // This will be done every time we get information about DOM mutation
      getRulesFromDOM: !isBootstrap,
      callerContext: { tabId }
    });
    const styleFilters = [];
    const scriptFilters = [];
    for (const { filter, exception } of matches) {
      if (exception === void 0) {
        if (filter.isScriptInject()) {
          scriptFilters.push(filter);
        } else {
          styleFilters.push(filter);
        }
      }
    }
    if (isBootstrap) {
      injectScriptlets(scriptFilters, hostname, details);
    }
    if (scriptletsOnly) {
      return;
    }
    const { styles, extended } = engine.injectCosmeticFilters(styleFilters, {
      url,
      injectScriptlets: isBootstrap,
      injectExtended: isBootstrap,
      injectPureHasSafely: true,
      allowGenericHides: false,
      getBaseRules: false
    });
    if (styles) {
      injectStyles(styles, details);
    }
    if (EXTENDED_SELECTORS.enabled && extended && extended.length > 0) {
      chrome.tabs.sendMessage(
        tabId,
        { action: "evaluateExtendedSelectors", extended },
        { frameId }
      );
    }
  }
  if (isBootstrap) {
    const { styles } = engine.getCosmeticsFilters({
      domain,
      hostname,
      url,
      getBaseRules: true,
      getInjectionRules: false,
      getExtendedRules: false,
      getRulesFromDOM: false,
      getRulesFromHostname: false
    });
    injectStyles(styles, details);
  }
}
chrome.webNavigation.onCommitted.addListener(
  (details) => injectCosmetics(details, { bootstrap: true }),
  { url: [{ urlPrefix: "http://" }, { urlPrefix: "https://" }] }
);
chrome.runtime.onMessage.addListener((msg, sender) => {
  if (msg.action === "injectCosmetics" && sender.tab) {
    const url = !sender.url.startsWith("http") ? sender.origin : sender.url;
    const details = {
      url,
      tabId: sender.tab.id,
      frameId: sender.frameId
    };
    injectCosmetics(details, msg);
  }
});
{
  FIREFOX_CONTENT_SCRIPT_SCRIPTLETS.then((enabled) => {
    if (!enabled) contentScripts.unregisterAll();
  });
  addListener(
    "paused",
    function firefoxContentScriptScriptlets(paused) {
      if (!FIREFOX_CONTENT_SCRIPT_SCRIPTLETS.enabled) return;
      for (const hostname of Object.keys(paused)) {
        contentScripts.unregister(hostname);
      }
    }
  );
  chrome.webNavigation.onBeforeNavigate.addListener(
    (details) => {
      if (FIREFOX_CONTENT_SCRIPT_SCRIPTLETS.enabled) {
        injectCosmetics(details, { bootstrap: true, scriptletsOnly: true });
      }
    },
    { url: [{ urlPrefix: "http://" }, { urlPrefix: "https://" }] }
  );
}
function isTrusted(request, type) {
  if (getPausedDetails(options, request.sourceHostname)) {
    return true;
  }
  if (type === "main_frame") {
    return false;
  }
  return getStatus(
    options,
    // Get exception for known tracker (metadata id) or by the request hostname (unidentified tracker)
    getMetadata(request)?.id || request.hostname,
    request.sourceHostname
  ).trusted;
}
{
  let isExtensionRequest = function(details) {
    return details.tabId === -1 && details.url.startsWith("moz-extension://") || details.originUrl?.startsWith("moz-extension://");
  };
  chrome.webRequest.onBeforeRequest.addListener(
    (details) => {
      if (isExtensionRequest(details)) return;
      if (setup.pending) {
        console.error("[adblocker] not ready for network requests blocking");
        return;
      }
      const request = ExtendedRequest.fromRequestDetails(details);
      let result = void 0;
      if (request.sourceHostname && !isTrusted(request, details.type)) {
        const engine = get(MAIN_ENGINE);
        const { redirect, match } = engine.match(request);
        if (match === true && details.type === "main_frame") {
          const redirectUrl = getRedirectProtectionUrl(
            details.url,
            request.hostname,
            options
          );
          return { redirectUrl };
        } else if (redirect !== void 0) {
          request.blocked = true;
          if (details.type !== "xmlhttprequest") {
            result = {
              redirectUrl: chrome.runtime.getURL(
                "rule_resources/redirects/" + redirect.filename
              )
            };
          } else {
            result = { redirectUrl: redirect.dataUrl };
          }
        } else if (match === true) {
          request.blocked = true;
          result = { cancel: true };
        }
      }
      updateTabStats(details.tabId, [request]);
      return result;
    },
    { urls: ["<all_urls>"] },
    ["blocking"]
  );
  chrome.webRequest.onHeadersReceived.addListener(
    (details) => {
      if (isExtensionRequest(details)) return;
      if (setup.pending) {
        console.error("[adblocker] not ready for network headers modification");
        return;
      }
      const request = ExtendedRequest.fromRequestDetails(details);
      if (isTrusted(request, details.type)) return;
      const engine = get(MAIN_ENGINE);
      const htmlFilters = engine.getHtmlFilters(request);
      if (htmlFilters.length !== 0) {
        request.modified = true;
        updateTabStats(details.tabId, [request]);
        filterRequestHTML(
          chrome.webRequest.filterResponseData,
          request,
          htmlFilters
        );
      }
      if (details.type !== "main_frame") return;
      const cspPolicies = engine.getCSPDirectives(request);
      if (!cspPolicies || cspPolicies.length === 0) return;
      return updateResponseHeadersWithCSP(details, cspPolicies);
    },
    { urls: ["http://*/*", "https://*/*"] },
    ["blocking", "responseHeaders"]
  );
}

export { UPDATE_ENGINES_DELAY, reloadMainEngine, setup, updateEngines };
