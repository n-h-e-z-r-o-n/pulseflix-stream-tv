globalThis.chrome = globalThis.browser;

import Options, { MODE_DEFAULT, MODE_ZAP } from '../../../store/options.js';
import successDefaultImage from '../assets/success-default.js';
import successZapImage from '../assets/success-zap.js';
import store from '../../../npm/hybrids/src/store.js';
import { html } from '../../../npm/hybrids/src/template/index.js';

const Success = {
  options: store(Options),
  render: {
    connect: () => {
      chrome.runtime.sendMessage({
        action: "telemetry",
        event: "install_complete"
      });
    },
    value: ({ options }) => html`
      <template layout="column gap:2 width:::375px">
        <ui-card data-qa="view:success">
          <section layout="block:center column gap:2">
            ${options.mode === MODE_DEFAULT && html`
              <div layout="row center">
                <img src="${successDefaultImage}" layout="size:20" />
              </div>
              <ui-text type="display-s">Setup Successful</ui-text>
              <ui-text>
                Ghostery is all set to stop trackers in their tracks and protect
                your privacy while browsing!
              </ui-text>
            `}
            ${options.mode === MODE_ZAP && html`
              <div layout="row center">
                <img src="${successZapImage}" layout="size:20" />
              </div>
              <ui-text type="display-s">Setup Successful</ui-text>
              <ui-text>
                You’re all set to zap ads away, one site at a time.
              </ui-text>
              <div layout="column gap:0.5">
                <ui-text><span>1.</span> Open a site</ui-text>
                <ui-text><span>2.</span> Zap ads in the Ghostery panel</ui-text>
                <ui-text
                  ><span>3.</span> Build your own ad-free internet</ui-text
                >
              </div>
            `}
          </section>
        </ui-card>
        ${false}
      </template>
    `
  }
};

export { Success as default };
