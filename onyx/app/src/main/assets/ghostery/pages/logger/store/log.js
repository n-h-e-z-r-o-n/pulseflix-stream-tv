globalThis.chrome = globalThis.browser;

import '../../../npm/tldts-experimental/npm/tldts-core/dist/es6/src/options.js';
import '../../../npm/@ghostery/adblocker/dist/esm/data-view.js';
import '../../../npm/@ghostery/adblocker/dist/esm/fetch.js';
import '../../../npm/@ghostery/adblocker/dist/esm/filters/cosmetic.js';
import { FilterType } from '../../../npm/@ghostery/adblocker/dist/esm/lists.js';
import '../../../npm/@ghostery/adblocker/dist/esm/request.js';
import '../../../npm/@remusao/small/dist/esm/index.js';
import '../../../npm/@ghostery/adblocker/dist/esm/filters/network.js';
import '../../../npm/@ghostery/adblocker/dist/esm/preprocessor.js';
import store from '../../../npm/hybrids/src/store.js';

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

const storage = [];

const Log = {
  id: true,
  tabId: '',

  // From request
  url: '',
  timestamp: 0,
  blocked: false,
  modified: false,
  type: '',

  // From matched filter
  filter: '',
  filterType: FilterType.NETWORK, // FilterType enum from adblocker library

  // From TrackerDB
  tracker: '',
  organization: '',

  time: ({ timestamp }) => new Date(timestamp).toLocaleTimeString(),
  typeLabel: ({ filterType, type }) =>
    `${FilterType[filterType].toLowerCase()}${type ? ` (${type})` : ''}`,

  [store.connect]: {
    get: (id) => storage.find((item) => item.id === id),
    set: (id, values) => {
      values = {
        ...values,
        type: values.type === 'xmlhttprequest' ? 'xhr' : values.type,
      };

      const log = storage.find((item) => item.id === id);
      if (log) {
        Object.assign(log, values);
      } else {
        storage.push(values);
      }

      return values;
    },
    list: ({ tabId, query, filterType }) => {
      query = query && new RegExp(query.trim(), 'i');

      if (!tabId && !query && !filterType)
        return storage.slice().sort((a, b) => a.timestamp - b.timestamp);

      return storage
        .filter((log) => {
          let match = true;

          if (tabId && log.tabId !== tabId) {
            match = false;
          }

          if (filterType && log.filterType !== filterType) {
            match = false;
          }

          if (
            query &&
            !query.test(log.url) &&
            !query.test(log.filter) &&
            !query.test(log.tracker) &&
            !query.test(log.organization)
          ) {
            match = false;
          }

          return match;
        })
        .sort((a, b) => a.timestamp - b.timestamp);
    },
    loose: true,
  },
};

export { Log as default };
