globalThis.chrome = globalThis.browser;

import { FLAG_MODES } from '../npm/@ghostery/config/dist/esm/flags.js';
import Config from '../store/config.js';
import Options, { MODE_DEFAULT } from '../store/options.js';
import { getDynamicRulesIds, PAUSED_ID_RANGE } from '../utils/dnr.js';
import '../utils/options-observer.js';
import store from '../npm/hybrids/src/store.js';

store.observe(Config, async (_, config, lastConfig) => {
  if (lastConfig?.hasFlag(FLAG_MODES) && !config.hasFlag(FLAG_MODES)) {
    const removeRuleIds = await getDynamicRulesIds(PAUSED_ID_RANGE);
    await chrome.declarativeNetRequest.updateDynamicRules({
      removeRuleIds
    });
    await store.set(Options, {
      mode: MODE_DEFAULT,
      zapped: null
    });
    console.log(
      `[zapped] Filtering mode flag removed, resetting filtering mode and zapped data`
    );
  }
});
