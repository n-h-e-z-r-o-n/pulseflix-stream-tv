globalThis.chrome = globalThis.browser;

import Options, { MODE_DEFAULT } from '../store/options.js';
import { addListener } from '../utils/options-observer.js';
import '../store/managed-config.js';
import store from '../npm/hybrids/src/store.js';

const PAUSED_ALARM_PREFIX = "options:revoke";
addListener(async function pausedSites(options, lastOptions) {
  if (options.mode !== MODE_DEFAULT) {
    if (lastOptions && options.mode !== lastOptions?.mode) {
      (await chrome.alarms.getAll()).forEach(({ name }) => {
        if (name.startsWith(PAUSED_ALARM_PREFIX)) {
          chrome.alarms.clear(name);
        }
      });
    }
    return;
  }
  const alarms = (await chrome.alarms.getAll()).filter(
    ({ name }) => name.startsWith(PAUSED_ALARM_PREFIX)
  );
  const revokeHostnames = Object.entries(options.paused).filter(
    ([, { revokeAt }]) => revokeAt
  );
  alarms.forEach(({ name }) => {
    if (!revokeHostnames.find(([id]) => name === `${PAUSED_ALARM_PREFIX}:${id}`)) {
      chrome.alarms.clear(name);
    }
  });
  if (revokeHostnames.length) {
    revokeHostnames.filter(([id]) => !alarms.some(({ name }) => name === id)).forEach(([id, { revokeAt }]) => {
      chrome.alarms.create(`${PAUSED_ALARM_PREFIX}:${id}`, {
        when: revokeAt
      });
    });
  }
});
chrome.alarms.onAlarm.addListener((alarm) => {
  if (alarm.name.startsWith(PAUSED_ALARM_PREFIX)) {
    const id = alarm.name.slice(PAUSED_ALARM_PREFIX.length + 1);
    store.set(Options, { paused: { [id]: null } });
  }
});
