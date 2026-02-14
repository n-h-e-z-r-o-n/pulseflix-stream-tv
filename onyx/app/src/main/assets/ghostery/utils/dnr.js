globalThis.chrome = globalThis.browser;

/**
 * Ghostery Browser Extension
 * https://www.ghostery.com/
 *
 * Copyright 2017-present Ghostery GmbH. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0
 */

// Dynamic Rules ID Ranges and Priorities

const PAUSED_ID_RANGE = { start: 1, end: 1_000_000 };

async function getDynamicRules(type) {
  return (await chrome.declarativeNetRequest.getDynamicRules()).filter(
    (rule) => rule.id >= type.start && rule.id < type.end,
  );
}

async function getDynamicRulesIds(type) {
  return (await getDynamicRules(type)).map((rule) => rule.id);
}

export { PAUSED_ID_RANGE, getDynamicRules, getDynamicRulesIds };
