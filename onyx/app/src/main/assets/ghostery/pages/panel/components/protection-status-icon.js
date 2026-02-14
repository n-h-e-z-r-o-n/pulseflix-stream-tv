globalThis.chrome = globalThis.browser;

import Options from '../../../store/options.js';
import { getStatus, getLabel } from '../../../utils/exceptions.js';
import store from '../../../npm/hybrids/src/store.js';
import { html } from '../../../npm/hybrids/src/template/index.js';

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


const __vite_glob_0_17 = {
  options: store(Options),
  trackerId: '',
  hostname: '',
  label: ({ options, trackerId, hostname }) =>
    getLabel(options, trackerId, hostname),
  exceptionStatus: ({ options, trackerId, hostname }) =>
    getStatus(options, trackerId, hostname),
  render: ({ label, exceptionStatus }) => html`
    <template layout="contents">
      <ui-tooltip>
        <span slot="content"> ${label} </span>
        <div layout="relative">
          <ui-icon
            name="${exceptionStatus.trusted ? 'trust' : 'block'}-m"
            color="${exceptionStatus.trusted ? 'secondary' : 'quaternary'}"
          ></ui-icon>

          ${exceptionStatus.trusted &&
          !exceptionStatus.global &&
          html`
            <div id="pin-icon" layout="row center absolute right:-1 bottom:-1">
              <ui-icon name="pin-filled"></ui-icon>
            </div>
          `}
        </div>
      </ui-tooltip>
    </template>
  `.css`
    #pin-icon {
      background: var(--color-secondary);
      color: var(--background-secondary);
      border-radius: 50%;
      width: 18px;
      height: 18px;
      box-shadow: 0 2px 4px var(--shadow-panel);
    }
  `,
};

export { __vite_glob_0_17 as default };
