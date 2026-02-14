globalThis.chrome = globalThis.browser;

import __vite_glob_0_26 from './report-form.js';
import router from '../../../npm/hybrids/src/router.js';
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


const __vite_glob_0_24 = {
  render: () => html`
    <template layout="column grow shrink">
      <ui-header>
        <ui-text type="label-m" layout="row gap items:center">
          Report an issue
        </ui-text>
        <ui-action slot="actions">
          <a href="${router.backUrl()}">
            <ui-icon name="close" color="primary" layout="size:3"></ui-icon>
          </a>
        </ui-action>
      </ui-header>
      <panel-container>
        <div layout="padding:2:1.5">
          <ui-text type="label-s" layout="margin:bottom">
            Choose category
          </ui-text>
          <div layout="column gap:0.5">
            <panel-actions-button>
              <a
                href="${router.url(__vite_glob_0_26, { category: 'cookie-banner' })}"
              >
                <panel-actions-icon name="cookie"></panel-actions-icon>
                Cookie banner visible
                <ui-icon
                  name="chevron-right"
                  color="tertiary"
                  layout="size:2"
                ></ui-icon>
              </a>
            </panel-actions-button>
            <panel-actions-button>
              <a href="${router.url(__vite_glob_0_26, { category: 'ads-showing' })}">
                <panel-actions-icon name="ads"></panel-actions-icon>
                Ads are showing
                <ui-icon
                  name="chevron-right"
                  color="tertiary"
                  layout="size:2"
                ></ui-icon>
              </a>
            </panel-actions-button>
            <panel-actions-button>
              <a href="${router.url(__vite_glob_0_26, { category: 'page-frozen' })}">
                <panel-actions-icon name="panel-top"></panel-actions-icon>
                Page frozen
                <ui-icon
                  name="chevron-right"
                  color="tertiary"
                  layout="size:2"
                ></ui-icon>
              </a>
            </panel-actions-button>
            <panel-actions-button>
              <a
                href="${router.url(__vite_glob_0_26, {
                  category: 'adblocker-detected',
                })}"
              >
                <panel-actions-icon name="report"></panel-actions-icon>
                Ad blocker detected
                <ui-icon
                  name="chevron-right"
                  color="tertiary"
                  layout="size:2"
                ></ui-icon>
              </a>
            </panel-actions-button>
            <panel-actions-button>
              <a
                href="${router.url(__vite_glob_0_26, { category: 'layout-broken' })}"
              >
                <panel-actions-icon name="panels-top-left"></panel-actions-icon>
                Layout broken
                <ui-icon
                  name="chevron-right"
                  color="tertiary"
                  layout="size:2"
                ></ui-icon>
              </a>
            </panel-actions-button>
          </div>
          <ui-text type="body-s" color="secondary" layout="margin:top:2">
            Didn’t find a match? Select
            ${html`<ui-text type="label-s" underline
              ><a href="${router.url(__vite_glob_0_26)}">Other</a></ui-text
            >`}
            to describe your issue.
          </ui-text>
        </div>
      </panel-container>
    </template>
  `,
};

export { __vite_glob_0_24 as default };
