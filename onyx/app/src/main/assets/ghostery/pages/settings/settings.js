globalThis.chrome = globalThis.browser;

import { BECOME_A_CONTRIBUTOR_PAGE_URL } from '../../utils/urls.js';
import __vite_glob_0_18 from './views/privacy.js';
import __vite_glob_0_29 from './views/websites.js';
import __vite_glob_0_30 from './views/whotracksme.js';
import __vite_glob_0_17 from './views/my-ghostery.js';
import __vite_glob_0_25 from './views/trackers.js';
import assets from './assets/index.js';
import router from '../../npm/hybrids/src/router.js';
import { html } from '../../npm/hybrids/src/template/index.js';

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


const Settings = {
  stack: router([__vite_glob_0_18, __vite_glob_0_29, __vite_glob_0_30, __vite_glob_0_17, __vite_glob_0_25], {
    transition: true,
  }),
  render: ({ stack }) =>
    html`
      <template layout="contents">
        <settings-layout data-qa="page:settings">
          <a
            href="${router.url(__vite_glob_0_18)}"
            class="${{ active: router.active(__vite_glob_0_18, { stack: true }) }}"
            slot="nav"
            data-qa="button:privacy-protection"
          >
            <ui-icon name="shield-menu" color="nav" layout="size:3"></ui-icon>
            Privacy protection
          </a>
          <a
            href="${router.url(__vite_glob_0_29)}"
            class="${{
              active:
                router.active(__vite_glob_0_29, { stack: true }) &&
                !router.active(__vite_glob_0_25, { stack: true }),
            }}"
            slot="nav"
            data-qa="button:websites"
          >
            <ui-icon name="websites" color="nav" layout="size:3"></ui-icon>
            Websites
          </a>
          <a
            href="${router.url(__vite_glob_0_25)}"
            class="${{
              active: router.active(__vite_glob_0_25, { stack: true }),
            }}"
            slot="nav"
          >
            <ui-icon name="block-m" color="nav" layout="size:3"></ui-icon>
            Trackers
          </a>
          <a
            href="${router.url(__vite_glob_0_30)}"
            class="${{ active: router.active(__vite_glob_0_30), wrap: true }}"
            slot="nav"
            translate="no"
            data-qa="button:whotracksme"
          >
            <ui-icon name="wtm" color="nav" layout="size:3"></ui-icon>
            WhoTracks.Me
          </a>

          <a
            href="${router.url(__vite_glob_0_17)}"
            class="${{ active: router.active(__vite_glob_0_17), bottom: true }}"
            slot="nav"
            data-qa="button:my-ghostery"
          >
            <ui-icon name="user" color="nav"></ui-icon>
            My Ghostery
          </a>
          <settings-card
            layout="hidden"
            layout@992px="
              area::6/7 self:end:stretch
              margin:top:2 padding:2 gap
              column items:center
            "
            slot="nav"
          >
            <img
              src="${assets.hands}"
              layout="size:12"
              alt="Contribution"
              slot="picture"
            />
            <div layout="column gap:0.5">
              <ui-text type="label-l" layout="block:center">
                Become a Contributor
              </ui-text>
              <ui-text type="body-s" color="secondary" layout="block:center">
                Help Ghostery fight for a web where privacy is a basic human
                right.
              </ui-text>
              <ui-button type="primary" layout="margin:top">
                <a
                  href="${BECOME_A_CONTRIBUTOR_PAGE_URL}?utm_source=gbe&utm_campaign=settings-becomeacontributor"
                  target="_blank"
                >
                  Become a Contributor
                </a>
              </ui-button>
            </div>
          </settings-card>
          <div layout="column grow height::0 view:main layer">${stack}</div>
        </settings-layout>
      </template>
    `.use(html.transition),
};

export { Settings as default };
