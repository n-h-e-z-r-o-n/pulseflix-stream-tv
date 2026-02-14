globalThis.chrome = globalThis.browser;

import Options, { MODE_DEFAULT } from '../../../store/options.js';
import ManagedConfig from '../../../store/managed-config.js';
import { TRACKERS_PREVIEW_LEARN_MORE_URL, PAUSE_ASSISTANT_LEARN_MORE_URL } from '../../../utils/urls.js';
import assets from '../assets/index.js';
import store from '../../../npm/hybrids/src/store.js';
import { html } from '../../../npm/hybrids/src/template/index.js';

const __vite_glob_0_30 = {
  options: store(Options),
  managedConfig: store(ManagedConfig),
  render: ({ options, managedConfig }) => html`
    <template layout="contents">
      <settings-page-layout layout="gap:4" layout@768px="gap:4">
        <div layout="column gap" layout@992px="margin:bottom">
          <ui-text type="headline-m" translate="no">WhoTracks.Me</ui-text>
          <ui-text type="body-l" mobile-type="body-m" color="secondary">
            WhoTracks.Me, operated by Ghostery, is a vital cornerstone of
            Ghostery’s AI anti-tracking technology, playing a crucial role in
            providing real-time privacy protection for the Ghostery community.
            It is a comprehensive global resource on trackers, bringing
            transparency to web tracking.
          </ui-text>
          <ui-text type="body-l" mobile-type="body-m" color="secondary">
            It exists thanks to the micro-contributions of every Ghostery user
            who chooses to send non-personal information to WhoTracks.Me. This
            input enables Ghostery to provide real-time intel on trackers,
            which, in turn, delivers privacy protection to the entire Ghostery
            community.
          </ui-text>
        </div>
        ${store.ready(options, managedConfig) && html`
          <section layout="column gap:4">
            ${options.mode === MODE_DEFAULT && html`
              <ui-toggle
                value="${options.pauseAssistant}"
                onchange="${html.set(options, "pauseAssistant")}"
                data-qa="toggle:pauseAssistant"
              >
                <div layout="row items:start gap:2 grow" layout@768px="gap:3">
                  <settings-help-image>
                    <img
                      src="${assets.pause_assistant}"
                      alt="Browsing Assistant"
                    />
                  </settings-help-image>
                  <settings-option>
                    Browsing Assistant
                    <span slot="description">
                      Detects and prevents ad blocker breakage by automatically
                      pausing on affected pages.
                    </span>
                    <ui-text
                      type="label-s"
                      color="secondary"
                      underline
                      slot="footer"
                      layout="self:start"
                    >
                      <a
                        href="${PAUSE_ASSISTANT_LEARN_MORE_URL}"
                        target="_blank"
                        layout="row gap:0.5"
                      >
                        Learn more <ui-icon name="arrow-right-s"></ui-icon>
                      </a>
                    </ui-text>
                  </settings-option>
                </div>
              </ui-toggle>
            `}
            <ui-toggle
              value="${options.trackerWheel}"
              onchange="${html.set(options, "trackerWheel")}"
              data-qa="toggle:trackerWheel"
            >
              <div layout="row items:start gap:2 grow" layout@768px="gap:3">
                <settings-help-image>
                  <img src="${assets.wtm_wheel}" alt="WhoTracks.Me Wheel" />
                </settings-help-image>
                <settings-option>
                  WhoTracks.Me Wheel
                  <span slot="description">
                    Replaces the Ghostery icon in the browser toolbar with the
                    tracker wheel.
                  </span>
                </settings-option>
              </div>
            </ui-toggle>
            ${Options.trackerCount && html`
              <ui-toggle
                value="${options.trackerCount}"
                onchange="${html.set(options, "trackerCount")}"
                data-qa="toggle:trackerCount"
              >
                <div layout="row items:start gap:2 grow" layout@768px="gap:3">
                  <settings-help-image>
                    <img src="${assets.trackers_count}" alt="Trackers Count" />
                  </settings-help-image>
                  <settings-option>
                    Trackers Count
                    <span slot="description">
                      Displays the tracker count on the Ghostery icon in the
                      browser toolbar.
                    </span>
                  </settings-option>
                </div>
              </ui-toggle>
            `}
            <ui-toggle
              value="${options.wtmSerpReport}"
              onchange="${html.set(options, "wtmSerpReport")}"
              data-qa="toggle:wtmSerpReport"
              disabled="${managedConfig.disableTrackersPreview}"
            >
              <div layout="row items:start gap:2 grow" layout@768px="gap:3">
                <settings-help-image>
                  <img
                    src="${assets.trackers_preview}"
                    alt="Trackers Preview"
                  />
                </settings-help-image>
                <settings-option>
                  Trackers Preview
                  <span slot="description">
                    Shows the tracker preview beside search results.
                  </span>
                  <ui-text
                    type="label-s"
                    color="secondary"
                    underline
                    slot="footer"
                    layout="self:start"
                  >
                    <a
                      href="${TRACKERS_PREVIEW_LEARN_MORE_URL}"
                      target="_blank"
                      layout="row gap:0.5"
                    >
                      Learn more <ui-icon name="arrow-right-s"></ui-icon>
                    </a>
                  </ui-text>
                </settings-option>
              </div>
            </ui-toggle>
            ${html`
              <ui-toggle
                value="${options.feedback}"
                onchange="${html.set(options, "feedback")}"
              >
                <div layout="row items:start gap:2 grow" layout@768px="gap:3">
                  <settings-help-image static>
                    <img src="${assets.feedback}" alt="Feedback Sharing" />
                  </settings-help-image>
                  <settings-option>
                    Feedback Sharing
                    <span slot="description">
                      Contributes non-personal information about add-on health
                      and performance telemetry to help advance privacy
                      protections for the entire Ghostery community.
                    </span>
                  </settings-option>
                </div>
              </ui-toggle>
            `}

            <div layout="row items:start gap:2 grow" layout@768px="gap:3">
              <ui-action>
                <a
                  href="${chrome.runtime.getURL(
    "/pages/whotracksme/index.html"
  )}"
                  target="_blank"
                  layout="row gap:2"
                  layout@768px="gap:3"
                >
                  <settings-help-image static>
                    <img
                      src="${assets.wtm_privacy_report}"
                      alt="WTM Privacy Report"
                    />
                  </settings-help-image>
                  <settings-option>
                    Your Browser Privacy Report
                    <span slot="description">
                      Generates a global transparency report on web tracking in
                      your Ghostery-protected browser.
                    </span>
                  </settings-option>
                </a>
              </ui-action>
              <ui-button size="s">
                <a
                  href="${chrome.runtime.getURL(
    "/pages/whotracksme/index.html"
  )}"
                  target="_blank"
                >
                  View Report
                </a>
              </ui-button>
            </div>
          </section>
        `}
      </settings-page-layout>
    </template>
  `
};

export { __vite_glob_0_30 as default };
